package io.student.rangiffler.jupiter.probe.user;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.probe.ProbeReports;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Мишень EngineTestKit: plain {@code @User} без связей.
 */
@ProbeTarget
@ExtendWith(UserExtension.class)
public class PlainUserCase {

  @Test
  void resolves(@User TestUser user, TestReporter reporter) {
    ProbeReports.publishUser(reporter, user);
  }
}
