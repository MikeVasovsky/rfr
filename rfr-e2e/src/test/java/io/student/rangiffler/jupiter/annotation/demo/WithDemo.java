package io.student.rangiffler.jupiter.annotation.demo;

import io.student.rangiffler.jupiter.extension.demo.DemoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Составная аннотация: {@link ExtendWith} как meta-annotation для {@link DemoExtension}.
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DemoExtension.class)
public @interface WithDemo {
}
