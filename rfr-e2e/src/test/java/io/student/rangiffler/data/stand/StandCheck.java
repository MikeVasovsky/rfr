package io.student.rangiffler.data.stand;

/**
 * Одна проверка готовности сервиса стенда (имя для ошибки, probe, команда запуска).
 */
public interface StandCheck {

  String name();

  boolean ready();

  String startCommand();
}
