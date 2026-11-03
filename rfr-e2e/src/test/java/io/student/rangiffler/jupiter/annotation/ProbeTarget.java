package io.student.rangiffler.jupiter.annotation;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Мишень EngineTestKit для {@code UserExtensionTest}: не должна попадать в обычный suite.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Tag("probe")
public @interface ProbeTarget {
}
