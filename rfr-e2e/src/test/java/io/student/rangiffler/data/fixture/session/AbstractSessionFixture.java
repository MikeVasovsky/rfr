package io.student.rangiffler.data.fixture.session;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.model.UserSession;

abstract class AbstractSessionFixture implements SessionFixture {

  protected BrowserContext context;
  protected Page page;
  protected UserSession session;
  private boolean closed;

  protected final void openBrowser(BrowserRuntime runtime, TestConfig config) {
    context = runtime.newContext(config);
    page = context.newPage();
    page.setDefaultTimeout(config.defaultTimeoutMs());
  }

  @Override
  public final UserSession session() {
    return session;
  }

  protected abstract void closeData();

  @Override
  public final void close() {
    if (closed) {
      return;
    }
    closed = true;
    RuntimeException first = null;
    try {
      if (context != null) {
        context.close();
      }
    } catch (RuntimeException e) {
      first = e;
    }
    try {
      closeData();
    } catch (RuntimeException e) {
      if (first == null) {
        first = e;
      } else {
        first.addSuppressed(e);
      }
    }
    if (first != null) {
      throw first;
    }
  }
}
