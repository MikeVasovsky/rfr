package io.student.rangiffler.data.db;

import io.student.rangiffler.model.TestUser;

import static io.student.rangiffler.data.db.Databases.API;

/**
 * Связи пользователя в {@code rangiffler-api.friendship}: exists / delete по статусам.
 */
public final class Friendships {

  private final TestUser user;

  private Friendships(TestUser user) {
    this.user = user;
  }

  public static Friendships of(TestUser user) {
    return new Friendships(user);
  }

  public TestUser user() {
    return user;
  }

  public boolean exists() {
    byte[] userId = Profile.of(user).id();
    if (userId == null) {
      return false;
    }
    return API.jdbcClient()
        .sql(Sql.load("sql/api/find_any_friendship.sql"))
        .params(userId, userId)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional()
        .isPresent();
  }

  public void deleteFriends() {
    byte[] userId = Profile.of(user).id();
    if (userId == null) {
      return;
    }
    API.run(() ->
        API.jdbcClient()
            .sql(Sql.load("sql/api/delete_friends.sql"))
            .params(userId, userId)
            .update()
    );
  }

  public void deleteInvitations() {
    byte[] userId = Profile.of(user).id();
    if (userId == null) {
      return;
    }
    API.run(() ->
        API.jdbcClient()
            .sql(Sql.load("sql/api/delete_invitations.sql"))
            .params(userId, userId)
            .update()
    );
  }

  public void delete() {
    deleteFriends();
    deleteInvitations();
  }
}
