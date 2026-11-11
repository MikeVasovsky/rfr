package io.student.rangiffler.jupiter.probe.stand;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Мишень: UI-класс при {@code rfr.standCheck=false} — к стенду нет запросов.
 */
@ProbeTarget
@Tag("ui")
public class StandCheckDisabledCase {

  @Test
  void runsWithStandCheckDisabled() {
  }
}
