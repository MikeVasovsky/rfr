package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Общие шаги фикстур: ensure api-профиль и создание related user.
 */
final class ApiUserSupport {

    private ApiUserSupport() {
    }

    record CreatedApiUser(Profile handle, TestProfile profile) {
        TestUser user() {
            return handle.user();
        }
    }

    static Profile ensureProfile(TestUser user) {
        Profile profile = Profile.of(user);
        if (profile.exists()) {
            return profile;
        }
        return Profile.create(user, RandomDataUtils.createProfile());
    }

    static CreatedApiUser createUserWithProfile() {
        TestUser user = RandomDataUtils.createUser();
        TestProfile profile = RandomDataUtils.createProfile();
        return new CreatedApiUser(Profile.create(user, profile), profile);
    }

    static TestUser createRelatedUser() {
        return createUserWithProfile().user();
    }
}
