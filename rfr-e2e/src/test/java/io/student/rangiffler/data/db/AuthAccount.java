package io.student.rangiffler.data.db;

import io.student.rangiffler.model.TestUser;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.student.rangiffler.data.db.Databases.AUTH;

/**
 * Учётная запись в {@code rangiffler-auth}: create / lookup / delete.
 */
public final class AuthAccount {

  private static final PasswordEncoder PASSWORD_ENCODER =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();

  private final TestUser user;

  private AuthAccount(TestUser user) {
    this.user = user;
  }

  public static AuthAccount create(TestUser user) {
    AUTH.run(() -> {
      JdbcClient jdbc = AUTH.jdbcClient();
      jdbc.sql(Sql.load("sql/auth/insert_user.sql"))
          .params(user.username(), PASSWORD_ENCODER.encode(user.password()))
          .update();

      byte[] userId = requireUserId(jdbc, user.username());
      insertAuthority(jdbc, userId, "read");
      insertAuthority(jdbc, userId, "write");
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
    return findUserId(AUTH.jdbcClient(), user.username()) != null;
  }

  public void delete() {
    AUTH.run(() -> {
      JdbcClient jdbc = AUTH.jdbcClient();
      byte[] userId = findUserId(jdbc, user.username());
      if (userId == null) {
        return;
      }
      jdbc.sql(Sql.load("sql/auth/delete_authorities.sql")).param(userId).update();
      jdbc.sql(Sql.load("sql/auth/delete_user.sql")).param(user.username()).update();
    });
  }

  private static byte[] findUserId(JdbcClient jdbc, String username) {
    return jdbc.sql(Sql.load("sql/auth/find_user_id.sql"))
        .param(username)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional()
        .orElse(null);
  }

  private static byte[] requireUserId(JdbcClient jdbc, String username) {
    byte[] userId = findUserId(jdbc, username);
    if (userId == null) {
      throw new IllegalStateException("Пользователь не найден после insert: " + username);
    }
    return userId;
  }

  private static void insertAuthority(JdbcClient jdbc, byte[] userId, String authority) {
    jdbc.sql(Sql.load("sql/auth/insert_authority.sql")).params(userId, authority).update();
  }
}
