## Запуск

```bash
cd rfr-auth && ../gradlew bootRun
# оба бэкенда: из корня ./gradlew runAll --parallel
```

Порт **9001**, нужен MySQL. OAuth/client/TTL — `README.md` модуля; порядок стека — корневой `AGENTS.md` / `README.md`.

## Неочевидное

- Auth хранит только учётки для входа. Профиль (имя, аватар, дружба) — в **api** при первом `Query.user`. Связь сервисов по **username**, UUID в двух БД разные.
- В API уходит только **access** Bearer; `id_token` — для фронта / OIDC logout. Restart auth пересоздаёт JWKS → старые JWT невалидны.
- Публичный OAuth-клиент `client` (PKCE); client / redirect / TTL — в `application.yaml`, сверять с фронтом.
- UI логина/регистрации — Thymeleaf, не SPA. Не плодить top-level пакеты под `io.student.rangiffler` без нужды.
