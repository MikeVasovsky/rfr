package io.student.rangiffler.utils;

import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import net.datafaker.Faker;

import java.util.List;

/**
 * Утилиты генерации тестовых данных.
 */
public final class RandomDataUtils {

  private static final Faker FAKER = new Faker();

  private static final List<String> COUNTRIES = List.of(
      "France",
      "China",
      "Latvia",
      "Argentina",
      "Canada"
  );

  private RandomDataUtils() {
  }

  public static TestUser createUser() {
    String username = FAKER.regexify("[a-zA-Z]{6,10}");
    String password = FAKER.internet().password(6, 10, true, false, true);
    return new TestUser(username, password);
  }

  public static TestProfile createProfile() {
    String firstname = FAKER.name().firstName();
    String surname = FAKER.name().lastName();
    String country = COUNTRIES.get(FAKER.random().nextInt(COUNTRIES.size()));
    return new TestProfile(firstname, surname, country);
  }
}
