package io.student.rangiffler.session;

import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.MainPage;

/**
 * Авторизованная dual UI-сессия: пользователь из фикстуры и {@link MainPage} после UI-login.
 */
public record UserSession(TestUser user, MainPage mainPage) {
}
