## Запуск

```bash
cd rfr-auth && ../gradlew bootRun
# оба бэкенда: из корня ./gradlew runAll --parallel
```

Main: `io.student.rangiffler.RangifflerAuthApplication` (порт **9001**). Нужен MySQL. Подробности OAuth/клиент/TTL — `README.md` в этом модуле; общий порядок стека — корневой `AGENTS.md`.

## Как работает сервис

**Регистрация** (`GET/POST /register` → `UserServiceImpl.registerUser`):
- Пишет в схему `rangiffler-auth`: строка `user` (encoded password, флаги account = true) + две `authority`: `read` и `write`.
- Дубликат username → ошибка. Ответ — Thymeleaf, **не** создаёт пользователя в `rangiffler-api`.

**Логин и токены:**
- Form login (`/login`) → сессия `JSESSIONID` (timeout из YAML).
- Фронт идёт на `/oauth2/authorize` (клиент `client`, PKCE) → при необходимости login → code → `/oauth2/token` → **access_token** + **id_token**.
- Access JWT: `sub` = username, `aud` = `rangiffler-api`, claim `authorities` = `read`/`write` (`AccessTokenCustomizer`). API принимает только access Bearer; `id_token` — для фронта / OIDC logout.
- Refresh token публичному клиенту не выдаётся. JWKS in-memory: restart auth инвалидирует старые JWT.

**Граница с api:** auth хранит только учётки для входа. Профиль Rangiffler (имя, аватар, дружба) появляется в api при первом `Query.user` — см. `rfr-api/AGENTS.md`. Связь сервисов — по **username**, UUID в двух БД разные.

## Архитектура модулссвцс

Пакеты под `io.student.rangiffler` (новые классы — в существующие пакеты):

| Пакет | Назначение |
|-------|------------|
| `config` | Security filter chains, JWKS, OAuth customizers, theme cookie |
| `controller` | Login / register / error (Thymeleaf) |
| `data` (+ `repository`) | Сущности пользователей и authorities, JPA |
| `model` | Формы (регистрация) |
| `service` (+ `api` / `impl` / `cors`) | Регистрация, UserDetails, logout/CORS/errors |
| `validation` | Кастомные constraint’ы форм |

Ресурсы: `application.yaml`, Flyway `db/migration/rangiffler-auth/`, `templates/`, `static/`.

- Не плодить новые top-level пакеты без нужды; UI логина — Thymeleaf, не SPA.
- Схема БД — только Flyway (`ddl-auto: none`); связь с api-пользователями — по **username**, UUID разные.
- Access token кастомизируется в `AccessTokenCustomizer` (`aud`, `authorities`); restart сервиса пересоздаёт JWKS → старые токены невалидны.
- Публичный OAuth-клиент `client` (PKCE); правки client/redirect/TTL — в `application.yaml`, сверять с фронтом.
- Spring Boot 4 / Jackson 3: для нового кода `tools.jackson.databind.*`, не legacy `com.fasterxml...`.
