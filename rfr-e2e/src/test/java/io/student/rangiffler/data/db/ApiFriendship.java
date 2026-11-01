package io.student.rangiffler.data.db;

import static io.student.rangiffler.data.db.Databases.API;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Связь в {@code rangiffler-api.friendship}: create одной пары.
 */
public final class ApiFriendship {

  private ApiFriendship() {
  }

  public static void create(byte[] requesterId, byte[] addresseeId, String status) {
    LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    LocalDateTime respondedAt = "ACCEPTED".equals(status) ? createdAt : null;
    API.run(() -> {
      JdbcClient jdbc = API.jdbcClient();
      jdbc.sql(Sql.load("sql/api/create_friendship.sql"))
          .params(uuidBytes(UUID.randomUUID()), requesterId, addresseeId, status, createdAt, respondedAt)
          .update();
    });
  }

  public static void deleteFriends(byte[] userId) {
    API.run(() ->
        API.jdbcClient()
            .sql(Sql.load("sql/api/delete_friends.sql"))
            .params(userId, userId)
            .update()
    );
  }

  public static void deleteInvitations(byte[] userId) {
    API.run(() ->
        API.jdbcClient()
            .sql(Sql.load("sql/api/delete_invitations.sql"))
            .params(userId, userId)
            .update()
    );
  }

  private static byte[] uuidBytes(UUID id) {
    return ByteBuffer.allocate(16)
        .putLong(id.getMostSignificantBits())
        .putLong(id.getLeastSignificantBits())
        .array();
  }
}
