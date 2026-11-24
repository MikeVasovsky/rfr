package io.student.rangiffler.data.fixture.session;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.data.fixture.user.UserRequest;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.model.UserSession;

/**
 * Сессия пользователя, заранее созданного в БД через {@link UserRequest}.
 */
public final class UserSessionFixture extends AbstractSessionFixture {

  private UserFixture userFixture;

  @Override
  public void start(BrowserRuntime runtime, Session annotation, TestConfig config) {
    if (annotation.guest()) {
      throw new IllegalArgumentException("UserSessionFixture не поддерживает @Session(guest = true)");
    }
    userFixture = UserRequest.from(annotation).fixture();
    openBrowser(runtime, config);
    TestUser user = userFixture.user();
    session = new UserSession(user, page);
  }

  @Override
  protected void closeData() {
    if (userFixture != null) {
      userFixture.close();
    }
  }
}
