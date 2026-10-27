package io.student.rangiffler.data.dao;

import io.student.rangiffler.data.db.Sql;
import org.springframework.jdbc.core.simple.JdbcClient;

import static io.student.rangiffler.data.db.Databases.API;

/**
 * JDBC-доступ к схеме {@code rangiffler-api}: создание и удаление профиля пользователя.
 */
public final class ApiUserDao {

  private static final String DEFAULT_COUNTRY_CODE = "ru";

  private ApiUserDao() {
  }

  /**
   * Insert профиля с {@code username} и страной {@code ru}
   * (как lazy-create в API при первом {@code Query.user}).
   */
  public static void create(String username) {
    API.run(() -> {
      JdbcClient jdbc = API.jdbc();
      byte[] countryId = requireCountryId(jdbc, DEFAULT_COUNTRY_CODE);
      jdbc.sql(Sql.load("sql/api/insert_user.sql"))
          .params(username, countryId)
          .update();
    });
  }

  /**
   * Удаляет профиль по username.
   * Если записи нет — ничего не делает (идемпотентно).
   */
  public static void delete(String username) {
    API.run(() ->
        API.jdbc().sql(Sql.load("sql/api/delete_user.sql")).param(username).update()
    );
  }

  private static byte[] requireCountryId(JdbcClient jdbc, String code) {
    return jdbc.sql(Sql.load("sql/api/find_country_id.sql"))
        .param(code)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional()
        .orElseThrow(() -> new IllegalStateException("Страна не найдена по code: " + code));
  }
}
