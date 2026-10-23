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

# Sends a request and records both the body and the status, so one call can
# check the code and still keep the id of the resource it created.
LAST_BODY=""
LAST_STATUS=""
request() {
  local output
  output=$(curl -sS -w '\n%{http_code}' "$@")
  LAST_STATUS="${output##*$'\n'}"
  LAST_BODY="${output%$'\n'*}"
}

echo "=============================================="
echo " Task Manager API smoke test - $BASE"
echo "=============================================="

echo ""
echo "-- Empty list --"
check "GET /api/tasks returns 200" "200" "$(status "$BASE/api/tasks")"
check "GET /api/tasks returns a page" "true" "$(body "$BASE/api/tasks" | grep -q '"content"' && echo true || echo false)"
check "GET /api/tasks reports the totals" "true" "$(body "$BASE/api/tasks" | grep -q '"totalElements"' && echo true || echo false)"

echo ""
echo "-- Create --"
CREATED=$(body -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' \
  -d '{"title":"Buy milk","description":"Semi skimmed","dueDate":"2030-01-15","completed":false}')
ID=$(echo "$CREATED" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
check "POST /api/tasks creates a task" "Buy milk" "$(echo "$CREATED" | sed -n 's/.*"title":"\([^"]*\)".*/\1/p')"
check "POST /api/tasks returns the generated id" "yes" "$([ -n "$ID" ] && echo yes || echo no)"
request -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"Second task"}'
SECOND_ID=$(echo "$LAST_BODY" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
check "POST /api/tasks returns 201" "201" "$LAST_STATUS"

echo ""
echo "-- Validation --"
check "POST with a blank title returns 400" "400" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"   "}')"
check "POST with a title over 200 chars returns 400" "400" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "{\"title\":\"$(printf 'a%.0s' {1..201})\"}")"
check "POST with a malformed body returns 400" "400" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{ nope }')"
request -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"Extra field","unknown":"x"}'
EXTRA_ID=$(echo "$LAST_BODY" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
check "POST with an unknown field is ignored" "201" "$LAST_STATUS"

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
echo "-- Priority --"
# A unique suffix keeps this run's tasks apart from any other task that
# happens to have the same title.
RUN_TAG="smoke-$$"
URGENT=$(body -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "{\"title\":\"Urgent $RUN_TAG\",\"priority\":\"HIGH\"}")
URGENT_ID=$(echo "$URGENT" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
check "POST with a priority returns it" "HIGH" "$(echo "$URGENT" | sed -n 's/.*"priority":"\([^"]*\)".*/\1/p')"
NORMAL=$(body -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d "{\"title\":\"Normal $RUN_TAG\"}")
NORMAL_ID=$(echo "$NORMAL" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
check "POST without a priority returns MEDIUM" "MEDIUM" "$(echo "$NORMAL" | sed -n 's/.*"priority":"\([^"]*\)".*/\1/p')"
check "POST with an unknown priority returns 400" "400" "$(status -X POST "$BASE/api/tasks" -H 'Content-Type: application/json' -d '{"title":"Broken","priority":"URGENT"}')"
check "GET /api/tasks?priority=HIGH returns 200" "200" "$(status "$BASE/api/tasks?priority=HIGH")"
check "GET /api/tasks?priority=URGENT returns 400" "400" "$(status "$BASE/api/tasks?priority=URGENT")"

echo ""
echo "-- Search --"
check "GET /api/tasks?search=Pending returns 200" "200" "$(status "$BASE/api/tasks?search=Pending")"
check "GET /api/tasks?search=Pending finds the task" "true" "$(body "$BASE/api/tasks?search=Pending" | grep -q 'Pending one' && echo true || echo false)"
check "GET /api/tasks?search=bicycle finds nothing" "false" "$(body "$BASE/api/tasks?search=bicycle" | grep -q 'Pending one' && echo true || echo false)"

echo ""
echo "-- Sort and pagination --"
check "GET /api/tasks?sort=TITLE returns 200" "200" "$(status "$BASE/api/tasks?sort=TITLE")"
check "GET /api/tasks?sort=random returns 400" "400" "$(status "$BASE/api/tasks?sort=random")"
check "GET /api/tasks?page=0&size=2 returns 200" "200" "$(status "$BASE/api/tasks?page=0&size=2")"
check "GET /api/tasks?size=2 limits the page" "2" "$(body "$BASE/api/tasks?page=0&size=2" | grep -o '"size":[0-9]*' | head -1 | sed 's/.*://')"
check "GET /api/tasks?size=500 clamps the size to 100" "100" "$(body "$BASE/api/tasks?page=0&size=500" | grep -o '"size":[0-9]*' | head -1 | sed 's/.*://')"

echo ""
echo "-- Priority ordering --"
# The most urgent priority must come first, not the alphabetically first one.
ORDERED_IDS=$(body "$BASE/api/tasks?sort=PRIORITY&size=100" | grep -o '"id":[0-9]*' | sed 's/.*://')
HIGH_POS=$(echo "$ORDERED_IDS" | grep -n "^$URGENT_ID$" | cut -d: -f1 | head -1)
LOW_POS=$(echo "$ORDERED_IDS" | grep -n "^$NORMAL_ID$" | cut -d: -f1 | head -1)
check "sort=PRIORITY puts HIGH before MEDIUM and LOW" "true" "$([ -n "$HIGH_POS" ] && [ -n "$LOW_POS" ] && [ "$HIGH_POS" -lt "$LOW_POS" ] && echo true || echo false)"

echo ""
echo "-- Statistics --"
check "GET /api/tasks/statistics returns 200" "200" "$(status "$BASE/api/tasks/statistics")"
check "GET /api/tasks/statistics reports the total" "true" "$(body "$BASE/api/tasks/statistics" | grep -q '"total"' && echo true || echo false)"
check "GET /api/tasks/statistics reports the completion" "true" "$(body "$BASE/api/tasks/statistics" | grep -q '"completionPercentage"' && echo true || echo false)"

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
echo "-- Cleanup --"
# Delete only the tasks this script created, so the database is left as it
# was found and the smoke test can be run again and again.
for task_id in "$ID" "$SECOND_ID" "$PENDING_ID" "$DONE_ID" "$URGENT_ID" "$NORMAL_ID" "$EXTRA_ID"; do
  if [ -n "$task_id" ]; then
    curl -sS -o /dev/null -X DELETE "$BASE/api/tasks/$task_id"
  fi
done
echo "  The tasks created by this script were deleted."

echo ""
echo "=============================================="
printf ' Result: %s passed, %s failed\n' "$PASS" "$FAIL"
echo "=============================================="
[ "$FAIL" -eq 0 ]
