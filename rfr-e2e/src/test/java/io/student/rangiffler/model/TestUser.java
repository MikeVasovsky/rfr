package io.student.rangiffler.model;

import java.util.List;

/**
 * Учётные данные тестового пользователя для UI-сценариев,
 * включая прекондишены друзей и входящих заявок.
 */
public record TestUser(
    String username,
    String password,
    List<TestUser> friends,
    List<TestUser> incomeInvitations
) {

  public TestUser {
    friends = List.copyOf(friends);
    incomeInvitations = List.copyOf(incomeInvitations);
  }

  public TestUser(String username, String password) {
    this(username, password, List.of(), List.of());
  }
}
