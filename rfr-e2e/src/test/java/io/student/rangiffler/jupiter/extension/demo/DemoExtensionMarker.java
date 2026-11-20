package io.student.rangiffler.jupiter.extension.demo;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Маркерный интерфейс: {@link ExtendWith} на TYPE (test interface).
 */
@ExtendWith(DemoExtension.class)
public interface DemoExtensionMarker {
}
