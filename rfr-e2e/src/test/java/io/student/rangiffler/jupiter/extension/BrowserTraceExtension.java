package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Пишет Playwright trace контекста UI-теста: screenshots и snapshots.
 * {@code trace.zip} сохраняется и прикладывается к Allure только если тест упал.
 */
public final class BrowserTraceExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

  private static final String RESULTS_DIRECTORY_PROPERTY = "allure.results.directory";
  private static final String DEFAULT_RESULTS_DIRECTORY = "build/allure-results";
  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(BrowserTraceExtension.class);

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    BrowserContext browserContext = browserContext(context);
    if (browserContext == null) {
      return;
    }
    browserContext.tracing().start(new Tracing.StartOptions()
        .setScreenshots(true)
        .setSnapshots(true));
    context.getStore(NAMESPACE).put(context.getUniqueId(), Boolean.TRUE);
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    if (context.getStore(NAMESPACE).get(context.getUniqueId()) == null) {
      return;
    }
    BrowserContext browserContext = browserContext(context);
    if (browserContext == null) {
      return;
    }
    if (context.getExecutionException().isEmpty()) {
      browserContext.tracing().stop();
      return;
    }
    Path trace = tracePath(context);
    browserContext.tracing().stop(new Tracing.StopOptions().setPath(trace));
    attach(trace);
  }

  private static void attach(Path trace) {
    try (InputStream input = Files.newInputStream(trace)) {
      Allure.attachment(
          "trace.zip",
          "application/zip",
          input,
          AttachmentOptions.withFileExtension(".zip")
      );
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to attach Playwright trace " + trace.toAbsolutePath(), e);
    }
  }

  private static Path tracePath(ExtensionContext context) {
    String fileName = context.getRequiredTestClass().getSimpleName()
        + "-"
        + context.getRequiredTestMethod().getName()
        + "-trace.zip";
    Path directory = resultsDirectory();
    try {
      Files.createDirectories(directory);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create Allure results directory " + directory.toAbsolutePath(), e);
    }
    return directory.resolve(fileName);
  }

  private static Path resultsDirectory() {
    String configured = System.getProperty(RESULTS_DIRECTORY_PROPERTY);
    if (configured != null && !configured.isBlank()) {
      return Path.of(configured);
    }
    return Path.of(DEFAULT_RESULTS_DIRECTORY);
  }

  private static BrowserContext browserContext(ExtensionContext context) {
    Object instance = context.getRequiredTestInstance();
    if (!(instance instanceof BaseUiTest uiTest)) {
      return null;
    }
    return uiTest.context();
  }
}
