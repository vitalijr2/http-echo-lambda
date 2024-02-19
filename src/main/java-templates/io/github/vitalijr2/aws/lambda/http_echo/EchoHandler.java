package io.github.vitalijr2.aws.lambda.http_echo;

import static java.util.Objects.nonNull;
import static java.util.Optional.empty;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class EchoHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  public static final String LOGGER_NAME = "echo";

  private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent,
      Context context) {
    MDC.put("@aws-request-id@", context.getAwsRequestId());

    Optional<APIGatewayProxyResponseEvent> responseEvent = empty();
    var requestBody = requestEvent.getBody();
    var requestHeaders = requestEvent.getHeaders();
    var responseBody = new JSONObject();

    if (nonNull(requestHeaders) && !requestHeaders.isEmpty()) {
      var headers = new JSONArray();
      requestHeaders.forEach((key, value) -> headers.put(key + ": " + value));
      responseBody.put("headers", headers);
      logger.info("Request headers:\n{}", headers.toString());
    }
    if (nonNull(requestBody) && !requestBody.isBlank()) {
      responseBody.put("body", requestBody);
      logger.info("Request body:\n{}", requestBody);
    }
    if (!responseBody.isEmpty()) {
      responseEvent = Optional.of(LambdaUtils.getResponseEvent(responseBody.toString()));
    }

    return responseEvent.orElseGet(LambdaUtils::responseOK);
  }

}
