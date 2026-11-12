package io.student.rangiffler.test.extension.pool;

import io.qameta.allure.Allure;
import io.student.rangiffler.data.fixture.pool.UsersQueue;
import io.student.rangiffler.jupiter.extension.pool.UsersQueueExtension;
import io.student.rangiffler.jupiter.probe.pool.FailingStaticUserCase;
import io.student.rangiffler.jupiter.probe.pool.FourSequentialStaticUserCases;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.Events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EngineTestKit.engine;

/**
 * Оркестратор EngineTestKit для {@link UsersQueueExtension}: мишени в {@code jupiter.probe.pool}.
 */
@Tag("fake")
class UsersQueueExtensionTest {

  @BeforeEach
  void fillPool() {
    UsersQueue.resetToDefaultPool();
  }

  @AfterEach
  void restorePool() {
    UsersQueue.resetToDefaultPool();
  }

  @Test
  @DisplayName("Пул из 3 хватает на 4 последовательных @StaticUser теста")
  void fourSequentialTestsSucceedOnPoolOfThree() {
    Events testEvents = execute(FourSequentialStaticUserCases.class).testEvents();

    Allure.step("Четыре пробы успешны", () ->
        testEvents.assertStatistics(stats -> stats.started(4).succeeded(4))
    );
    Allure.step("После suite размер пула снова 3", () ->
        assertEquals(3, UsersQueue.size())
    );
  }

  @Test
  @DisplayName("После упавшего @StaticUser теста слот возвращается в пул")
  void returnsUserToQueueAfterFailedTest() {
    Events testEvents = execute(FailingStaticUserCase.class).testEvents();

    Allure.step("Проба FailingStaticUserCase упала", () ->
        testEvents.assertStatistics(stats -> stats.started(1).failed(1))
    );
    Allure.step("После fail размер пула снова 3", () ->
        assertEquals(3, UsersQueue.size())
    );
  }

  private static org.junit.platform.testkit.engine.EngineExecutionResults execute(Class<?> testClass) {
    return engine("junit-jupiter")
        .selectors(selectClass(testClass))
        .execute();
  }
}
