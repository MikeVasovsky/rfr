package io.student.rangiffler.data.fixture.user;

import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.data.fixture.TearDown;
import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Владелец api-профиля: {@link #ensureFor} для уже существующего пользователя,
 * {@link #createRelated} — новый партнёр с профилем; {@link #close()} удаляет только свой профиль.
 */
public final class ProfileFixture implements AutoCloseable {

  private final Profile apiProfile;
  private final TearDown tearDown;

  private ProfileFixture(Profile apiProfile, TearDown tearDown) {
    this.apiProfile = apiProfile;
    this.tearDown = tearDown;
  }

  public static Profile ensureFor(TestUser user) {
    Profile profile = Profile.of(user);
    if (profile.exists()) {
      return profile;
    }
    return Profile.create(user, RandomDataUtils.createProfile());
  }

  public static ProfileFixture createRelated() {
    TestUser user = RandomDataUtils.createUser();
    TestProfile profile = RandomDataUtils.createProfile();
    Profile handle = Profile.create(user, profile);
    return new ProfileFixture(handle, new TearDown().with(handle::delete));
  }

  public TestUser user() {
    return apiProfile.user();
  }

  @Override
  public void close() {
    tearDown.close();
  }
}
