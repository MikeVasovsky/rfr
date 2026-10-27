package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.dao.ApiUserDao;
import io.student.rangiffler.data.dao.AuthUserDao;
import io.student.rangiffler.data.db.Sql;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

import java.util.Optional;

import static io.student.rangiffler.data.db.Databases.API;

/** Общие JDBC/auth+api шаги для friendship-декораторов (не Dao). */
final class ApiUsers {

  private ApiUsers() {
  }

  static Optional<byte[]> findId(String username) {
    return API.jdbc().sql(Sql.load("sql/api/find_user_id.sql"))
        .param(username)
        .query((rs, rowNum) -> rs.getBytes("id"))
        .optional();
  }

  static byte[] id(String username) {
    return findId(username)
        .orElseThrow(() -> new IllegalStateException("api user не найден: " + username));
  }

  /** @return true, если профиль создали здесь */
  static boolean ensure(String username) {
    if (findId(username).isPresent()) {
      return false;
    }
    ApiUserDao.create(username);
    return true;
  }

  static TestUser companion() {
    TestUser user = RandomDataUtils.createUser();
    AuthUserDao.create(user);
    ApiUserDao.create(user.username());
    return user;
  }

  static void deleteCompanion(TestUser user) {
    ApiUserDao.delete(user.username());
    AuthUserDao.delete(user);
  }

  /** ACCEPTED + PENDING с участием user; no-op, если api-профиля нет. */
  static void deleteFriendships(String username) {
    findId(username).ifPresent(id -> API.run(() -> {
      API.jdbc().sql(Sql.load("sql/api/delete_friends.sql")).params(id, id).update();
      API.jdbc().sql(Sql.load("sql/api/delete_invitations.sql")).params(id, id).update();
    }));
  }
}
