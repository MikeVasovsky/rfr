# Rangiffler (rfr) — agent / developer guide


Подробный продуктовый README: `README.md`. Модули: `rfr-auth/README.md`, `rfr-api/README.md`, `rfr-gql-client/README.md`.

## Working rules (global)

1. **Текущее дерево, без worktree, без коммитов.** Правки — в текущем working tree; новые git worktree не создавать; `git commit` / push / PR — только по явной просьбе пользователя.
   - Cursor rule: `.cursor/rules/00-edit-in-place-no-commit.mdc` (`alwaysApply: true`)
2. **Перекрытие user rules.** Глобальные правила из `~/.cursor/rules` (Worktree, Elegant Objects, жёсткий unit-TDD) для этого репо **не применяются**, где конфликтуют.
   - Cursor rule: `.cursor/rules/02-rfr-overrides-globals.mdc` (`alwaysApply: true`)
3. **Сначала предложение.** Перед правками кода — 2–3 варианта и явный OK (исключения в rule).
   - Cursor rule: `.cursor/rules/01-propose-before-implement.mdc` (`alwaysApply: true`)
4. **Новый архитектурный слой → обновить AGENTS.md модуля.** Пакеты/папки-слои отражать в `AGENTS.md` той части проекта, где слой появился (например `rfr-e2e/AGENTS.md`). Корневой `AGENTS.md` — только для нового модуля в корне / Important paths / краткой отсылки.
   - Cursor rule: `.cursor/rules/03-agents-md-architecture-sync.mdc` (`alwaysApply: true`)

## Structure

```
rfr/
├── rfr-auth/         # OAuth 2.1 / OIDC Authorization Server (готов)
├── rfr-api/          # GraphQL API: users/countries/friendship real; feed/photos mock
├── rfr-gql-client/   # React SPA (готов)
├── rfr-e2e/          # E2E заготовка (JUnit 6 + Playwright + Allure 3)
├── docker-compose.yml # MySQL
├── localenv.sh       # поднять MySQL через compose
└── ports.sh          # освободить порты (Unix/lsof)
```

Gradle multi-project (`settings.gradle`): `rfr-api`, `rfr-auth`, `rfr-e2e`. Frontend — отдельный npm-проект.

Java package root: `io.student.rangiffler`.

## Stack

| Layer | Versions |
|-------|----------|
| Build | Java 25, Gradle 9.7.1 |
| Backend | Spring Boot 4.1.1, Spring Security / Authorization Server, GraphQL, JPA, Flyway 13 |
| Frontend | React 19, TypeScript 6, Vite 8, Apollo Client 4, MUI 9, React Router 8 |
| DB | MySQL 8.4.7 (Docker) |
| E2E | JUnit 6, Playwright Java 1.63, Allure 3 |

## Ports and URLs

| Service | Port | URL |
|---------|------|-----|
| MySQL | 3306 | `localhost:3306` |
| rfr-auth | 9001 | http://localhost:9001 |
| rfr-api GraphQL | 8081 | http://localhost:8081/graphql |
| rfr-api GraphiQL | 8081 | http://localhost:8081/graphiql |
| Frontend | 3001 | http://localhost:3001 |

OIDC discovery: http://localhost:9001/.well-known/openid-configuration

## Local startup (order matters)

1. MySQL  
2. `rfr-auth`  
3. `rfr-api` (нужны MySQL + issuer `http://localhost:9001`)  
4. Frontend  

### MySQL

```bash
./localenv.sh
# или: docker compose up mysql --wait
```

Credentials: user `root`, password `secret`.  
Schemas: `rangiffler-auth`, `rangiffler-api` (создаются JDBC `createDatabaseIfNotExist=true` + Flyway).

Windows: если `docker` не в PATH, CLI часто лежит в  
`%LOCALAPPDATA%\Programs\DockerDesktop\resources\bin\docker.exe`.

### Backend (auth + api)

Одной командой (нужен **`--parallel`**, иначе второй `bootRun` не стартанёт):

```bash
./gradlew runAll --parallel
# Windows: .\gradlew.bat runAll --parallel
```

По отдельности: `./gradlew :rfr-auth:bootRun` / `./gradlew :rfr-api:bootRun`.  
Main: `RangifflerAuthApplication` (9001), `RangifflerApiApplication` (8081).

### Frontend

```bash
cd rfr-gql-client
npm ci   # или npm i
npm run dev
```

Env уже в `rfr-gql-client/.env`:

```
VITE_AUTH_URL=http://localhost:9001
VITE_API_URL=http://localhost:8081
VITE_FRONT_URL=http://localhost:3001
VITE_CLIENT_ID=client
```

Vite может слушать только `localhost` / `[::1]` — проверять `http://localhost:3001`.

### Quick health check

- MySQL: `docker exec rfr-mysql mysqladmin ping -uroot -psecret` → `mysqld is alive`
- Auth: `GET http://127.0.0.1:9001/` → 302 → `/login`
- API: `GET /graphiql` без токена → **401** (сервис жив и защищён)
- Frontend: `GET http://localhost:3001/` → 200

## Auth flow (short)

- OAuth client: `client` (public, PKCE), redirect `http://localhost:3001/authorized`
- Access token: `aud=rangiffler-api`, claim `authorities` (`read`/`write`), TTL из YAML (**30m**)
- API принимает только **access** Bearer; `id_token` — для фронта / logout
- Restart `rfr-auth` пересоздаёт JWKS → старые токены становятся невалидны

## What is real vs mock in rfr-api

**Real (DB):** users, friendship, countries — controllers + JPA + Flyway.

**Mock (JSON):** feed / photos / likes — `resources/mock/*.json`, mutations не персистятся.

GraphQL schema: `rfr-api/src/main/resources/graphql/query.graphqls`  
DGS Codegen генерирует типы в `io.student.rangiffler.model.types` (`generateJava`) — руками не править.

## Database / Flyway

| Schema | Migration |
|--------|-----------|
| `rangiffler-auth` | `rfr-auth/src/main/resources/db/migration/rangiffler-auth/V1__schema_init.sql` |
| `rangiffler-api` | `rfr-api/src/main/resources/db/migration/rangiffler-api/V1__schema_init.sql` |

`ddl-auto: none`. Связь auth↔api пользователей — по **username**, UUID разные.

В API-схеме есть таблицы `photo` / `like` / `statistic` — пока без полноценной реализации в коде.

## Important paths

| What | Path |
|------|------|
| Auth config | `rfr-auth/src/main/resources/application.yaml` |
| API config | `rfr-api/src/main/resources/application.yml` |
| API security | `rfr-api/.../config/RangifflerApiConfiguration.java` |
| Auth security / OAuth | `rfr-auth/.../config/RangifflerAuthServiceConfig.java` |
| Mock feed JSON | `rfr-api/src/main/resources/mock/` |
| Frontend Apollo / auth | `rfr-gql-client/src/api/` |

## E2E

Архитектура, запуск, Page Object, фикстуры, теги — `rfr-e2e/AGENTS.md`. Подробности — `rfr-e2e/README.md`.

```bash
./gradlew runE2e
```

## Conventions for agents

- Порядок старта: MySQL → auth → api → frontend.
- GraphQL-модели не редактировать вручную — менять `query.graphqls` и codegen.
- Spring Boot 4 / Jackson 3: `tools.jackson.databind.*`, не legacy `com.fasterxml...` для нового кода.
- При правках API сверяться с фронтом и схемой GraphQL — контракт уже зафиксирован клиентом.
- Дипломная цель: разбить монолитный api/БД на микросервисы + полноценные тесты; Niffler — образец сервисов.
