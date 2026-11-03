## Запуск

```bash
cd rfr-gql-client
npm ci   # или npm i
npm run dev
```

`http://localhost:3001` (часто только `localhost` / `[::1]`). Env — `.env`. Нужен стек MySQL → auth → api. Короткие команды — корневой `README.md` («Быстрый старт»). Маршруты/хуки — `README.md` модуля.

## Неочевидное

- Auth только через UI/OAuth PKCE; в GraphQL уходит **access** token; `id_token` — для logout hint.
- Новые GraphQL-вызовы — в `hooks/` (`operationName` стабилен для e2e), не размазывать `useQuery`/`useMutation` по страницам без нужды.
- Сверять операции со схемой `rfr-api` (`query.graphqls`); mock feed/photo на бэке → UI «успех» ≠ персист.
- Не плодить top-level каталоги под `src/` без явной нужды; стили/тема — в существующем MUI setup.
