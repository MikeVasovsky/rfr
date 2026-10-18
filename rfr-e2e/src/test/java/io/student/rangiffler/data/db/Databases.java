package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Известные схемы Rangiffler: у каждой свой {@link DataSource} и {@link PlatformTransactionManager}.
 */
public enum Databases {

  /** Схема {@code rangiffler-auth} (пользователи, authorities). */
  AUTH(TestConfig.getInstance().authJdbcUrl()),
  /** Схема {@code rangiffler-api} (users/countries и т.д. в API). */
  API(TestConfig.getInstance().apiJdbcUrl());

  private final DataSource dataSource;
  private final PlatformTransactionManager transactionManager;

  /**
   * Собирает DataSource и transaction manager для JDBC URL схемы
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
  }

  /** Пул/источник соединений к этой схеме (для {@link JdbcClients}, {@link Sql}). */
  public DataSource dataSource() {
    return dataSource;
  }

  /** Менеджер транзакций для {@link InTransaction} по этой схеме. */
  public PlatformTransactionManager transactionManager() {
    return transactionManager;
  }
}
