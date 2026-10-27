package io.student.rangiffler.data.db;

import java.util.Objects;

import static io.student.rangiffler.data.db.Databases.API;

/**
 * Одна строка {@code friendship}: ACCEPTED или PENDING (income invitation).
 */
public final class Friendship {

  private final Profile requester;
  private final Profile addressee;
  private final String status;

  private Friendship(Profile requester, Profile addressee, String status) {
    this.requester = Objects.requireNonNull(requester, "requester");
    this.addressee = Objects.requireNonNull(addressee, "addressee");
    this.status = Objects.requireNonNull(status, "status");
  }

  public static Friendship accepted(Profile requester, Profile addressee) {
    insert(requester, addressee, "sql/api/insert_friendship.sql");
    return new Friendship(requester, addressee, "ACCEPTED");
  }

  public static Friendship invitation(Profile requester, Profile addressee) {
    insert(requester, addressee, "sql/api/insert_invitation.sql");
    return new Friendship(requester, addressee, "PENDING");
  }

  public Profile requester() {
    return requester;
  }

  public Profile addressee() {
    return addressee;
  }

  public String status() {
    return status;
  }

  public boolean exists() {
    byte[] requesterId = requester.id();
    byte[] addresseeId = addressee.id();
    Integer count = API.jdbc().sql("""
            SELECT COUNT(*) AS cnt FROM friendship
            WHERE requester_id = ? AND addressee_id = ? AND status = ?
            """)
        .params(requesterId, addresseeId, status)
        .query((rs, rowNum) -> rs.getInt("cnt"))
        .single();
    return count != null && count > 0;
  }

  private static void insert(Profile requester, Profile addressee, String sqlClasspath) {
    API.run(() -> API.jdbc().sql(Sql.load(sqlClasspath))
        .params(requester.id(), addressee.id())
        .update());
  }
}
