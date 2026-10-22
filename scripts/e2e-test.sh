#!/usr/bin/env bash
# End-to-end check of the full stack: the React dev server (5173) proxies the
# API calls to the Spring Boot backend (8080), which stores them in PostgreSQL.
set -u

FRONTEND="${1:-http://localhost:5173}"
BACKEND="${2:-http://localhost:8080}"
PASS=0
FAIL=0

check() {
  local label="$1" expected="$2" actual="$3"
  if [ "$expected" = "$actual" ]; then
    printf '  [PASS] %-58s (%s)\n' "$label" "$actual"
    PASS=$((PASS + 1))
  else
    printf '  [FAIL] %-58s expected %s, got %s\n' "$label" "$expected" "$actual"
    FAIL=$((FAIL + 1))
  fi
}

status() { curl -sS -o /dev/null -w '%{http_code}' "$@"; }
body() { curl -sS "$@"; }

echo "=============================================="
echo " Full stack end-to-end test"
echo " frontend: $FRONTEND   backend: $BACKEND"
echo "=============================================="

echo ""
echo "-- Frontend is served --"
check "GET / returns 200" "200" "$(status "$FRONTEND/")"
check "index.html contains the app root" "yes" "$(body "$FRONTEND/" | grep -q 'id="root"' && echo yes || echo no)"
check "index.html loads the React entry point" "yes" "$(body "$FRONTEND/" | grep -q '/src/main.tsx' && echo yes || echo no)"
check "Vite serves the React entry point" "200" "$(status "$FRONTEND/src/main.tsx")"

echo ""
echo "-- The dev server proxies /api to the backend --"
check "GET /api/tasks through the proxy returns 200" "200" "$(status "$FRONTEND/api/tasks")"

# Start from a clean slate so the assertions below are deterministic.
for id in $(body "$FRONTEND/api/tasks" | grep -o '"id":[0-9]*' | cut -d: -f2); do
  curl -sS -o /dev/null -X DELETE "$FRONTEND/api/tasks/$id"
done

echo ""
echo "-- Create through the proxy --"
CREATED=$(body -X POST "$FRONTEND/api/tasks" -H 'Content-Type: application/json' \
  -d '{"title":"End to end task","description":"Created through the Vite proxy","dueDate":"2030-06-01","completed":false}')
ID=$(echo "$CREATED" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
check "POST through the proxy creates the task" "End to end task" "$(echo "$CREATED" | sed -n 's/.*"title":"\([^"]*\)".*/\1/p')"

echo ""
echo "-- The task really reached PostgreSQL --"
check "The task is visible through the backend directly" "End to end task" "$(body "$BACKEND/api/tasks/$ID" | sed -n 's/.*"title":"\([^"]*\)".*/\1/p')"
check "The task is listed by the backend" "yes" "$(body "$BACKEND/api/tasks" | grep -q 'End to end task' && echo yes || echo no)"
check "The task persisted with its due date" "2030-06-01" "$(body "$BACKEND/api/tasks/$ID" | sed -n 's/.*"dueDate":"\([^"]*\)".*/\1/p')"

echo ""
echo "-- Update, complete and delete through the proxy --"
UPDATED=$(body -X PUT "$FRONTEND/api/tasks/$ID" -H 'Content-Type: application/json' \
  -d '{"title":"End to end task (updated)","description":"Updated","dueDate":"2030-07-02","completed":false}')
check "PUT through the proxy updates the task" "End to end task (updated)" "$(echo "$UPDATED" | sed -n 's/.*"title":"\([^"]*\)".*/\1/p')"

TOGGLED=$(body -X PATCH "$FRONTEND/api/tasks/$ID/complete")
check "PATCH through the proxy completes the task" "true" "$(echo "$TOGGLED" | sed -n 's/.*"completed":\([a-z]*\).*/\1/p')"
check "The backend reflects the new status" "true" "$(body "$BACKEND/api/tasks/$ID" | sed -n 's/.*"completed":\([a-z]*\).*/\1/p')"

check "DELETE through the proxy removes the task" "204" "$(status -X DELETE "$FRONTEND/api/tasks/$ID")"
check "The task is gone from PostgreSQL" "404" "$(status "$BACKEND/api/tasks/$ID")"
check "The task is no longer listed" "no" "$(body "$BACKEND/api/tasks" | grep -q 'End to end task' && echo yes || echo no)"

echo ""
echo "-- CORS allows the Vite origin --"
CORS=$(curl -sS -D - -o /dev/null -X OPTIONS "$BACKEND/api/tasks" \
  -H 'Origin: http://localhost:5173' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: content-type' 2>&1)
check "Access-Control-Allow-Origin is http://localhost:5173" "yes" "$(echo "$CORS" | grep -qi 'access-control-allow-origin: http://localhost:5173' && echo yes || echo no)"
check "Access-Control-Allow-Methods includes POST" "yes" "$(echo "$CORS" | grep -qi 'access-control-allow-methods:.*POST' && echo yes || echo no)"

echo ""
echo "-- Swagger UI --"
check "GET /swagger-ui/index.html returns 200" "200" "$(status "$BACKEND/swagger-ui/index.html")"
check "The OpenAPI document lists the tasks endpoints" "yes" "$(body "$BACKEND/v3/api-docs" | grep -q '/api/tasks' && echo yes || echo no)"

echo ""
echo "=============================================="
printf ' Result: %s passed, %s failed\n' "$PASS" "$FAIL"
echo "=============================================="
[ "$FAIL" -eq 0 ]
