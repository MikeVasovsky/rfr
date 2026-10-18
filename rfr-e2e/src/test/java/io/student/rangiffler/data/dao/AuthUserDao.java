package io.student.rangiffler.data.dao;

import io.student.rangiffler.data.db.InTransaction;
import io.student.rangiffler.data.db.JdbcClients;
import io.student.rangiffler.data.db.Sql;
import io.student.rangiffler.model.TestUser;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.student.rangiffler.data.db.Databases.AUTH;

/**
 * JDBC-доступ к схеме {@code rangiffler-auth}: создание и удаление пользователей и authorities.
 */
public final class AuthUserDao {

  /** Тот же delegating encoder, что у Spring Security в auth-сервисе ({@code {bcrypt}...}). */
  private static final PasswordEncoder PASSWORD_ENCODER =
      PasswordEncoderFactories.createDelegatingPasswordEncoder();

  private AuthUserDao() {
  }

  /**
   * В одной транзакции: insert пользователя (пароль хешируется),
   * затем authorities {@code read} и {@code write} по его id.
   */
  public static void create(TestUser user) {
    InTransaction.run(AUTH, () -> {
      JdbcClient jdbc = jdbc();
      jdbc.sql(Sql.load("sql/auth/insert_user.sql"))
          .params(user.username(), PASSWORD_ENCODER.encode(user.password()))
          .update();

      byte[] userId = requireUserId(jdbc, user.username());
      insertAuthority(jdbc, userId, "read");
      insertAuthority(jdbc, userId, "write");
    });
  }

  /**
   * Удаляет authorities и пользователя по username.
   * Если записи нет — ничего не делает (идемпотентно).
   */
  public static void delete(TestUser user) {
    InTransaction.run(AUTH, () -> {
      JdbcClient jdbc = jdbc();
      byte[] userId = findUserId(jdbc, user.username());
      if (userId == null) {
        return;
      }
      jdbc.sql(Sql.load("sql/auth/delete_authorities.sql")).param(userId).update();
      jdbc.sql(Sql.load("sql/auth/delete_user.sql")).param(user.username()).update();
    });
  }

  /** {@link JdbcClient} для схемы auth. */
  private static JdbcClient jdbc() {
    return JdbcClients.of(AUTH);
  }

  /**
   * Ищет binary id пользователя по username.
   * Нужен для insert/delete authorities; {@code null}, если пользователя нет.
   */
  private static byte[] findUserId(JdbcClient jdbc, String username) {
    return jdbc.sql(Sql.load("sql/auth/find_user_id.sql"))
        .param(username)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional()
        .orElse(null);
  }

  /**
   * Как {@link #findUserId}, но после insert: отсутствие строки — ошибка состояния,
   * а не «пользователь уже удалён».
   */
  private static byte[] requireUserId(JdbcClient jdbc, String username) {
    byte[] userId = findUserId(jdbc, username);
    if (userId == null) {
      throw new IllegalStateException("Пользователь не найден после insert: " + username);
    }
    return userId;
  }

  /** Одна строка в {@code authority}: связь userId ↔ роль ({@code read}/{@code write}). */
  private static void insertAuthority(JdbcClient jdbc, byte[] userId, String authority) {
    jdbc.sql(Sql.load("sql/auth/insert_authority.sql")).params(userId, authority).update();
  }
}
