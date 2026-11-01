package io.student.rangiffler.data.db;

import static io.student.rangiffler.data.db.Databases.API;

import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class ApiProfile {
    private final TestUser user;

    private  ApiProfile(TestUser user) {
        this.user = user;
    }

    public static ApiProfile create(TestUser u, TestProfile p) {
        API.run(() -> {
            JdbcClient jdbc = API.jdbcClient();
            byte[] countryId = findCountryId(jdbc, p);
            if (countryId == null) {
                throw new IllegalStateException("Такой страны нет");
            }
            jdbc.sql(Sql.load("sql/api/create_profile.sql"))
                    .params(u.username(), p.firstname(), p.surname(), countryId).update();
        });
        return new ApiProfile(u);
    }

    private static byte[] findCountryId(JdbcClient jdbc, TestProfile p) {
        return jdbc.sql(
                        Sql.load("sql/api/find_country_id.sql"))
                .param(p.country())
                .query((rs, rowNum) -> rs.getBytes("id"))
                .optional()
                .orElse(null);
    }

    public void delete() {
        API.run(() ->
                API.jdbcClient()
                        .sql(Sql.load("sql/api/delete_user.sql"))
                        .param(user.username())
                        .update()
        );
    }

}
