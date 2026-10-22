#!/usr/bin/env bash
# Smoke test of every REST endpoint against a running backend.
# Usage: ./scripts/api-smoke-test.sh [base-url]
set -u

BASE="${1:-http://localhost:8080}"
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
echo " Task Manager API smoke test - $BASE"
echo "=============================================="

echo ""
echo "-- Empty list --"
check "GET /api/tasks returns 200" "200" "$(status "$BASE/api/tasks")"
check "GET /api/tasks returns []" "[]" "$(body "$BASE/api/tasks")"

echo ""
echo "-- Create --"
CREATED=$(body -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' \
  -d '{"title":"Buy milk","description":"Semi skimmed","dueDate":"2030-01-15","completed":false}')
ID=$(echo "$CREATED" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
check "POST /api/tasks creates a task" "Buy milk" "$(echo "$CREATED" | sed -n 's/.*"title":"\([^"]*\)".*/\1/p')"
check "POST /api/tasks returns the generated id" "yes" "$([ -n "$ID" ] && echo yes || echo no)"
check "POST /api/tasks returns 201" "201" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"Second task"}')"

echo ""
echo "-- Validation --"
check "POST with a blank title returns 400" "400" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"   "}')"
check "POST with a title over 200 chars returns 400" "400" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "{\"title\":\"$(printf 'a%.0s' {1..201})\"}")"
check "POST with a malformed body returns 400" "400" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{ nope }')"
check "POST with an unknown field is ignored" "201" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"Extra field","unknown":"x"}')"

echo ""
echo "-- Read --"
check "GET /api/tasks/{id} returns 200" "200" "$(status "$BASE/api/tasks/$ID")"
check "GET /api/tasks/{id} returns the title" "Buy milk" "$(body "$BASE/api/tasks/$ID" | sed -n 's/.*"title":"\([^"]*\)".*/\1/p')"
check "GET /api/tasks/{id} returns 404 when missing" "404" "$(status "$BASE/api/tasks/999999")"
check "GET /api/tasks/{id} returns 400 for a non numeric id" "400" "$(status "$BASE/api/tasks/abc")"

echo ""
echo "-- Filter --"
PENDING=$(body -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"Pending one","completed":false}')
PENDING_ID=$(echo "$PENDING" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
DONE=$(body -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"Completed one","completed":true}')
DONE_ID=$(echo "$DONE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
check "GET /api/tasks?status=PENDING returns 200" "200" "$(status "$BASE/api/tasks?status=PENDING")"
check "GET /api/tasks?status=PENDING only returns pending tasks" "false" "$(body "$BASE/api/tasks?status=PENDING" | grep -o '"completed":true' | head -1 | sed 's/.*://' | grep -q true && echo true || echo false)"
check "GET /api/tasks?status=COMPLETED returns 200" "200" "$(status "$BASE/api/tasks?status=COMPLETED")"
check "GET /api/tasks?status=COMPLETED only returns completed tasks" "true" "$(body "$BASE/api/tasks?status=COMPLETED" | grep -q '"completed":false' && echo false || echo true)"
check "GET /api/tasks?status=UNKNOWN returns 400" "400" "$(status "$BASE/api/tasks?status=UNKNOWN")"

echo ""
echo "-- Update --"
UPDATED=$(body -X PUT "$BASE/api/tasks/$ID" -H 'Content-Type: application/json' \
  -d '{"title":"Buy oat milk","description":"Changed","dueDate":"2030-02-20","completed":false}')
check "PUT /api/tasks/{id} updates the title" "Buy oat milk" "$(echo "$UPDATED" | sed -n 's/.*"title":"\([^"]*\)".*/\1/p')"
check "PUT /api/tasks/{id} updates the due date" "2030-02-20" "$(echo "$UPDATED" | sed -n 's/.*"dueDate":"\([^"]*\)".*/\1/p')"
check "PUT /api/tasks/{id} returns 404 when missing" "404" "$(status -X PUT "$BASE/api/tasks/999999" -H 'Content-Type: application/json' -d '{"title":"x"}')"
check "PUT with a blank title returns 400" "400" "$(status -X PUT "$BASE/api/tasks/$ID" -H 'Content-Type: application/json' -d '{"title":""}')"

echo ""
echo "-- Complete / uncomplete --"
TOGGLED=$(body -X PATCH "$BASE/api/tasks/$ID/complete")
check "PATCH toggles a pending task to completed" "true" "$(echo "$TOGGLED" | sed -n 's/.*"completed":\([a-z]*\).*/\1/p')"
TOGGLED=$(body -X PATCH "$BASE/api/tasks/$ID/complete")
check "PATCH toggles it back to pending" "false" "$(echo "$TOGGLED" | sed -n 's/.*"completed":\([a-z]*\).*/\1/p')"
EXPLICIT=$(body -X PATCH "$BASE/api/tasks/$ID/complete" -H 'Content-Type: application/json' -d '{"completed":true}')
check "PATCH with an explicit body sets the status" "true" "$(echo "$EXPLICIT" | sed -n 's/.*"completed":\([a-z]*\).*/\1/p')"
check "PATCH returns 404 when missing" "404" "$(status -X PATCH "$BASE/api/tasks/999999/complete")"

echo ""
echo "-- Delete --"
check "DELETE /api/tasks/{id} returns 204" "204" "$(status -X DELETE "$BASE/api/tasks/$PENDING_ID")"
check "GET after DELETE returns 404" "404" "$(status "$BASE/api/tasks/$PENDING_ID")"
check "DELETE returns 404 when missing" "404" "$(status -X DELETE "$BASE/api/tasks/999999")"
check "DELETE returns 404 when deleted twice" "404" "$(status -X DELETE "$BASE/api/tasks/$PENDING_ID")"

echo ""
echo "-- Documentation --"
check "GET /v3/api-docs returns 200" "200" "$(status "$BASE/v3/api-docs")"
check "GET /swagger-ui/index.html returns 200" "200" "$(status "$BASE/swagger-ui/index.html")"

echo ""
echo "=============================================="
printf ' Result: %s passed, %s failed\n' "$PASS" "$FAIL"
echo "=============================================="
[ "$FAIL" -eq 0 ]
