package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;
import org.springframework.jdbc.core.simple.JdbcClient;

import static io.student.rangiffler.data.db.ApiFriendship.deleteFriends;
import static io.student.rangiffler.data.db.ApiFriendship.deleteInvitations;
import static io.student.rangiffler.data.db.Databases.API;

/**
 * Тестовый пользователь в auth БД: {@link #create()} создаёт запись, {@link #close()} удаляет.
 */
public final class SingleUserFixture implements UserFixture {

    private final AuthAccount account;
    private final TearDown tearDown;

    private SingleUserFixture(AuthAccount account) {
        this.account = account;
        this.tearDown = new TearDown()
            .with(() -> {
                JdbcClient jdbc = API.jdbcClient();
                byte[] id = ApiProfile.findUserId(jdbc, account.user());
                if (id != null) {
                    deleteFriends(id);
                    deleteInvitations(id);
                    ApiProfile.of(account.user()).delete();
                }
            })
            .with(account::delete);
    }

    public static UserFixture create() {
        return new SingleUserFixture(AuthAccount.create(RandomDataUtils.createUser()));
    }

    public TestUser user() {
        return account.user();
    }

    @Override
    public void close() {
        tearDown.close();
    }
}
