package io.student.rangiffler.jupiter.probe.allure;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import org.junit.jupiter.api.Test;

/**
 * Мишень: прогон с {@code AllureEnvironmentExtension} — в results появляется environment.properties.
 */
@ProbeTarget
public class AllureEnvironmentCase {

  @Test
  void runsForAllureEnvironment() {
  }
}
