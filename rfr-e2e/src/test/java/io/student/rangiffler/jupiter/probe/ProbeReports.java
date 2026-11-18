package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.TestReporter;

import java.util.Map;

/**
 * Публикация событий EngineTestKit-мишеней через {@link TestReporter}.
 */
public final class ProbeReports {

  private ProbeReports() {
  }

  public static void publishUser(TestReporter reporter, TestUser user) {
    reporter.publishEntry(Map.of(
        "event", "resolved",
        "username", user.username(),
        "friends", String.valueOf(user.friends().size()),
        "incomeInvitations", String.valueOf(user.incomeInvitations().size()),
        "outcomeInvitations", String.valueOf(user.outcomeInvitations().size())
    ));
  }
}
