package io.student.rangiffler.page;

/**
 * Вкладки страницы People.
 */
public enum PeopleTab {
  FRIENDS("Friends"),
  ALL_PEOPLE("All People"),
  OUTCOME_INVITATIONS("Outcome invitations"),
  INCOME_INVITATIONS("Income invitations");

  private final String title;

  PeopleTab(String title) {
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
