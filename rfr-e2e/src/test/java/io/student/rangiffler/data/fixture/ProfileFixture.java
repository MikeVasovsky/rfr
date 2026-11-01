package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Тестовый профиль в api БД: {@link #create()} создаёт запись, {@link #close()} удаляет.
 */
public final class ProfileFixture implements AutoCloseable {

    private final TestProfile profile;
    private final TearDown tearDown;

    private ProfileFixture(ApiProfile apiProfile, TestProfile profile) {
        this.profile = profile;
        this.tearDown = new TearDown().with(apiProfile::delete);
    }

    public static ProfileFixture create() {
        TestUser user = RandomDataUtils.createUser();
        TestProfile profile = RandomDataUtils.createProfile();
        return new ProfileFixture(ApiProfile.create(user, profile), profile);
    }

    public TestProfile profile() {
        return profile;
    }

    @Override
    public void close() {
        tearDown.close();
    }
}
