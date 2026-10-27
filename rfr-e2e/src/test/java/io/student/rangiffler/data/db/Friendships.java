package io.student.rangiffler.data.db;

import java.util.Objects;

import static io.student.rangiffler.data.db.Databases.API;

/**
 * Все friendship-связи пользователя: deleteAll / подсчёты.
 */
public final class Friendships {

  private final String username;

  private Friendships(String username) {
    this.username = Objects.requireNonNull(username, "username");
  }

  public static Friendships of(String username) {
    return new Friendships(username);
  }

  public static Friendships of(Profile profile) {
    return new Friendships(profile.username());
  }

  public void deleteAll() {
    Profile.findId(username).ifPresent(id -> API.run(() -> {
      API.jdbc().sql(Sql.load("sql/api/delete_friends.sql")).params(id, id).update();
      API.jdbc().sql(Sql.load("sql/api/delete_invitations.sql")).params(id, id).update();
    }));
  }

  public int countAccepted() {
    return countByStatus("ACCEPTED");
  }

  public int countIncomePending() {
    return Profile.findId(username)
        .map(id -> API.jdbc().sql("""
                SELECT COUNT(*) AS cnt FROM friendship
                WHERE status = 'PENDING' AND addressee_id = ?
                """)
            .param(id)
            .query((rs, rowNum) -> rs.getInt("cnt"))
            .single())
        .orElse(0);
  }

  public int countAll() {
    return countAccepted() + countByStatus("PENDING");
  }

  private int countByStatus(String status) {
    return Profile.findId(username)
        .map(id -> API.jdbc().sql("""
                SELECT COUNT(*) AS cnt FROM friendship
                WHERE status = ? AND (requester_id = ? OR addressee_id = ?)
                """)
            .params(status, id, id)
            .query((rs, rowNum) -> rs.getInt("cnt"))
            .single())
        .orElse(0);
  }
}
