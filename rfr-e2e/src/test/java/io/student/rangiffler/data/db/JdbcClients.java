package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.EnumMap;
import java.util.Map;

/**
 * Кэш Spring {@link JdbcClient} по {@link Databases}.
 */
public final class JdbcClients {

  /** Один клиент на схему — создаётся при загрузке класса. */
  private static final Map<Databases, JdbcClient> CLIENTS = new EnumMap<>(Databases.class);

  static {
    for (Databases database : Databases.values()) {
      CLIENTS.put(database, JdbcClient.create(database.dataSource()));
    }
  }

  private JdbcClients() {
  }

  /**
   * Готовый {@link JdbcClient} для схемы (auth/api).
   * Не создаёт новый клиент на каждый вызов — берёт из кэша.
   */
  public static JdbcClient of(Databases database) {
    return CLIENTS.get(database);
  }
}
