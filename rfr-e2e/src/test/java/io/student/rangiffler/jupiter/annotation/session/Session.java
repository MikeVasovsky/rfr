package io.student.rangiffler.jupiter.annotation.session;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает параметр {@link io.student.rangiffler.model.UserSession}.
 * {@code guest = false} — пользователь создаётся в БД ({@code UserSessionFixture}).
 * {@code guest = true} — только credentials + BrowserContext ({@code GuestSessionFixture}), без create в БД.
 * UI-login — в тесте через {@link io.student.rangiffler.model.UserSession#login()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Session {
  boolean guest() default false;

  int friends() default 0;

  int incomeInvitations() default 0;

  int outcomeInvitations() default 0;
}
