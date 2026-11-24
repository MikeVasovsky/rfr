package io.student.rangiffler.jupiter.annotation;

import io.student.rangiffler.jupiter.extension.session.SessionExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Мета-аннотация dual UI-теста: теги {@code UI}/{@code ui} и {@link SessionExtension}
 * для параметров {@code @Session UserSession}. Без поля {@code page} — браузер поднимает extension.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@UiTest
@Tag("ui")
@ExtendWith(SessionExtension.class)
public @interface MultiSessionUiTest {
}
