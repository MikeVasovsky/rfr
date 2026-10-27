package io.student.rangiffler.test.fake;

import io.qameta.allure.Allure;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.extension.UserExtension;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * SQL-smoke: прекондишены {@code friends}/{@code incomeInvitations} и cleanup ручной связи после store close.
 */
@Tag("fake")
@ExtendWith({FriendshipFixtureSmokeTest.AssertAfterStoreClose.class, UserExtension.class})
public class FriendshipFixtureSmokeTest {

  private static String usernameForCleanupAssert;
  private static boolean assertCleanup;

  @Test
  @DisplayName("Фикстуры создают 2 ACCEPTED и 1 PENDING income")
  void shouldCreateFriendsAndIncomeInvitations(@User(friends = 2, incomeInvitations = 1) TestUser user) {
    Friendships friendships = Friendships.of(user.username());

    Allure.step("Проверить SQL-прекондишены friendship", () ->
        assertAll(
            () -> assertEquals(2, friendships.countAccepted(), "ACCEPTED friends"),
            () -> assertEquals(1, friendships.countIncomePending(), "PENDING income invitations")
        )
    );
  }

  @Test
  @DisplayName("Ручная ACCEPTED-связь удаляется при close store")
  void shouldDeleteManualFriendshipWhenStoreCloses(@User TestUser user) {
    Profile main = Profile.create(user.username());
    TestUser companion = RandomDataUtils.createUser();
    AuthAccount.create(companion);
    Profile friend = Profile.create(companion.username());
    Friendship.accepted(main, friend);

    Allure.step("До close: одна ACCEPTED-связь", () ->
        assertEquals(1, Friendships.of(main).countAccepted(), "manual ACCEPTED before close")
    );

    usernameForCleanupAssert = user.username();
    assertCleanup = true;
  }

  /**
   * После {@link UserExtension#afterEach}: в store уже закрыли fixture — проверяем, что связей не осталось.
   */
  static final class AssertAfterStoreClose implements AfterEachCallback {
    @Override
    public void afterEach(ExtensionContext context) {
      if (!assertCleanup) {
        return;
      }
      try {
        Allure.step("После store close: friendship count = 0", () ->
            assertEquals(0, Friendships.of(usernameForCleanupAssert).countAll(), "friendships after store close")
        );
      } finally {
        assertCleanup = false;
        usernameForCleanupAssert = null;
      }
    }
  }
}
