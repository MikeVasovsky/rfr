package io.student.rangiffler.utils;

import io.student.rangiffler.model.TestUser;
import net.datafaker.Faker;

/**
 * Утилиты генерации тестовых данных.
 */
public final class RandomDataUtils {

  private static final Faker FAKER = new Faker();

  private RandomDataUtils() {
  }

  public static TestUser createUser() {
    String username = FAKER.regexify("[a-zA-Z]{6,10}");
    String password = FAKER.internet().password(6, 10, true, false, true);
    return new TestUser(username, password);
  }
}
