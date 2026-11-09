package io.student.rangiffler.utils;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

/**
 * Ожидание успешного GraphQL-ответа Playwright при действии, которое его запускает.
 */
public final class GraphqlResponses {

  private GraphqlResponses() {
  }

  public static Response waitForSuccess(Page page, String operationName, Runnable trigger) {
    return page.waitForResponse(
        response -> {
          String postData = response.request().postData();
          if (!response.url().contains("/graphql")
              || postData == null
              || !postData.contains(operationName)
              || !response.ok()) {
            return false;
          }
          String body = response.text();
          return body == null || !body.contains("\"errors\"");
        },
        trigger
    );
  }
}
