package io.student.rangiffler.data.fixture.user;

import io.student.rangiffler.data.fixture.TearDown;

import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestProfile;

/**
 * Тестовый профиль в api БД: {@link #create()} создаёт запись, {@link #close()} удаляет.
 */
public final class ProfileFixture implements AutoCloseable {

  private final TestProfile profile;
  private final TearDown tearDown;

  private ProfileFixture(Profile apiProfile, TestProfile profile) {
    this.profile = profile;
    this.tearDown = new TearDown().with(apiProfile::delete);
  }

  public static ProfileFixture create() {
    ApiUserSupport.CreatedApiUser created = ApiUserSupport.createUserWithProfile();
    return new ProfileFixture(created.handle(), created.profile());
  }

  public TestProfile profile() {
    return profile;
  }

  @Override
  public void close() {
    tearDown.close();
  }
}
