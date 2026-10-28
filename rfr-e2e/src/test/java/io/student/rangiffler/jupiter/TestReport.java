package io.student.rangiffler.jupiter;

import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Event;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Обёртка над {@link EngineExecutionResults}: статистика test-событий и published report entries.
 */
public final class TestReport {

  private final EngineExecutionResults results;

  private TestReport(EngineExecutionResults results) {
    this.results = Objects.requireNonNull(results, "results");
  }

  public static TestReport of(EngineExecutionResults results) {
    return new TestReport(results);
  }

  public TestReport assertTestsStarted(int count) {
    results.testEvents().assertStatistics(stats -> stats.started(count));
    return this;
  }

  public TestReport assertTestsSucceeded(int count) {
    results.testEvents().assertStatistics(stats -> stats.succeeded(count));
    return this;
  }

  public TestReport assertTestsFailed(int count) {
    results.testEvents().assertStatistics(stats -> stats.failed(count));
    return this;
  }

  public List<Map<String, String>> reportEntries() {
    return results.testEvents()
        .reportingEntryPublished()
        .stream()
        .map(TestReport::keyValuePairs)
        .toList();
  }

  public String requireCreatedUsername() {
    return reportEntries().stream()
        .filter(entry -> "created".equals(entry.get("user")))
        .map(entry -> entry.get("username"))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(() -> new AssertionError("Нет report entry user=created: " + reportEntries()));
  }

  public TestReport assertHasReportEntry(String key, String value) {
    boolean found = reportEntries().stream()
        .anyMatch(entry -> value.equals(entry.get(key)));
    assertTrue(found, () -> "Ожидали " + key + "=" + value + " в " + reportEntries());
    return this;
  }

  public TestReport assertHasReportEntryContaining(Map<String, String> expected) {
    Objects.requireNonNull(expected, "expected");
    boolean found = reportEntries().stream()
        .anyMatch(entry -> entry.entrySet().containsAll(expected.entrySet()));
    assertTrue(found, () -> "Ожидали " + expected + " в " + reportEntries());
    return this;
  }

  public TestReport assertNoUserLifecycleEntries() {
    boolean found = reportEntries().stream()
        .anyMatch(entry -> entry.containsKey("user"));
    assertTrue(!found, () -> "Не ожидали user lifecycle entries: " + reportEntries());
    return this;
  }

  private static Map<String, String> keyValuePairs(Event event) {
    return event.getPayload(ReportEntry.class)
        .orElseThrow(() -> new AssertionError("Event без ReportEntry: " + event))
        .getKeyValuePairs();
  }
}
