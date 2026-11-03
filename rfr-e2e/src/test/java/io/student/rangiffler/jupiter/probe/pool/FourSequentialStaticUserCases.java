package io.student.rangiffler.jupiter.probe.pool;

import io.student.rangiffler.jupiter.annotation.pool.StaticUser;
import io.student.rangiffler.jupiter.extension.pool.UsersQueueExtension;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Мишень EngineTestKit: четыре подряд {@code @StaticUser} на пуле из трёх.
 */
@ExtendWith(UsersQueueExtension.class)
public class FourSequentialStaticUserCases {

  @Test
  void first(@StaticUser TestUser user) {
    assertNotNull(user.username());
  }

  @Test
  void second(@StaticUser TestUser user) {
    assertNotNull(user.username());
  }

  @Test
  void third(@StaticUser TestUser user) {
    assertNotNull(user.username());
  }

  @Test
  void fourth(@StaticUser TestUser user) {
    assertNotNull(user.username());
  }
}
