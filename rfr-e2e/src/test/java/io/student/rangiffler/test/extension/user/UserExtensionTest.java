package io.student.rangiffler.test.extension.user;

import io.qameta.allure.Allure;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import io.student.rangiffler.jupiter.probe.user.NoUserCase;
import io.student.rangiffler.jupiter.probe.user.OutcomeInvitationObservedCase;
import io.student.rangiffler.jupiter.probe.user.PlainUserCase;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("Plain @User: создаёт, резолвит и закрывает пользователя")
  void createsResolvesAndClosesPlainUser() {
    Events testEvents = execute(PlainUserCase.class).testEvents();

    Allure.step("Проба PlainUserCase успешна", () ->
        testEvents.assertStatistics(stats -> stats.started(1).succeeded(1))
    );
    Map<String, String> resolved = Allure.step("В отчёте есть event=resolved", () ->
        requireReported(testEvents, Map.of("event", "resolved"))
    );
    TestUser user = new TestUser(resolved.get("username"), "unused");
    Allure.step("После теста пользователя нет в auth БД", () ->
        assertFalse(AuthAccount.of(user).exists())
    );
  }

  @Test
  @DisplayName("Без @User фикстура не создаётся")
  void doesNotCreateFixtureWhenUserAnnotationAbsent() {
    Events testEvents = execute(NoUserCase.class).testEvents();

    Allure.step("Проба NoUserCase успешна", () ->
        testEvents.assertStatistics(stats -> stats.started(1).succeeded(1))
    );
    Allure.step("В отчёте есть event=no-user", () ->
        requireReported(testEvents, Map.of("event", "no-user"))
    );
  }

  @Test
  @DisplayName("Outcome invitation создаётся на время теста и чистится после")
  void createsOutcomeInvitationAndCleansItAfterTest() {
    Events testEvents = execute(OutcomeInvitationObservedCase.class).testEvents();

    Allure.step("Проба OutcomeInvitationObservedCase успешна", () ->
        testEvents.assertStatistics(stats -> stats.started(1).succeeded(1))
    );
    Map<String, String> observed = Allure.step("В отчёте есть event=outcome-observed", () ->
        requireReported(testEvents, Map.of("event", "outcome-observed"))
    );
    TestUser user = new TestUser(observed.get("username"), "unused");
    TestUser invitee = new TestUser(observed.get("invitee"), "unused");

    Allure.step("После теста нет PENDING friendship и профилей", () -> {
      assertFalse(Friendship.of(user, invitee, Friendship.PENDING).exists());
      assertFalse(Profile.of(invitee).exists());
      assertFalse(AuthAccount.of(user).exists());
    });
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
