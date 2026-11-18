package io.student.rangiffler.jupiter.annotation;

import io.student.rangiffler.jupiter.extension.BrowserTraceExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Мета-аннотация UI-теста: тег {@code UI} и трассировка упавших тестов.
 * Браузер — {@link io.student.rangiffler.test.BaseUiTest}; action-timeout — {@code BrowserTimeoutExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@ExtendWith(BrowserTraceExtension.class)
@Tag("UI")
public @interface UiTest {
}
