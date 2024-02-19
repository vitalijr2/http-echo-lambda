/*
 * Copyright 2022-2023 Vitalij Berdinskih
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.vitalijr2.aws.lambda.http_echo;

import static java.util.Collections.singletonMap;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.jetbrains.annotations.NotNull;

public class LambdaUtils {

  // Response
  private static final String APPLICATION_JSON = "application/json";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final int HTTP_OK = 200;
  private static final String OK = "OK";
  private static final String TEXT_HTML = "text/html";

  private LambdaUtils() {
  }

  /**
   * Wrap an object to the Gateway response, set <tt>Content-Type</tt> to
   * <tt>application/json</tt>.
   *
   * @param body response body
   * @return Gateway response that wraps body.
   */
  @NotNull
  public static APIGatewayV2HTTPResponse getResponseEvent(@NotNull Object body) {
    var responseEvent = new APIGatewayV2HTTPResponse();

    responseEvent.setBody(body.toString());
    responseEvent.setHeaders(singletonMap(CONTENT_TYPE, APPLICATION_JSON));
    responseEvent.setIsBase64Encoded(false);
    responseEvent.setStatusCode(HTTP_OK);

    return responseEvent;
  }

  @NotNull
  public static APIGatewayV2HTTPResponse responseOK(String body) {
    var responseEvent = getResponseEvent(body);

    responseEvent.setHeaders(singletonMap(CONTENT_TYPE, TEXT_HTML));

    return responseEvent;
  }

  /**
   * Make Gateway response with <em>OK</em> string.
   *
   * @return Gateway &quot;OK&quot; response
   */
  @NotNull
  public static APIGatewayV2HTTPResponse responseOK() {
    return responseOK(OK);
  }

}
