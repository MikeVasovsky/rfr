#!/usr/bin/env bash
# Wait for MySQL, rfr-auth, rfr-api, rfr-gql-client (same readiness criteria as StandReadinessExtension).
set -euo pipefail

FRONT_URL="${RFR_FRONT_URL:-http://localhost:3001}"
AUTH_URL="${RFR_AUTH_URL:-http://localhost:9001}"
API_URL="${RFR_API_URL:-http://localhost:8081}"
TIMEOUT_SEC="${RFR_STAND_TIMEOUT:-180}"
POLL_SEC=0.5

deadline=$((SECONDS + TIMEOUT_SEC))

mysql_ready() {
  if command -v docker >/dev/null 2>&1 && docker exec rfr-mysql mysqladmin ping -uroot -psecret --silent >/dev/null 2>&1; then
    return 0
  fi
  return 1
}

http_status() {
  local url="$1"
  curl -sS -o /dev/null -w '%{http_code}' --connect-timeout 2 --max-time 2 "$url" || echo "000"
}

auth_ready() {
  local code
  code="$(http_status "${AUTH_URL}/")"
  [[ "$code" == "200" || "$code" == "302" ]]
}

api_ready() {
  local code
  code="$(http_status "${API_URL}/graphiql")"
  [[ "$code" == "401" ]]
}

front_ready() {
  local code
  code="$(http_status "${FRONT_URL}/")"
  [[ "$code" == "200" ]]
}

down_names=()
down_cmds=()

record_down() {
  down_names+=("$1")
  down_cmds+=("$2")
}

while true; do
  down_names=()
  down_cmds=()
  mysql_ready || record_down "MySQL" "docker compose up -d --wait"
  auth_ready || record_down "rfr-auth" "./gradlew runAll --parallel"
  api_ready || record_down "rfr-api" "./gradlew runAll --parallel"
  front_ready || record_down "rfr-gql-client" "npm run dev"

  if [[ ${#down_names[@]} -eq 0 ]]; then
    echo "Stand is ready"
    exit 0
  fi

  if (( SECONDS >= deadline )); then
    echo "Stand not ready within ${TIMEOUT_SEC}s. Still down: ${down_names[*]}" >&2
    echo "Start: ${down_cmds[*]}" >&2
    exit 1
  fi
  sleep "$POLL_SEC"
done
