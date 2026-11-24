package io.student.rangiffler.session;

import io.student.rangiffler.page.LandingPage;

/**
 * Гостевая dual UI-сессия: открытый лендинг без UI-login и без create в БД.
 */
public record GuestSession(LandingPage landingPage) {
}
