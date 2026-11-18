package io.student.rangiffler.jupiter.probe.user;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Мишень EngineTestKit: метод без {@code @User} — fixture не создаётся.
 */
@ProbeTarget
@ExtendWith(UserExtension.class)
public class NoUserCase {

  @Test
  void runsWithoutUser(TestReporter reporter) {
    reporter.publishEntry("event", "no-user");
  }
}
