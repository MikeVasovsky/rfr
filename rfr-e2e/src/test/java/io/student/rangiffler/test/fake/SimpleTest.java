package io.student.rangiffler.test.fake;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smoke-заглушка пайплайна JUnit + Allure без браузера.
 */
@Tag("fake")
public class SimpleTest {

  @Test
  void simpleTest() {
    Allure.step("Простой тест", () -> {
      assertEquals(2, 1 + 1);
    });
  }
}
