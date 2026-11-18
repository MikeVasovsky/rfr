package io.student.rangiffler.model;

import java.util.List;

/**
 * Учётные данные тестового пользователя и связанные api-пользователи из фикстур.
 */
public record TestUser(
        String username,
        String password,
        List<TestUser> friends,
        List<TestUser> incomeInvitations,
        List<TestUser> outcomeInvitations
) {

    public TestUser {
        friends = List.copyOf(friends);
        incomeInvitations = List.copyOf(incomeInvitations);
        outcomeInvitations = List.copyOf(outcomeInvitations);
    }

    public TestUser(String username, String password) {
        this(username, password, List.of(), List.of(), List.of());
    }

    public TestUser withFriends(List<TestUser> friends) {
        return new TestUser(username, password, friends, incomeInvitations, outcomeInvitations);
    }

    public TestUser withIncomeInvitations(List<TestUser> incomeInvitations) {
        return new TestUser(username, password, friends, incomeInvitations, outcomeInvitations);
    }

    public TestUser withOutcomeInvitations(List<TestUser> outcomeInvitations) {
        return new TestUser(username, password, friends, incomeInvitations, outcomeInvitations);
    }
}
