package io.student.rangiffler.test.extension.suite;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.extension.suite.StandSchemaLauncherSessionListener;
import io.student.rangiffler.jupiter.probe.suite.EmptyLauncherProbeCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * HW04-2: проверяет, открывает ли {@code EngineTestKit} новую {@code LauncherSession}
 * (и тем самым SPI {@code LauncherSessionListener}).
 */
@Tag("fake")
class SuiteExtensionTest {

  @Test
  @DisplayName("EngineTestKit не открывает LauncherSession для SPI-слушателя")
  void engineTestKitDoesNotOpenLauncherSession() {
    int before = StandSchemaLauncherSessionListener.OPENED.get();
    EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(EmptyLauncherProbeCase.class))
        .execute()
        .testEvents()
        .assertStatistics(stats -> stats.started(1).succeeded(1));
    Allure.step("Счётчик opened не вырос внутри EngineTestKit", () ->
        assertEquals(before, StandSchemaLauncherSessionListener.OPENED.get())
    );
  }
}
