package io.student.rangiffler.data.fixture.session;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.model.UserSession;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Гостевая сессия: credentials сгенерированы, в БД не создаются.
 * После UI-register cleanup на {@link #close} удаляет строки, если они появились.
 */
public final class GuestSessionFixture extends AbstractSessionFixture {

  private TestUser guest;

  @Override
  public void start(BrowserRuntime runtime, Session annotation, TestConfig config) {
    if (!annotation.guest()) {
      throw new IllegalArgumentException("GuestSessionFixture требует @Session(guest = true)");
    }
    if (annotation.friends() > 0
        || annotation.incomeInvitations() > 0
        || annotation.outcomeInvitations() > 0) {
      throw new IllegalArgumentException(
          "У @Session(guest = true) нельзя задавать friends / incomeInvitations / outcomeInvitations"
      );
    }
    guest = RandomDataUtils.createUser();
    openBrowser(runtime, config);
    session = new UserSession(guest, page);
  }

  @Override
  protected void closeData() {
    if (guest == null) {
      return;
    }
    Friendships.of(guest).delete();
    Profile.of(guest).delete();
    AuthAccount.of(guest).delete();
  }
}
