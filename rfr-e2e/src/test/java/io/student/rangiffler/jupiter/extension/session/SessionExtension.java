package io.student.rangiffler.jupiter.extension.session;

import io.qameta.allure.Allure;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.fixture.session.BrowserRuntime;
import io.student.rangiffler.data.fixture.session.GuestSessionFixture;
import io.student.rangiffler.data.fixture.session.SessionFixture;
import io.student.rangiffler.data.fixture.session.UserSessionFixture;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.UserSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dual UI-сессии: поднимает {@link BrowserRuntime}, для каждого {@link Session} — соответствующий
 * {@link SessionFixture}, в тест отдаёт {@link UserSession}. UI-login — в тесте.
 */
public final class SessionExtension implements BeforeEachCallback, ParameterResolver {

  private static final TestConfig CFG = TestConfig.getInstance();
  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(SessionExtension.class);
  private static final String BROWSER_KEY = "browserRuntime";

  @Override
  public void beforeEach(ExtensionContext context) {
    List<Integer> sessionIndexes = sessionParameterIndexes(context);
    if (sessionIndexes.isEmpty()) {
      return;
    }

    BrowserRuntime runtime = new BrowserRuntime();
    store(context).put(BROWSER_KEY, runtime);
    Allure.step("Запустить Playwright Browser для dual-session", () -> runtime.launch(CFG));

    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (int index : sessionIndexes) {
      Session annotation = parameters[index].getAnnotation(Session.class);
      SessionFixture fixture = newFixture(annotation);
      store(context).put(sessionKey(index), fixture);
      Allure.step("Подготовить session[" + index + "]", () ->
          fixture.start(runtime, annotation, CFG)
      );
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return isSessionParameter(parameterContext.getParameter());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    int index = parameterContext.getIndex();
    SessionFixture fixture = store(extensionContext).get(sessionKey(index), SessionFixture.class);
    if (fixture == null || fixture.session() == null) {
      throw new ParameterResolutionException(
          "UserSession не подготовлен в BeforeEach для параметра index=" + index
      );
    }
    return fixture.session();
  }

  private static SessionFixture newFixture(Session annotation) {
    return annotation.guest() ? new GuestSessionFixture() : new UserSessionFixture();
  }

  private static List<Integer> sessionParameterIndexes(ExtensionContext context) {
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    List<Integer> indexes = new ArrayList<>();
    for (int i = 0; i < parameters.length; i++) {
      if (isSessionParameter(parameters[i])) {
        indexes.add(i);
      }
    }
    return indexes;
  }

  private static boolean isSessionParameter(Parameter parameter) {
    return parameter.getAnnotation(Session.class) != null
        && UserSession.class.isAssignableFrom(parameter.getType());
  }

  private static String sessionKey(int index) {
    return "sessionFixture-" + index;
  }

  private static ExtensionContext.Store store(ExtensionContext context) {
    return context.getStore(NAMESPACE);
  }
}
