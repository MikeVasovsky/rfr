package io.student.rangiffler.jupiter.extension.demo;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Демо-extension: все lifecycle-callback'и Jupiter и {@link ParameterResolver}.
 * В логах печатает identityHashCode instance — видно, шарится ли объект между тестами.
 */
public final class DemoExtension implements
    BeforeAllCallback,
    AfterAllCallback,
    BeforeEachCallback,
    AfterEachCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    ParameterResolver {

  public static final String RESOLVED_VALUE = "resolved by DemoExtension";

  private final int instanceId = System.identityHashCode(this);

  public DemoExtension() {
    System.out.println("DemoExtension.<init> instance=" + instanceId);
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    System.out.println("DemoExtension.beforeAll instance=" + instanceId + " " + context.getDisplayName());
  }

  @Override
  public void afterAll(ExtensionContext context) {
    System.out.println("DemoExtension.afterAll instance=" + instanceId + " " + context.getDisplayName());
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    System.out.println("DemoExtension.beforeEach instance=" + instanceId + " " + context.getDisplayName());
  }

  @Override
  public void afterEach(ExtensionContext context) {
    System.out.println("DemoExtension.afterEach instance=" + instanceId + " " + context.getDisplayName());
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    System.out.println("DemoExtension.beforeTestExecution instance=" + instanceId + " " + context.getDisplayName());
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    System.out.println("DemoExtension.afterTestExecution instance=" + instanceId + " " + context.getDisplayName());
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(String.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    System.out.println("DemoExtension.resolveParameter instance=" + instanceId
        + " " + parameterContext.getParameter().getName());
    return RESOLVED_VALUE;
  }
}
