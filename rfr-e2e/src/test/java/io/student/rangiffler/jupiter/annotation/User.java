package io.student.rangiffler.jupiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает параметр {@link io.student.rangiffler.model.TestUser}:
 * extension соберёт {@link io.student.rangiffler.jupiter.UserRequest} и создаст пользователя в auth БД.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface User {

  int friends() default 0;

  int incomeInvitations() default 0;
}
