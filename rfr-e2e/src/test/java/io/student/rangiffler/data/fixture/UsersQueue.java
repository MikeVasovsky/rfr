package io.student.rangiffler.data.fixture;

import io.student.rangiffler.model.TestUser;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * JVM-wide потокобезопасная очередь заранее заведённых тестовых пользователей.
 */
public final class UsersQueue {

  private static final long TIMEOUT_SECONDS = 5;
  private static final BlockingQueue<TestUser> USERS = new LinkedBlockingQueue<>();

  private UsersQueue() {
  }

  public static TestUser acquire() {
    try {
      TestUser user = USERS.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (user == null) {
        throw new IllegalStateException(
            "Очередь тестовых пользователей пуста после ожидания " + TIMEOUT_SECONDS + " с"
        );
      }
      return user;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Ожидание пользователя из очереди прервано", e);
    }
  }

  public static void release(TestUser user) {
    Objects.requireNonNull(user, "user");
    if (!USERS.offer(user)) {
      throw new IllegalStateException("Не удалось вернуть пользователя в очередь: " + user.username());
    }
  }

  public static void reset(TestUser... users) {
    USERS.clear();
    for (TestUser user : users) {
      release(user);
    }
  }

  public static int size() {
    return USERS.size();
  }
}
