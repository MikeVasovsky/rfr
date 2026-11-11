package io.student.rangiffler.jupiter.probe.stand;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import org.junit.jupiter.api.Test;

/**
 * Мишень: класс без тега {@code ui} — к стенду нет запросов.
 */
@ProbeTarget
public class NoUiTagCase {

  @Test
  void runsWithoutStand() {
  }
}
