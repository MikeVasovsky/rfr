package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * JDBC-схемы e2e: DataSource, {@link JdbcClient} и транзакции.
 */
public enum Databases {

  AUTH(TestConfig.getInstance().authJdbcUrl());

  private final DataSource dataSource;
  private final JdbcClient jdbcClient;
  private final TransactionTemplate transactionTemplate;

  Databases(String jdbcUrl) {
    TestConfig cfg = TestConfig.getInstance();
    var ds = new DriverManagerDataSource();
    ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
    ds.setUrl(jdbcUrl);
    ds.setUsername(cfg.jdbcUser());
    ds.setPassword(cfg.jdbcPassword());
    this.dataSource = ds;
    this.jdbcClient = JdbcClient.create(ds);
    this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(ds));
  }

  public DataSource dataSource() {
    return dataSource;
  }

  public JdbcClient jdbcClient() {
    return jdbcClient;
  }

  public void run(Runnable action) {
    transactionTemplate.executeWithoutResult(status -> action.run());
  }
}
