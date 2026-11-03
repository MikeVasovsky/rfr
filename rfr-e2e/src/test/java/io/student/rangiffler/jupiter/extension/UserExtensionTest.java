package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

/**
 * Проверяет контракт {@link UserExtension} через Jupiter EngineTestKit:
 * создание fixture по {@code @User}, inject {@link TestUser}, close после теста.
 * События резолва фиксируются через {@link TestReporter}.
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
  void passesFriendsCountFromAnnotationIntoTestUser() {
    installRecordingFactory();

    Events testEvents = execute(FriendsUserCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(1));
    assertReported(testEvents,
        Map.of(
            "event", "resolved",
            "friends", "2",
            "incomeInvitations", "0"
        )
    );
  }

  @Test
  void passesIncomeInvitationsFromAnnotationIntoTestUser() {
    installRecordingFactory();

    Events testEvents = execute(IncomeInvitationsUserCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(1));
    assertReported(testEvents,
        Map.of(
            "event", "resolved",
            "friends", "0",
            "incomeInvitations", "3"
        )
    );
  }

  @Test
  void passesFriendsAndIncomeInvitationsTogether() {
    installRecordingFactory();

    Events testEvents = execute(FriendsAndInvitationsUserCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).succeeded(1));
    assertReported(testEvents,
        Map.of(
            "event", "resolved",
            "friends", "1",
            "incomeInvitations", "2"
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
  void failsWhenUserParameterCannotBeResolvedWithoutBeforeEachFixture() {
    UserExtension.installFixtureFactory(annotation -> {
      throw new IllegalStateException("factory must not be called");
    });

    Events testEvents = execute(BrokenSupportsOnlyCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).failed(1));
    testEvents.assertThatEvents().haveExactly(1,
        event(test(), finishedWithFailure(
            instanceOf(org.junit.jupiter.api.extension.ParameterResolutionException.class),
            message(m -> m.contains("Пользователь не создан в BeforeEachCallback"))
        ))
    );
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
    List<Map<String, String>> published = new ArrayList<>();
    for (Event event : testEvents.reportingEntryPublished().list()) {
      ReportEntry entry = event.getRequiredPayload(ReportEntry.class);
      published.add(entry.getKeyValuePairs());
    }
    assertTrue(
        published.stream().anyMatch(actual -> actual.entrySet().containsAll(expected.entrySet())),
        () -> "expected reporting entry containing " + expected + " but was " + published
    );
  }

  private static void publishUser(TestReporter reporter, TestUser user) {
    reporter.publishEntry(Map.of(
        "event", "resolved",
        "username", user.username(),
        "friends", String.valueOf(user.friends().size()),
        "incomeInvitations", String.valueOf(user.incomeInvitations().size()),
        "outcomeInvitations", String.valueOf(user.outcomeInvitations().size())
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

  @ExtendWith(UserExtension.class)
  static class PlainUserCase {
    @Test
    void resolves(@User TestUser user, TestReporter reporter) {
      publishUser(reporter, user);
    }
  }

  @ExtendWith(UserExtension.class)
  static class FriendsUserCase {
    @Test
    void resolves(@User(friends = 2) TestUser user, TestReporter reporter) {
      publishUser(reporter, user);
    }
  }

  @ExtendWith(UserExtension.class)
  static class IncomeInvitationsUserCase {
    @Test
    void resolves(@User(incomeInvitations = 3) TestUser user, TestReporter reporter) {
      publishUser(reporter, user);
    }
  }

  @ExtendWith(UserExtension.class)
  static class FriendsAndInvitationsUserCase {
    @Test
    void resolves(@User(friends = 1, incomeInvitations = 2) TestUser user, TestReporter reporter) {
      publishUser(reporter, user);
    }
  }

  @ExtendWith(UserExtension.class)
  static class NoUserCase {
    @Test
    void runsWithoutUser(TestReporter reporter) {
      reporter.publishEntry("event", "no-user");
    }
  }

  /** beforeEach не создаёт fixture (нет {@code @User}); резолв TestUser делегируется в UserExtension.store. */
  @ExtendWith({UserExtension.class, ForceUserResolveExtension.class})
  static class BrokenSupportsOnlyCase {
    @Test
    void cannotResolve(TestUser user) {
      throw new AssertionError("must not run: " + user);
    }
  }

  static final class ForceUserResolveExtension implements org.junit.jupiter.api.extension.ParameterResolver {
    private final UserExtension delegate = new UserExtension();

    @Override
    public boolean supportsParameter(
        org.junit.jupiter.api.extension.ParameterContext parameterContext,
        org.junit.jupiter.api.extension.ExtensionContext extensionContext
    ) {
      return TestUser.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(
        org.junit.jupiter.api.extension.ParameterContext parameterContext,
        org.junit.jupiter.api.extension.ExtensionContext extensionContext
    ) {
      return delegate.resolveParameter(parameterContext, extensionContext);
    }
  }
}
