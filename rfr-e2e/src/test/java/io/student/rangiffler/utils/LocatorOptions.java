package io.student.rangiffler.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Короткие фабрики Playwright Options с {@code exact = true} для читаемых локаторов в Page Object.
 */
public final class LocatorOptions {

  private LocatorOptions() {
  }

  /** Опции для {@code page.getByLabel(...)} — полное совпадение accessible name. */
  public static Page.GetByLabelOptions exactLabel() {
    return new Page.GetByLabelOptions().setExact(true);
  }

  /** Опции для {@code page.getByRole(...)} — роль с точным {@code name}. */
  public static Page.GetByRoleOptions exactRole(String name) {
    return new Page.GetByRoleOptions().setName(name).setExact(true);
  }

  /** Опции для {@code locator.getByRole(...)} — роль с точным {@code name} в цепочке от родителя. */
  public static Locator.GetByRoleOptions exactLocatorRole(String name) {
    return new Locator.GetByRoleOptions().setName(name).setExact(true);
  }

  /** Опции для {@code page.getByText(...)} — полное совпадение текста. */
  public static Page.GetByTextOptions exactText() {
    return new Page.GetByTextOptions().setExact(true);
  }
}
