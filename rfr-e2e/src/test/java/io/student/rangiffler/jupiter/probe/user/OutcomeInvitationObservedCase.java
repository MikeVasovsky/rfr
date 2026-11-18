package io.student.rangiffler.jupiter.probe.user;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Мишень Observed: во время теста есть Friendship PENDING и профиль invitee.
 */
@ProbeTarget
@ExtendWith(UserExtension.class)
public class OutcomeInvitationObservedCase {

  @Test
  void observesPendingFriendship(@User(outcomeInvitations = 1) TestUser user, TestReporter reporter) {
    TestUser invitee = user.outcomeInvitations().get(0);
    assertTrue(Friendship.of(user, invitee, Friendship.PENDING).exists());
    assertTrue(Profile.of(invitee).exists());
    reporter.publishEntry(Map.of(
        "event", "outcome-observed",
        "username", user.username(),
        "invitee", invitee.username()
    ));
  }
}
