package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Objects;
import java.util.Optional;

import static io.student.rangiffler.data.db.Databases.API;

/**
 * Профиль в {@code rangiffler-api.user}: create / delete / exists / id.
 */
public final class Profile {

  private static final String DEFAULT_COUNTRY_CODE = "ru";

  private final String username;

  private Profile(String username) {
    this.username = Objects.requireNonNull(username, "username");
  }

  public static Profile create(String username) {
    Objects.requireNonNull(username, "username");
    API.run(() -> {
      JdbcClient jdbc = API.jdbc();
      byte[] countryId = requireCountryId(jdbc, DEFAULT_COUNTRY_CODE);
      jdbc.sql(Sql.load("sql/api/insert_user.sql"))
          .params(username, countryId)
          .update();
    });
    return new Profile(username);
  }

  public static Profile of(String username) {
    return new Profile(username);
  }

  public static Optional<Profile> find(String username) {
    return findId(username).map(id -> new Profile(username));
  }

  public static Profile require(String username) {
    return find(username)
        .orElseThrow(() -> new IllegalStateException("api profile не найден: " + username));
  }

  /** @return {@code true}, если профиль создали здесь */
  public static boolean ensure(String username) {
    if (find(username).isPresent()) {
      return false;
    }
    create(username);
    return true;
  }

  public String username() {
    return username;
  }

  public boolean exists() {
    return findId(username).isPresent();
  }

  public byte[] id() {
    return findId(username)
        .orElseThrow(() -> new IllegalStateException("api profile не найден: " + username));
  }

  public void delete() {
    API.run(() ->
        API.jdbc().sql(Sql.load("sql/api/delete_user.sql")).param(username).update()
    );
  }

  static Optional<byte[]> findId(String username) {
    return API.jdbc().sql(Sql.load("sql/api/find_user_id.sql"))
        .param(username)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional();
  }

  private static byte[] requireCountryId(JdbcClient jdbc, String code) {
    return jdbc.sql(Sql.load("sql/api/find_country_id.sql"))
        .param(code)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional()
        .orElseThrow(() -> new IllegalStateException("Страна не найдена по code: " + code));
  }
}
