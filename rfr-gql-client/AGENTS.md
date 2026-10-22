## Запуск

```bash
cd rfr-gql-client
npm ci   # или npm i
npm run dev
```

Dev-сервер: `http://localhost:3001` (часто только `localhost` / `[::1]`). Env — `.env` (`VITE_AUTH_URL`, `VITE_API_URL`, `VITE_FRONT_URL`, `VITE_CLIENT_ID`). Нужен стек MySQL → auth → api. Подробности маршрутов/хуков — `README.md` в этом модуле.

## Архитектура модуля

Каталоги под `src/` (новые фичи — в существующие слои):

| Каталог | Назначение |
|---------|------------|
| `api/` | Apollo client, OAuth token/revoke, PKCE/utils, разбор GraphQL errors |
| `components/` | UI-блоки (карта, лента, люди, профиль, layout) |
| `context/` | Session, countries, dialogs, snackbar |
| `hooks/` | По хуку на GraphQL-операцию (`operationName` стабилен для e2e) |
| `pages/` | Маршруты (landing, OAuth callback, my-travels, people, profile, …) |
| `types/` | TS-модели под ответы GraphQL |
| `utils/` | Мелкие хелперы |
| `assets/` | Шрифты и статика |

Корень `src/`: `App.tsx`, `main.tsx`, `theme.tsx`.

- Auth только через UI/OAuth PKCE (`api/auth*`); в GraphQL уходит **access** token; `id_token` — для logout hint.
- Новые GraphQL-вызовы — в `hooks/`, не размазывать `useQuery`/`useMutation` по страницам без нужды.
- Сверять операции со схемой `rfr-api` (`query.graphqls`); mock feed/photo на бэке — UI «успех» ≠ персист.
- Не плодить top-level каталоги под `src/` без явной нужды; стили/тема — в существующем MUI setup.
- Следовать принятым паттернам React/MUI/Apollo в репо; не тащить Elegant Objects и т.п.
