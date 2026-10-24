## Запуск

```bash
cd rfr-api && ../gradlew bootRun
# оба бэкенда: из корня ./gradlew runAll --parallel
```

Порт **8081** (`/graphql`, `/graphiql`). Нужны MySQL и живой issuer `rfr-auth` (`http://localhost:9001`). Детали схемы/mock — `README.md` модуля.

## Неочевидное

- Auth регистрирует только учётку; строки в `rangiffler-api.user` ещё нет. Первый `Query.user` (JWT `sub` = username) → lazy-create в api (дефолт location `ru`). UUID api ≠ UUID auth.
- **Real (БД):** users, friendship, countries. **Mock (JSON):** feed / photos / likes — `resources/mock/`, mutations не персистятся. Таблицы `photo` / `like` / `statistic` в Flyway есть, кодом пока не используются. Моки не убирать без явной команды.
- Типы в `io.student.rangiffler.model.types` (DGS Codegen) — **не править руками**. Источник: `graphql/query.graphqls` → `:rfr-api:generateJava`.
- Контракт с фронтом = GraphQL-схема; при смене API сверять `rfr-gql-client`. Только **access** Bearer; `@PreAuthorize` — `read` на query, `write` на mutation.
