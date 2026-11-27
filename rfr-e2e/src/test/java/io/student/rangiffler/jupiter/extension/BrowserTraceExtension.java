package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Пишет Playwright trace: screenshots, snapshots и sources.
 * {@link #start}/{@link #stop} — для основного и session-контекстов;
 * zip в Allure только у упавшего теста.
 */
@Order(400)
public final class BrowserTraceExtension implements BeforeEachCallback, AfterTestExecutionCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(BrowserTraceExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    Object instance = context.getTestInstance().orElse(null);
    if (!(instance instanceof BaseUiTest uiTest)) {
      return;
    }
    BrowserContext browserContext = uiTest.context();
    if (browserContext == null) {
      throw new ExtensionConfigurationException(
          "BrowserContext is null in BrowserTraceExtension.beforeEach: "
              + "BrowserLifecycleExtension must run first"
      );
    }
    start(browserContext.tracing());
    context.getStore(NAMESPACE).put(context.getUniqueId(), Boolean.TRUE);
  }

  @Override
  public void afterTestExecution(ExtensionContext context) throws IOException {
    if (context.getStore(NAMESPACE).get(context.getUniqueId()) == null) {
      return;
    }
    BrowserContext browserContext = browserContext(context);
    if (browserContext == null) {
      return;
    }
    stop(browserContext.tracing(), "trace.zip", context);
  }

  /**
   * Старт трассировки любого {@link BrowserContext} (основной или session).
   */
  public static void start(Tracing tracing) {
    tracing.start(new Tracing.StartOptions()
        .setScreenshots(true)
        .setSnapshots(true)
        .setSources(true));
  }

  /**
   * Стоп трассировки: у упавшего теста — temp zip в Allure под {@code attachment}, файл удаляется;
   * у зелёного — {@code tracing.stop()} без файла.
   */
  public static void stop(Tracing tracing, String attachment, ExtensionContext context) throws IOException {
    if (context.getExecutionException().isEmpty()) {
      tracing.stop();
      return;
    }
    Path trace = Files.createTempFile("playwright-trace-", ".zip");
    try {
      tracing.stop(new Tracing.StopOptions().setPath(trace));
      try (InputStream input = Files.newInputStream(trace)) {
        Allure.attachment(
            attachment,
            "application/zip",
            input,
            AttachmentOptions.withFileExtension(".zip")
        );
      }
    } finally {
      Files.deleteIfExists(trace);
    }
  }

  private static BrowserContext browserContext(ExtensionContext context) {
    Object instance = context.getTestInstance().orElse(null);
    if (!(instance instanceof BaseUiTest uiTest)) {
      return null;
    }
    return uiTest.context();
  }
}
