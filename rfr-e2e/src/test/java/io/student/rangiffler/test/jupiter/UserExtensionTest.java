package io.student.rangiffler.test.jupiter;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.jupiter.TestReport;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.extension.UserExtension;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

/**
 * EngineTestKit: lifecycle и report entries {@link UserExtension}.
 */
@Tag("fake")
public class UserExtensionTest {

  @Test
  @DisplayName("EngineTestKit: @User создаёт и чистит пользователя (report created/cleaned)")
  void shouldCreateAndCleanUserViaReportEntries() {
    TestReport report = TestReport.of(
        EngineTestKit.engine("junit-jupiter")
            .selectors(selectMethod(CreateUserCase.class, "test", TestUser.class))
            .execute()
    );

    report.assertTestsStarted(1)
        .assertTestsSucceeded(1)
        .assertHasReportEntryContaining(Map.of(
            "user", "created",
            "friends", "0",
            "incomeInvitations", "0"
        ))
        .assertHasReportEntryContaining(Map.of("user", "cleaned"));

    String username = report.requireCreatedUsername();
    assertFalse(AuthAccount.of(new TestUser(username, "x")).exists(), "auth user after cleaned");
  }

  @Test
  @DisplayName("EngineTestKit: @User(friends, income) попадает в report и модель")
  void shouldPublishFriendsAndIncomeInvitationCounts() {
    TestReport report = TestReport.of(
        EngineTestKit.engine("junit-jupiter")
            .selectors(selectMethod(FriendsAndIncomeCase.class, "test", TestUser.class))
            .execute()
    );

    report.assertTestsStarted(1)
        .assertTestsSucceeded(1)
        .assertHasReportEntryContaining(Map.of(
            "user", "created",
            "friends", "2",
            "incomeInvitations", "1"
        ))
        .assertHasReportEntry("user", "cleaned");

    String username = report.requireCreatedUsername();
    assertAll(
        () -> assertFalse(AuthAccount.of(new TestUser(username, "x")).exists(), "auth user after cleaned"),
        () -> assertEquals(0, Friendships.of(username).countAll(), "friendships after cleaned")
    );
  }

  @Test
  @DisplayName("EngineTestKit: без @User lifecycle report не публикуется")
  void shouldNotPublishUserLifecycleWithoutAnnotation() {
    TestReport report = TestReport.of(
        EngineTestKit.engine("junit-jupiter")
            .selectors(selectMethod(NoUserCase.class, "test"))
            .execute()
    );

    report.assertTestsStarted(1)
        .assertTestsSucceeded(1)
        .assertNoUserLifecycleEntries();
  }

  @ExtendWith(UserExtension.class)
  static class CreateUserCase {

    @Test
    void test(@User TestUser user) {
      assertTrue(AuthAccount.of(user).exists());
      assertEquals(0, user.friends().size());
      assertEquals(0, user.incomeInvitations().size());
    }
  }

  @ExtendWith(UserExtension.class)
  static class FriendsAndIncomeCase {

    @Test
    void test(@User(friends = 2, incomeInvitations = 1) TestUser user) {
      assertAll(
          () -> assertTrue(AuthAccount.of(user).exists()),
          () -> assertEquals(2, user.friends().size()),
          () -> assertEquals(1, user.incomeInvitations().size()),
          () -> assertEquals(2, Friendships.of(user.username()).countAccepted()),
          () -> assertEquals(1, Friendships.of(user.username()).countIncomePending())
      );
    }
  }

  @ExtendWith(UserExtension.class)
  static class NoUserCase {

    @Test
    void test() {
    }
  }
}
