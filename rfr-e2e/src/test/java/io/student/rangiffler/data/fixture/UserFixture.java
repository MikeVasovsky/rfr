package io.student.rangiffler.data.fixture;

import io.student.rangiffler.jupiter.UserRequest;
import io.student.rangiffler.model.TestUser;

/**
 * Фикстура тестового пользователя: доступ к {@link UserRequest}/{@link TestUser} и cleanup.
 */
public interface UserFixture extends AutoCloseable {

  UserRequest request();

  TestUser user();

  @Override
  void close();
}
