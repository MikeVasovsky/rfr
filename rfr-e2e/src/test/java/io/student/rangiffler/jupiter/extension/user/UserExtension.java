package io.student.rangiffler.jupiter.extension.user;

import io.qameta.allure.Allure;
import io.student.rangiffler.data.fixture.user.UserRequest;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Жизненный цикл тестового пользователя в auth БД:
 * {@link BeforeEachCallback} создаёт {@link UserFixture},
 * {@link ParameterResolver} передаёт {@code @User TestUser},
 * {@link AfterEachCallback} закрывает fixture.
 */
public final class UserExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(UserExtension.class);
  private static final String FIXTURE_KEY = "userFixture";

  private static final Function<User, UserFixture> DEFAULT_FIXTURE_FACTORY =
      annotation -> UserRequest.from(annotation).fixture();

  private static final AtomicReference<Function<User, UserFixture>> FIXTURE_FACTORY =
      new AtomicReference<>(DEFAULT_FIXTURE_FACTORY);

  public static void installFixtureFactory(Function<User, UserFixture> factory) {
    FIXTURE_FACTORY.set(Objects.requireNonNull(factory, "factory"));
  }

  public static void resetFixtureFactory() {
    FIXTURE_FACTORY.set(DEFAULT_FIXTURE_FACTORY);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (Parameter p : parameters) {
      User u = p.getAnnotation(User.class);
      if (u != null && TestUser.class.isAssignableFrom(p.getType())) {
        Allure.step("Создать пользователя в auth БД", () ->
            store(context).put(FIXTURE_KEY, FIXTURE_FACTORY.get().apply(u))
        );
        break;
      }
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(User.class)
        && TestUser.class.isAssignableFrom(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return Optional.ofNullable(store(extensionContext).get(FIXTURE_KEY, UserFixture.class))
        .map(UserFixture::user)
        .orElseThrow(() -> new ParameterResolutionException(
            "Пользователь не создан в BeforeEachCallback для параметра @User TestUser"
        ));
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Optional.ofNullable(store(context).remove(FIXTURE_KEY, UserFixture.class))
        .ifPresent(fixture -> Allure.step("Удалить пользователя из auth БД", fixture::close));
  }

  private static ExtensionContext.Store store(ExtensionContext context) {
    return context.getStore(NAMESPACE);
  }
}
