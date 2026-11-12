package io.student.rangiffler.test.fake;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.demo.WithDemo;
import io.student.rangiffler.jupiter.extension.demo.DemoExtension;
import io.student.rangiffler.jupiter.extension.demo.DemoExtensionMarker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Демо class-level регистрации {@link ExtendWith}: класс, static-поля, interface, meta.
 * Два теста — по логам {@code instance=} видно шаринг extension и отдельные instance тестового класса.
 */
@Tag("fake")
@ExtendWith(DemoExtension.class)
@WithDemo
class DemoExtensionTest implements DemoExtensionMarker {

  @ExtendWith(DemoExtension.class)
  static Object extendWithOnStaticField;

  @WithDemo
  static Object withDemoOnStaticField;

  private final int testInstanceId = System.identityHashCode(this);

  DemoExtensionTest() {
    System.out.println("DemoExtensionTest.<init> testInstance=" + testInstanceId);
  }

  @Test
  @DisplayName("Первый тест: ParameterResolver отдаёт значение, в логах виден instance extension")
  void firstTest(String value) {
    System.out.println("DemoExtensionTest.firstTest testInstance=" + testInstanceId + " value=" + value);
    Allure.step("Проверить, что ParameterResolver вернул RESOLVED_VALUE", () ->
        assertEquals(DemoExtension.RESOLVED_VALUE, value)
    );
  }

  @Test
  @DisplayName("Второй тест: тот же сценарий — сравнить instance= в логах с первым тестом")
  void secondTest(String value) {
    System.out.println("DemoExtensionTest.secondTest testInstance=" + testInstanceId + " value=" + value);
    Allure.step("Проверить, что ParameterResolver вернул RESOLVED_VALUE", () ->
        assertEquals(DemoExtension.RESOLVED_VALUE, value)
    );
  }
}
