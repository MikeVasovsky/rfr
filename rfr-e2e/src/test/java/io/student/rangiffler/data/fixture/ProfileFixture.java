package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

public final class ProfileFixture implements AutoCloseable {

    private final ApiProfile apiProfile;
    private boolean closed;
    private final TestProfile profile;

    private ProfileFixture(ApiProfile apiProfile,
                           TestProfile profile) {
        this.apiProfile = apiProfile;
        this.profile = profile;
    }

    public static ProfileFixture create() {
        TestUser user = RandomDataUtils.createUser();
        TestProfile profile = RandomDataUtils.createProfile();
        return new ProfileFixture(ApiProfile.create(user, profile), profile);
    }

    public TestProfile profile(){
        return profile;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        apiProfile.delete();
        closed = true;
    }
}
