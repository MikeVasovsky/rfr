package io.student.rangiffler.data.stand;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 * HTTP-проверка готовности: короткий connect/request timeout, допустимые status codes.
 */
public final class HttpStandCheck implements StandCheck {

  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);

  private final String name;
  private final URI uri;
  private final IntPredicate statusOk;
  private final String startCommand;
  private final HttpClient client;

  public HttpStandCheck(String name, String url, Set<Integer> okStatuses, String startCommand) {
    this(
        name,
        URI.create(Objects.requireNonNull(url, "url")),
        okStatuses::contains,
        startCommand,
        HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build()
    );
  }

  HttpStandCheck(
      String name,
      URI uri,
      IntPredicate statusOk,
      String startCommand,
      HttpClient client
  ) {
    this.name = Objects.requireNonNull(name, "name");
    this.uri = Objects.requireNonNull(uri, "uri");
    this.statusOk = Objects.requireNonNull(statusOk, "statusOk");
    this.startCommand = Objects.requireNonNull(startCommand, "startCommand");
    this.client = Objects.requireNonNull(client, "client");
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean ready() {
    try {
      HttpRequest request = HttpRequest.newBuilder(uri)
          .timeout(REQUEST_TIMEOUT)
          .GET()
          .build();
      HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
      return statusOk.test(response.statusCode());
    } catch (Exception ignored) {
      return false;
    }
  }

  @Override
  public String startCommand() {
    return startCommand;
  }
}
