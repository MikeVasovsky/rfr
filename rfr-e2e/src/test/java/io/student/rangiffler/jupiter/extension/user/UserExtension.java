package io.student.rangiffler.jupiter.extension.user;

import io.qameta.allure.Allure;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.data.fixture.user.UserRequest;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;

/**
 * Жизненный цикл тестовых пользователей: создаёт фикстуру на каждый параметр с {@link User},
 * отдаёт {@code @User TestUser} и делится пользователем с другими extension через {@link #user}.
 * Фикстура кладётся в Store до {@link UserFixture#create()}, чтобы {@code close()} сработал при падении подготовки.
 */
public final class UserExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(UserExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (int index = 0; index < parameters.length; index++) {
      User user = parameters[index].getAnnotation(User.class);
      if (user != null) {
        int parameterIndex = index;
        Allure.step("Создать пользователя в auth БД [param=" + parameterIndex + "]", () -> {
          UserFixture fixture = UserRequest.from(user).fixture();
          store(context).put(key(parameterIndex), fixture);
          fixture.create();
        });
      }
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(User.class)
        && TestUser.class.isAssignableFrom(parameterContext.getParameter().getType())
        && extensionContext.getTestMethod()
        .filter(parameterContext.getDeclaringExecutable()::equals)
        .isPresent();
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return user(extensionContext, parameterContext.getIndex());
  }

  /**
   * Пользователь, созданный для параметра с индексом {@code parameterIndex}
   * (в т.ч. для {@code @Session @User UserSession}).
   */
  public static TestUser user(ExtensionContext context, int parameterIndex) {
    UserFixture fixture = store(context).get(key(parameterIndex), UserFixture.class);
    if (fixture == null) {
      throw new ParameterResolutionException(
          "No user was created for parameter " + parameterIndex
              + " of " + context.getRequiredTestMethod()
      );
    }
    return fixture.user();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (int index = 0; index < parameters.length; index++) {
      if (parameters[index].getAnnotation(User.class) == null) {
        continue;
      }
      UserFixture fixture = store(context).remove(key(index), UserFixture.class);
      if (fixture != null) {
        int parameterIndex = index;
        Allure.step("Удалить пользователя из auth БД [param=" + parameterIndex + "]", fixture::close);
      }
    }
  }

  private static String key(int index) {
    return "user-" + index;
  }

  private static ExtensionContext.Store store(ExtensionContext context) {
    return context.getStore(NAMESPACE);
  }
}
