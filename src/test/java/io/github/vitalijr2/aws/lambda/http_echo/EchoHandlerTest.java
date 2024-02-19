package io.github.vitalijr2.aws.lambda.http_echo;

import static io.github.vitalijr2.aws.lambda.http_echo.EchoHandler.LOGGER_NAME;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Http;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class EchoHandlerTest {

  @Mock
  private Context context;
  @Mock
  private Http request;
  @Mock
  private RequestContext requestContext;
  @Mock
  private APIGatewayV2HTTPEvent requestEvent;

  private Logger logger;
  private RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> handler;

  @AfterEach
  void tearDown() {
    clearInvocations(logger);
  }

  @BeforeEach
  void setUp() {
    when(context.getAwsRequestId()).thenReturn("test-id");
    handler = new EchoHandler();
    logger = LoggerFactory.getLogger(LOGGER_NAME);

    when(requestContext.getHttp()).thenReturn(request);
    when(requestEvent.getRequestContext()).thenReturn(requestContext);
  }

  @DisplayName("A request event with an empty body and without headers")
  @ParameterizedTest(name = "[{index}] body <{0}>")
  @NullAndEmptySource
  @ValueSource(strings = " ")
  void emptyRequest(String body) {
    // given
    when(requestEvent.getBody()).thenReturn(body);

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(logger).debug(eq("Request event: {}"), same(requestEvent));
    verifyNoMoreInteractions(logger);

    assertAndVerify(responseEvent, "OK", "text/html");
  }

  @DisplayName("A request headers")
  @Test
  void requestHeaders() {
    // given
    when(requestEvent.getHeaders()).thenReturn(Map.of("key", "value"));

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(logger).debug(eq("Request event: {}"), same(requestEvent));
    verify(logger).info(eq("Request headers:\n{}"), isA(JSONArray.class));
    verifyNoMoreInteractions(logger);

    assertAndVerify(responseEvent, "{\"headers\":[\"key: value\"]}", "application/json");
  }

  @DisplayName("A request body")
  @Test
  void requestBody() {
    // given
    when(requestEvent.getBody()).thenReturn("qwerty");

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(logger).debug(eq("Request event: {}"), same(requestEvent));
    verify(logger).info(eq("Request body:\n{}"), anyString());
    verifyNoMoreInteractions(logger);

    assertAndVerify(responseEvent, "{\"body\":\"qwerty\"}", "application/json");
  }

  @DisplayName("An index page")
  @Test
  void indexPage() {
    // given
    when(request.getMethod()).thenReturn("GET");
    when(request.getPath()).thenReturn("/");

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(logger).debug(eq("Request event: {}"), same(requestEvent));
    verifyNoMoreInteractions(logger);

    assertAndVerify(responseEvent, "<!doctype html><html><body>test</body></html>", "text/html");
  }

  private void assertAndVerify(APIGatewayV2HTTPResponse responseEvent, String expectedBody,
      String expectedContentType) {
    verify(context).getAwsRequestId();
    assertAll("Response", () -> assertEquals(expectedBody, responseEvent.getBody(), "Body"),
        () -> assertEquals(expectedContentType, responseEvent.getHeaders().get("Content-Type"),
            "Content type"), () -> assertEquals(200, responseEvent.getStatusCode(), "Status code"));
  }

}