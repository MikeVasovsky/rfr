package io.student.rangiffler.jupiter.annotation.pool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает параметр {@link io.student.rangiffler.model.TestUser}:
 * {@link io.student.rangiffler.jupiter.extension.pool.UsersQueueExtension} берёт пользователя из пула.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface StaticUser {
}
