package io.student.rangiffler.test.extension.user;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import io.student.rangiffler.jupiter.probe.user.NoUserCase;
import io.student.rangiffler.jupiter.probe.user.OutcomeInvitationObservedCase;
import io.student.rangiffler.jupiter.probe.user.PlainUserCase;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Оркестратор EngineTestKit для {@link UserExtension}: мишени в {@code jupiter.probe.user}.
 */
@Tag("fake")
class UserExtensionTest {

  @Test
  void createsResolvesAndClosesPlainUser() {
    Events testEvents = execute(PlainUserCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(1));
    Map<String, String> resolved = requireReported(testEvents, Map.of("event", "resolved"));
    TestUser user = new TestUser(resolved.get("username"), "unused");
    assertFalse(AuthAccount.of(user).exists());
  }

  @Test
  void doesNotCreateFixtureWhenUserAnnotationAbsent() {
    Events testEvents = execute(NoUserCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(1));
    requireReported(testEvents, Map.of("event", "no-user"));
  }

  @Test
  void createsOutcomeInvitationAndCleansItAfterTest() {
    Events testEvents = execute(OutcomeInvitationObservedCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(1));
    Map<String, String> observed = requireReported(testEvents, Map.of("event", "outcome-observed"));
    TestUser user = new TestUser(observed.get("username"), "unused");
    TestUser invitee = new TestUser(observed.get("invitee"), "unused");

    assertFalse(Friendship.of(user, invitee, Friendship.PENDING).exists());
    assertFalse(Profile.of(invitee).exists());
    assertFalse(AuthAccount.of(user).exists());
  }

  private static org.junit.platform.testkit.engine.EngineExecutionResults execute(Class<?> testClass) {
    return EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(testClass))
        .execute();
  }

  private static Map<String, String> requireReported(Events testEvents, Map<String, String> expected) {
    List<Map<String, String>> published = new ArrayList<>();
    for (Event event : testEvents.reportingEntryPublished().list()) {
      ReportEntry entry = event.getRequiredPayload(ReportEntry.class);
      published.add(entry.getKeyValuePairs());
    }
    return published.stream()
        .filter(actual -> actual.entrySet().containsAll(expected.entrySet()))
        .findFirst()
        .orElseThrow(() -> new AssertionError(
            "expected reporting entry containing " + expected + " but was " + published
        ));
  }
}
