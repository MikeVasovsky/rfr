package io.student.rangiffler.jupiter.extension;

import io.qameta.allure.Allure;
import io.student.rangiffler.data.fixture.StaticUserFixture;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * Жизненный цикл {@link StaticUserFixture}: пользователь из {@link io.student.rangiffler.data.fixture.UsersQueue}.
 * Фикстура кладётся в Store до {@code create()}, чтобы {@code close()} вернул пользователя при закрытии Store.
 */
public final class UsersQueueExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(UsersQueueExtension.class);
  private static final String FIXTURE_KEY = "staticUserFixture";

  @Override
  public void beforeEach(ExtensionContext context) {
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (Parameter parameter : parameters) {
      if (parameter.getAnnotation(StaticUser.class) != null
          && TestUser.class.isAssignableFrom(parameter.getType())) {
        Allure.step("Взять пользователя из очереди", () -> {
          StaticUserFixture fixture = new StaticUserFixture();
          store(context).put(FIXTURE_KEY, fixture);
          fixture.create();
        });
        break;
      }
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(StaticUser.class)
        && TestUser.class.isAssignableFrom(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return Optional.ofNullable(store(extensionContext).get(FIXTURE_KEY, UserFixture.class))
        .map(UserFixture::user)
        .orElseThrow(() -> new ParameterResolutionException(
            "Пользователь не создан в BeforeEachCallback для параметра @StaticUser TestUser"
        ));
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Optional.ofNullable(store(context).remove(FIXTURE_KEY, UserFixture.class))
        .ifPresent(fixture -> Allure.step("Вернуть пользователя в очередь", fixture::close));
  }

  private static ExtensionContext.Store store(ExtensionContext context) {
    return context.getStore(NAMESPACE);
  }
}
