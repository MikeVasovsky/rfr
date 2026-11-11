package io.student.rangiffler.test;

import io.student.rangiffler.data.db.FlywayAlignment;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Declarative JUnit Platform Suite: сверка Flyway в {@link BeforeSuite}, затем тесты пакета {@code test}.
 */
@Suite
@SuiteDisplayName("Rangiffler e2e")
@SelectPackages("io.student.rangiffler.test")
@IncludeClassNamePatterns(".*Test")
@ExcludeTags("probe")
class RangifflerE2eSuite {

  @BeforeSuite
  static void verifySchemaBeforeSuite() {
    FlywayAlignment.verifyOrThrow();
  }
}
