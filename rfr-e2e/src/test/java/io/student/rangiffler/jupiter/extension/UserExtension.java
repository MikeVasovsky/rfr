package io.student.rangiffler.jupiter.extension;

import io.qameta.allure.Allure;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.Arrays;
import java.util.Optional;

/**
 * Жизненный цикл тестового пользователя в auth БД:
 * {@link BeforeEachCallback} создаёт {@link UserFixture},
 * {@link ParameterResolver} передаёт {@code @User TestUser},
 * {@link AfterEachCallback} закрывает fixture.
 */
public final class UserExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {

  /** Изолированное хранилище JUnit для данных этого extension в рамках теста. */
  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(UserExtension.class);
  /** Ключ, под которым в store лежит созданный {@link UserFixture}. */
  private static final String FIXTURE_KEY = "userFixture";

  /**
   * Перед тестом: если у метода есть параметр {@code @User TestUser},
   * создаёт пользователя в auth БД через {@link UserFixture#create()}
   * и кладёт fixture в store текущего теста.
   */
  @Override
  public void beforeEach(ExtensionContext context) {
    if (!requiresUser(context)) {
      return;
    }
    Allure.step("Создать пользователя в auth БД", () ->
        store(context).put(FIXTURE_KEY, UserFixture.create())
    );
  }

  /**
   * Сообщает JUnit, что этот resolver умеет внедрять параметр
   * с аннотацией {@link User} типа {@link TestUser}.
   */
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(User.class)
        && TestUser.class.isAssignableFrom(parameterContext.getParameter().getType());
  }

  /**
   * Достаёт из store созданный в {@link #beforeEach} fixture
   * и отдаёт тесту {@link TestUser} (логин/пароль и т.п.).
   * Если fixture нет — ошибка резолва параметра.
   */
  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return Optional.ofNullable(store(extensionContext).get(FIXTURE_KEY, UserFixture.class))
        .map(UserFixture::user)
        .orElseThrow(() -> new ParameterResolutionException(
            "Пользователь не создан в BeforeEachCallback для параметра @User TestUser"
        ));
  }

  /**
   * После теста: забирает fixture из store и вызывает {@link UserFixture#close()},
   * чтобы удалить пользователя из auth БД. Если fixture не создавали — ничего не делает.
   */
  @Override
  public void afterEach(ExtensionContext context) {
    Optional.ofNullable(store(context).remove(FIXTURE_KEY, UserFixture.class))
        .ifPresent(fixture -> Allure.step("Удалить пользователя из auth БД", fixture::close));
  }

  /** Store текущего теста в namespace этого extension (туда кладём/берём fixture). */
  private static ExtensionContext.Store store(ExtensionContext context) {
    return context.getStore(NAMESPACE);
  }

  /**
   * Нужен ли пользователю этот тест: есть ли среди параметров метода
   * хотя бы один {@code @User TestUser}. Без такого параметра before/after не трогают БД.
   */
  private static boolean requiresUser(ExtensionContext context) {
    return context.getTestMethod()
        .stream()
        .flatMap(method -> Arrays.stream(method.getParameters()))
        .anyMatch(parameter -> parameter.isAnnotationPresent(User.class)
            && TestUser.class.isAssignableFrom(parameter.getType()));
  }
}
