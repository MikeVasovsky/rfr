package io.student.rangiffler.model;

import java.util.List;

/**
 * Учётные данные тестового пользователя и связанные api-пользователи из фикстур.
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

  public TestUser withFriends(List<TestUser> friends) {
    return new TestUser(username, password, friends, incomeInvitations);
  }

  public TestUser withIncomeInvitations(List<TestUser> incomeInvitations) {
    return new TestUser(username, password, friends, incomeInvitations);
  }
}
