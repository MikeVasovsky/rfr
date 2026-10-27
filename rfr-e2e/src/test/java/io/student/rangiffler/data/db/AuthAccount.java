package io.student.rangiffler.data.db;

import io.student.rangiffler.model.TestUser;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;

import static io.student.rangiffler.data.db.Databases.AUTH;

/**
 * Учётка в {@code rangiffler-auth}: create / delete / exists.
 */
public final class AuthAccount {

  private static final PasswordEncoder PASSWORD_ENCODER =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();

  private final TestUser user;

  private AuthAccount(TestUser user) {
    this.user = Objects.requireNonNull(user, "user");
  }

  public static AuthAccount create(TestUser user) {
    Objects.requireNonNull(user, "user");
    AUTH.run(() -> {
      JdbcClient jdbc = AUTH.jdbc();
      jdbc.sql(Sql.load("sql/auth/insert_user.sql"))
          .params(user.username(), PASSWORD_ENCODER.encode(user.password()))
          .update();
      byte[] userId = requireId(jdbc, user.username());
      jdbc.sql(Sql.load("sql/auth/insert_authority.sql")).params(userId, "read").update();
      jdbc.sql(Sql.load("sql/auth/insert_authority.sql")).params(userId, "write").update();
    });
    return new AuthAccount(user);
  }

  public static AuthAccount of(TestUser user) {
    return new AuthAccount(user);
  }

  public TestUser user() {
    return user;
  }

  public String username() {
    return user.username();
  }

  public boolean exists() {
    return findId(user.username()).isPresent();
  }

  public void delete() {
    AUTH.run(() -> {
      JdbcClient jdbc = AUTH.jdbc();
      byte[] userId = findId(jdbc, user.username()).orElse(null);
      if (userId == null) {
        return;
      }
      jdbc.sql(Sql.load("sql/auth/delete_authorities.sql")).param(userId).update();
      jdbc.sql(Sql.load("sql/auth/delete_user.sql")).param(user.username()).update();
    });
  }

  private static Optional<byte[]> findId(String username) {
    return findId(AUTH.jdbc(), username);
  }

  private static Optional<byte[]> findId(JdbcClient jdbc, String username) {
    return jdbc.sql(Sql.load("sql/auth/find_user_id.sql"))
        .param(username)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional();
  }

  private static byte[] requireId(JdbcClient jdbc, String username) {
    return findId(jdbc, username)
        .orElseThrow(() -> new IllegalStateException("auth user не найден после insert: " + username));
  }
}
