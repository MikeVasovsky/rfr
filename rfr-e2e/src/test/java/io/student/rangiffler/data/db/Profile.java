package io.student.rangiffler.data.db;

import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import org.springframework.jdbc.core.simple.JdbcClient;

import static io.student.rangiffler.data.db.Databases.API;

/**
 * Профиль в {@code rangiffler-api.user}: create / exists / delete.
 */
public final class Profile {

  private final TestUser user;

  private Profile(TestUser user) {
    this.user = user;
  }

  public static Profile create(TestUser user, TestProfile profile) {
    API.run(() -> {
      JdbcClient jdbc = API.jdbcClient();
      byte[] countryId = findCountryId(jdbc, profile);
      if (countryId == null) {
        throw new IllegalStateException("Такой страны нет");
      }
      jdbc.sql(Sql.load("sql/api/create_profile.sql"))
          .params(user.username(), profile.firstname(), profile.surname(), countryId)
          .update();
    });
    return new Profile(user);
  }

  public static Profile of(TestUser user) {
    return new Profile(user);
  }

  public TestUser user() {
    return user;
  }

  public boolean exists() {
    return id() != null;
  }

  public byte[] id() {
    return API.jdbcClient()
        .sql(Sql.load("sql/api/find_user_id.sql"))
        .param(user.username())
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional()
        .orElse(null);
  }

  public byte[] requireId() {
    byte[] userId = id();
    if (userId == null) {
      throw new IllegalStateException("id пользователя не найден в api: " + user.username());
    }
    return userId;
  }

  public void delete() {
    API.run(() ->
        API.jdbcClient()
            .sql(Sql.load("sql/api/delete_user.sql"))
            .param(user.username())
            .update()
    );
  }

  private static byte[] findCountryId(JdbcClient jdbc, TestProfile profile) {
    return jdbc.sql(Sql.load("sql/api/find_country_id.sql"))
        .param(profile.country())
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional()
        .orElse(null);
  }
}
