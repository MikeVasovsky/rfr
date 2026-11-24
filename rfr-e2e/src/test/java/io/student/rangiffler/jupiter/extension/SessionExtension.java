package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.session.GuestSession;
import io.student.rangiffler.session.UserSession;
import io.student.rangiffler.session.fixture.GuestSessionFixture;
import io.student.rangiffler.session.fixture.SessionFixture;
import io.student.rangiffler.session.fixture.UserSessionFixture;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;

/**
 * Dual UI-сессии: второй {@link BrowserContext} на том же {@link Browser}, что у {@link BaseUiTest}.
 * {@code @Session GuestSession} — без {@code @User}; {@code @Session @User UserSession} — с UI-login.
 */
public final class SessionExtension implements BeforeTestExecutionCallback, ParameterResolver {

  private static final TestConfig CFG = TestConfig.getInstance();
  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(SessionExtension.class);

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (int index = 0; index < parameters.length; index++) {
      if (parameters[index].isAnnotationPresent(Session.class)) {
        SessionFixture<?> fixture = fixture(context, parameters[index], index);
        context.getStore(NAMESPACE).put(key(index), fixture);
        fixture.create();
      }
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
    Class<?> type = parameter.getParameter().getType();
    return (type == GuestSession.class || type == UserSession.class)
        && parameter.isAnnotated(Session.class)
        && context.getTestMethod().filter(parameter.getDeclaringExecutable()::equals).isPresent();
  }

  @Override
  public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
    SessionFixture<?> fixture = context.getStore(NAMESPACE).get(key(parameter.getIndex()), SessionFixture.class);
    if (fixture == null) {
      throw new ParameterResolutionException(
          "No session was created for parameter " + parameter.getIndex()
              + " of " + context.getRequiredTestMethod()
      );
    }
    return fixture.session();
  }

  private static SessionFixture<?> fixture(ExtensionContext context, Parameter parameter, int index) {
    boolean annotated = parameter.isAnnotationPresent(User.class);
    BrowserContext browser = browserContext(context);
    if (parameter.getType() == GuestSession.class && !annotated) {
      return new GuestSessionFixture(browser);
    }
    if (parameter.getType() == UserSession.class && annotated) {
      TestUser user = UserExtension.user(context, index);
      return new UserSessionFixture(browser, user);
    }
    throw new ExtensionConfigurationException(
        "@Session expects GuestSession without @User or UserSession with @User, got "
            + (annotated ? "@User " : "") + parameter.getType().getSimpleName()
            + " in parameter " + index + " of " + context.getRequiredTestMethod()
    );
  }

  private static BrowserContext browserContext(ExtensionContext context) {
    Object instance = context.getRequiredTestInstance();
    if (!(instance instanceof BaseUiTest uiTest)) {
      throw new ExtensionConfigurationException("@Session requires a test that extends BaseUiTest");
    }
    Browser browser = uiTest.browser();
    if (browser == null) {
      throw new ExtensionConfigurationException("Browser is not started before @Session");
    }
    BrowserContext browserContext = browser.newContext(
        new Browser.NewContextOptions()
            .setViewportSize(CFG.viewportWidth(), CFG.viewportHeight())
    );
    BrowserTimeoutExtension.apply(browserContext);
    return browserContext;
  }

  private static String key(int index) {
    return "session-" + index;
  }
}
