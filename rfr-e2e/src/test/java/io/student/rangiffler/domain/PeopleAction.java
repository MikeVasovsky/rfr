package io.student.rangiffler.domain;

/**
 * Подписи кнопок строки People (без строковых литералов в PO и тестах).
 */
public enum PeopleAction {
  ADD("Add"),
  ACCEPT("Accept"),
  DECLINE("Decline"),
  REMOVE("Remove"),
  CANCEL("Cancel");

  private final String title;

  PeopleAction(String title) {
    this.title = title;
  }

  public String title() {
    return title;
  }

  @Override
  public String toString() {
    return title;
  }
}
