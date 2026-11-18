package io.student.rangiffler.jupiter.probe.pool;

import io.student.rangiffler.jupiter.annotation.pool.StaticUser;
import io.student.rangiffler.jupiter.extension.pool.UsersQueueExtension;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Мишень EngineTestKit: упавший тест возвращает слот пула в {@code close()}.
 */
@ExtendWith(UsersQueueExtension.class)
public class FailingStaticUserCase {

  @Test
  void fails(@StaticUser TestUser user) {
    assertNotNull(user.username());
    throw new RuntimeException("boom: " + user.username());
  }
}
