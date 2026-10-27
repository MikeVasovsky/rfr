package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * Известные схемы Rangiffler: DataSource, транзакции и {@link JdbcClient} на каждую.
 */
public enum Databases {

  /** Схема {@code rangiffler-auth} (пользователи, authorities). */
  AUTH(TestConfig.getInstance().authJdbcUrl()),
  /** Схема {@code rangiffler-api} (users/countries и т.д. в API). */
  API(TestConfig.getInstance().apiJdbcUrl());

  private final DataSource dataSource;
  private final PlatformTransactionManager transactionManager;
  private final JdbcClient jdbcClient;

  /**
   * Собирает DataSource, transaction manager и JdbcClient для JDBC URL схемы
   * (credentials — из {@link TestConfig}).
   */
  Databases(String jdbcUrl) {
    TestConfig cfg = TestConfig.getInstance();
    var ds = new DriverManagerDataSource();
    ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
    ds.setUrl(jdbcUrl);
    ds.setUsername(cfg.jdbcUser());
    ds.setPassword(cfg.jdbcPassword());
    this.dataSource = ds;
    this.transactionManager = new DataSourceTransactionManager(ds);
    this.jdbcClient = JdbcClient.create(ds);
  }

  /** Источник соединений к этой схеме (для {@link Sql}). */
  public DataSource dataSource() {
    return dataSource;
  }

  /** Готовый {@link JdbcClient} для этой схемы. */
  public JdbcClient jdbc() {
    return jdbcClient;
  }

  /**
   * Выполняет {@code action} в транзакции схемы: commit при успехе, rollback при исключении.
   * Для void-операций (insert/delete без возвращаемого значения).
   */
  public void run(Runnable action) {
    new TransactionTemplate(transactionManager).executeWithoutResult(status -> action.run());
  }

  /**
   * Как {@link #run}, но возвращает результат {@code action}
   * (удобно, когда из транзакции нужно вернуть id/entity).
   */
  public <T> T call(Supplier<T> action) {
    return new TransactionTemplate(transactionManager).execute(status -> action.get());
  }
}
