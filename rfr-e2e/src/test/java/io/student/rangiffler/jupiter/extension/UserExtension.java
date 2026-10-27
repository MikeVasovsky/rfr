package io.student.rangiffler.jupiter.extension;

import io.qameta.allure.Allure;
import io.student.rangiffler.data.fixture.SingleUserFixture;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.jupiter.UserRequest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

/**
 * Жизненный цикл тестового пользователя в auth БД:
 * {@link BeforeEachCallback} собирает {@link UserRequest} из {@link User} и создаёт {@link UserFixture},
 * {@link ParameterResolver} передаёт {@code @User TestUser},
 * {@link AfterEachCallback} закрывает fixture.
 */
public final class UserExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(UserExtension.class);
  private static final String FIXTURE_KEY = "userFixture";

  /**
   * Перед тестом: если у метода есть параметр {@code @User TestUser},
   * строит {@link UserRequest} из аннотации, создаёт fixture и кладёт в store.
   */
  @Override
  public void beforeEach(ExtensionContext context) {
    findUserAnnotation(context).ifPresent(annotation ->
        Allure.step("Создать пользователя в auth БД", () ->
            store(context).put(FIXTURE_KEY, SingleUserFixture.create(UserRequest.from(annotation)))
        )
    );
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(User.class)
        && TestUser.class.isAssignableFrom(parameterContext.getParameter().getType());
  }

  /**
   * Достаёт из store созданный в {@link #beforeEach} fixture
   * и отдаёт тесту {@link TestUser}. Если fixture нет — ошибка резолва параметра.
   */
  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    UserFixture value = store(extensionContext).get(FIXTURE_KEY, UserFixture.class);
    if (value == null) {
      throw new ParameterResolutionException(
          "Пользователь не создан в BeforeEachCallback для параметра @User TestUser"
      );
    }
    TestUser user = value.user();
    if (user == null) {
      throw new ParameterResolutionException(
          "Пользователь не создан в BeforeEachCallback для параметра @User TestUser"
      );
    }
    return user;
  }

  /**
   * После теста: забирает fixture из store и вызывает {@link UserFixture#close()}.
   * Если fixture не создавали — ничего не делает.
   */
  @Override
  public void afterEach(ExtensionContext context) {
    Optional.ofNullable(store(context).remove(FIXTURE_KEY, UserFixture.class))
        .ifPresent(fixture -> Allure.step("Удалить пользователя из auth БД", fixture::close));
  }

  private static ExtensionContext.Store store(ExtensionContext context) {
    return context.getStore(NAMESPACE);
  }

  private static Optional<User> findUserAnnotation(ExtensionContext context) {
    return context.getTestMethod()
        .stream()
        .flatMap(method -> Arrays.stream(method.getParameters()))
        .filter(UserExtension::isUserTestUserParameter)
        .map(parameter -> parameter.getAnnotation(User.class))
        .findFirst();
  }

  private static boolean isUserTestUserParameter(Parameter parameter) {
    return parameter.isAnnotationPresent(User.class)
        && TestUser.class.isAssignableFrom(parameter.getType());
  }
}
