package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestUser;

/**
 * Тестовый профиль в api БД: {@link #create(TestUser)} создаёт запись,
 * {@link #close()} удаляет её.
 */
public final class ProfileFixture implements AutoCloseable {

  private final Profile profile;
  private final TearDown tearDown;

  private ProfileFixture(Profile profile) {
    this.profile = profile;
    this.tearDown = new TearDown().with(profile::delete);
  }

  /**
   * Пишет минимальный профиль (country {@code ru}) в api БД
   * для username из {@link TestUser} и возвращает fixture для cleanup.
   */
  public static ProfileFixture create(TestUser user) {
    return new ProfileFixture(Profile.create(user.username()));
  }

  public String username() {
    return profile.username();
  }

  @Override
  public void close() {
    tearDown.close();
  }
}
