package io.student.rangiffler.jupiter.annotation;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Мета-аннотация UI-теста: тег {@code UI}.
 * Браузер — {@link io.student.rangiffler.test.BaseUiTest}; action-timeout — {@code BrowserTimeoutExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Tag("UI")
public @interface UiTest {
}
