package io.student.rangiffler.jupiter.probe.stand;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Мишень: UI-класс, фронт не отвечает за таймаут — тесты не стартуют.
 */
@ProbeTarget
@Tag("ui")
public class FrontNeverReadyCase {

  @Test
  void shouldNotRun() {
  }
}
