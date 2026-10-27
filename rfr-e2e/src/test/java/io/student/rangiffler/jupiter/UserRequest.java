package io.student.rangiffler.jupiter;

import io.student.rangiffler.jupiter.annotation.User;

/**
 * Запрос на тестового пользователя: поля из {@link User} (без создания в БД).
 */
public record UserRequest(int friends, int incomeInvitations) {

  public UserRequest {
    if (friends < 0) {
      throw new IllegalArgumentException("friends must be >= 0: " + friends);
    }
    if (incomeInvitations < 0) {
      throw new IllegalArgumentException("incomeInvitations must be >= 0: " + incomeInvitations);
    }
  }

  public static UserRequest from(User annotation) {
    return new UserRequest(annotation.friends(), annotation.incomeInvitations());
  }
}
