package io.student.rangiffler.data.db;

import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Выполнение действия в транзакции выбранной {@link Databases}.
 */
public final class InTransaction {

  private InTransaction() {
  }

  /**
   * Запускает {@code action} в транзакции схемы: commit при успехе, rollback при исключении.
   * Для void-операций (insert/delete без возвращаемого значения).
   */
  public static void run(Databases database, Runnable action) {
    template(database).executeWithoutResult(status -> action.run());
  }

  /**
   * Как {@link #run}, но возвращает результат {@code action}
   * (удобно, когда из транзакции нужно вернуть id/entity).
   */
  public static <T> T call(Databases database, Supplier<T> action) {
    return template(database).execute(status -> action.get());
  }

  /** {@link TransactionTemplate} на transaction manager выбранной схемы. */
  private static TransactionTemplate template(Databases database) {
    return new TransactionTemplate(database.transactionManager());
  }
}
