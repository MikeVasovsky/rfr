package io.student.rangiffler.data.db;

import io.student.rangiffler.model.TestUser;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static io.student.rangiffler.data.db.Databases.API;

/**
 * Одна связь в {@code rangiffler-api.friendship}: create / exists / delete.
 */
public final class Friendship {

  public static final String ACCEPTED = "ACCEPTED";
  public static final String PENDING = "PENDING";

  private final TestUser requester;
  private final TestUser addressee;
  private final String status;

  private Friendship(TestUser requester, TestUser addressee, String status) {
    this.requester = requester;
    this.addressee = addressee;
    this.status = status;
  }

  public static Friendship create(TestUser requester, TestUser addressee, String status) {
    byte[] requesterId = Profile.of(requester).requireId();
    byte[] addresseeId = Profile.of(addressee).requireId();
    LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    LocalDateTime respondedAt = ACCEPTED.equals(status) ? createdAt : null;
    API.run(() -> {
      JdbcClient jdbc = API.jdbcClient();
      jdbc.sql(Sql.load("sql/api/create_friendship.sql"))
          .params(uuidBytes(UUID.randomUUID()), requesterId, addresseeId, status, createdAt, respondedAt)
          .update();
    });
    return new Friendship(requester, addressee, status);
  }

  public static Friendship of(TestUser requester, TestUser addressee, String status) {
    return new Friendship(requester, addressee, status);
  }

  public TestUser requester() {
    return requester;
  }

  public TestUser addressee() {
    return addressee;
  }

  public String status() {
    return status;
  }

  public boolean exists() {
    byte[] requesterId = Profile.of(requester).id();
    byte[] addresseeId = Profile.of(addressee).id();
    if (requesterId == null || addresseeId == null) {
      return false;
    }
    return API.jdbcClient()
        .sql(Sql.load("sql/api/find_friendship.sql"))
        .params(requesterId, addresseeId)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional()
        .isPresent();
  }

  public void delete() {
    byte[] requesterId = Profile.of(requester).id();
    byte[] addresseeId = Profile.of(addressee).id();
    if (requesterId == null || addresseeId == null) {
      return;
    }
    API.run(() ->
        API.jdbcClient()
            .sql(Sql.load("sql/api/delete_friendship.sql"))
            .params(requesterId, addresseeId)
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
