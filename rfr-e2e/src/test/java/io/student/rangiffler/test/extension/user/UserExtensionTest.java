package io.student.rangiffler.test.extension.user;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import io.student.rangiffler.jupiter.probe.user.NoUserCase;
import io.student.rangiffler.jupiter.probe.user.OutcomeInvitationObservedCase;
import io.student.rangiffler.jupiter.probe.user.PlainUserCase;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Оркестратор EngineTestKit для {@link UserExtension}: мишени в {@code jupiter.probe.user}.
 */
@Tag("fake")
class UserExtensionTest {

  private final AtomicInteger creations = new AtomicInteger();
  private final AtomicBoolean closed = new AtomicBoolean();

  @AfterEach
  void resetExtension() {
    UserExtension.resetFixtureFactory();
    creations.set(0);
    closed.set(false);
  }

  @Test
  void createsResolvesAndClosesPlainUser() {
    installRecordingFactory();

    Events testEvents = execute(PlainUserCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(1));
    assertEquals(1, creations.get());
    assertTrue(closed.get());
    assertReported(testEvents,
        Map.of(
            "event", "resolved",
            "username", "main",
            "friends", "0",
            "incomeInvitations", "0"
        )
    );
  }

  @Test
  void doesNotCreateFixtureWhenUserAnnotationAbsent() {
    installRecordingFactory();

    Events testEvents = execute(NoUserCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(1));
    assertEquals(0, creations.get());
    assertFalse(closed.get());
    assertReported(testEvents, Map.of("event", "no-user"));
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
  }

  private void installRecordingFactory() {
    UserExtension.installFixtureFactory(annotation -> {
      creations.incrementAndGet();
      List<TestUser> friends = IntStream.range(0, annotation.friends())
          .mapToObj(i -> new TestUser("friend-" + i, "pass"))
          .toList();
      List<TestUser> incomeInvitations = IntStream.range(0, annotation.incomeInvitations())
          .mapToObj(i -> new TestUser("inviter-" + i, "pass"))
          .toList();
      List<TestUser> outcomeInvitations = IntStream.range(0, annotation.outcomeInvitations())
          .mapToObj(i -> new TestUser("invitee-" + i, "pass"))
          .toList();
      TestUser user = new TestUser("main", "secret", friends, incomeInvitations, outcomeInvitations);
      return new RecordingUserFixture(user, closed);
    });
  }

  private static org.junit.platform.testkit.engine.EngineExecutionResults execute(Class<?> testClass) {
    return EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(testClass))
        .execute();
  }

  private static void assertReported(Events testEvents, Map<String, String> expected) {
    requireReported(testEvents, expected);
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

  private static final class RecordingUserFixture implements UserFixture {
    private final TestUser user;
    private final AtomicBoolean closed;

    private RecordingUserFixture(TestUser user, AtomicBoolean closed) {
      this.user = user;
      this.closed = closed;
    }

    @Override
    public TestUser user() {
      return user;
    }

    @Override
    public void close() {
      closed.set(true);
    }
  }
}
