package io.student.rangiffler.jupiter.probe.stand;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Мишень: UI-класс, фронт начинает отвечать через 2 с — тест выполняется.
 */
@ProbeTarget
@Tag("ui")
public class FrontReadyAfterDelayCase {

  @Test
  void runsWhenFrontBecomesReady() {
  }
}
