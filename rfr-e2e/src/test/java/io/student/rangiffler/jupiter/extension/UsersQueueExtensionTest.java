package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.fixture.UsersQueue;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.Events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EngineTestKit.engine;

/**
 * Мишени {@link UsersQueueExtension} через EngineTestKit: пул из трёх и возврат после падения.
 */
@Tag("fake")
class UsersQueueExtensionTest {

  private static final TestUser POOL_USER_1 = new TestUser("static-one", "pass-one");
  private static final TestUser POOL_USER_2 = new TestUser("static-two", "pass-two");
  private static final TestUser POOL_USER_3 = new TestUser("static-three", "pass-three");

  @BeforeEach
  void fillPool() {
    UsersQueue.reset(POOL_USER_1, POOL_USER_2, POOL_USER_3);
  }

  @AfterEach
  void clearPool() {
    UsersQueue.reset();
  }

  @Test
  void fourSequentialTestsSucceedOnPoolOfThree() {
    Events testEvents = execute(FourSequentialStaticUserCases.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(4).succeeded(4));
    assertEquals(3, UsersQueue.size());
  }

  @Test
  void returnsUserToQueueAfterFailedTest() {
    Events testEvents = execute(FailingStaticUserCase.class).testEvents();

    testEvents.assertStatistics(stats -> stats.started(1).failed(1));
    assertEquals(3, UsersQueue.size());
  }

  private static org.junit.platform.testkit.engine.EngineExecutionResults execute(Class<?> testClass) {
    return engine("junit-jupiter")
        .selectors(selectClass(testClass))
        .execute();
  }

  @ExtendWith(UsersQueueExtension.class)
  static class FourSequentialStaticUserCases {
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

  @ExtendWith(UsersQueueExtension.class)
  static class FailingStaticUserCase {
    @Test
    void fails(@StaticUser TestUser user) {
      assertNotNull(user.username());
      throw new RuntimeException("boom: " + user.username());
    }
  }
}
