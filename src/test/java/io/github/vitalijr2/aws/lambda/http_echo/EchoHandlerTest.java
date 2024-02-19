package io.github.vitalijr2.aws.lambda.http_echo;

import static io.github.vitalijr2.aws.lambda.http_echo.EchoHandler.LOGGER_NAME;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Map;
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
  private APIGatewayProxyRequestEvent requestEvent;

  private Logger logger;
  private RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> handler;

  @AfterEach
  void tearDown() {
    clearInvocations(logger);
  }

  @BeforeEach
  void setUp() {
    when(context.getAwsRequestId()).thenReturn("test-id");
    handler = new EchoHandler();
    logger = LoggerFactory.getLogger(LOGGER_NAME);
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
    verifyNoInteractions(logger);

    assertAndVerify(responseEvent, "OK", "text/plain");
  }

  @DisplayName("A request headers")
  @Test
  void requestHeaders() {
    // given
    when(requestEvent.getHeaders()).thenReturn(Map.of("key", "value"));

    // when
    var responseEvent = handler.handleRequest(requestEvent, context);

    // then
    verify(logger).info(eq("Request headers:\n{}"), anyString());
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
    verify(logger).info(eq("Request body:\n{}"), anyString());
    verifyNoMoreInteractions(logger);

    assertAndVerify(responseEvent, "{\"body\":\"qwerty\"}", "application/json");
  }

  private void assertAndVerify(APIGatewayProxyResponseEvent responseEvent, String expectedBody,
      String expectedContentType) {
    verify(context).getAwsRequestId();
    assertAll("Response", () -> assertEquals(expectedBody, responseEvent.getBody(), "Body"),
        () -> assertEquals(expectedContentType, responseEvent.getHeaders().get("Content-Type"),
            "Content type"), () -> assertEquals(200, responseEvent.getStatusCode(), "Status code"));
  }

}