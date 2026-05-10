#!/usr/bin/env bash
set -euo pipefail

MODEL="${MODEL:-qwen3:14b}"
BASE_BRANCH="${BASE_BRANCH:-main}"
BACKLOG_FILE="${BACKLOG_FILE:-tickets/backlog.md}"
DONE_FILE="${DONE_FILE:-.agent/done-tickets.txt}"
MAX_FIX_ATTEMPTS="${MAX_FIX_ATTEMPTS:-0}"
TICKET="${1:-}"

if [ -z "$TICKET" ]; then
  echo "Usage: MODEL=\"qwen3:14b\" scripts/agent-ticket-patch.sh TODO-001"
  exit 1
fi

if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "FAILED: Working tree has uncommitted changes. Commit or stash them first."
  exit 1
fi

run_aider() {
  python3 -m aider "$@"
}

record_ticket_done_locally() {
  mkdir -p "$(dirname "$DONE_FILE")"
  grep -qxF "$TICKET" "$DONE_FILE" 2>/dev/null || echo "$TICKET" >> "$DONE_FILE"
}

mark_ticket_done() {
  python3 - "$BACKLOG_FILE" "$TICKET" <<'PY'
import re
import sys

path, ticket = sys.argv[1], sys.argv[2]

with open(path, "r", encoding="utf-8") as f:
    text = f.read()

done_ticket = ticket.replace("TODO-", "DONE-", 1)

new_text, count = re.subn(
    rf"^## {re.escape(ticket)}:",
    f"## {done_ticket}:",
    text,
    count=1,
    flags=re.M,
)

if count != 1:
    print(f"FAILED: Could not mark ticket done: {ticket}")
    sys.exit(1)

with open(path, "w", encoding="utf-8") as f:
    f.write(new_text)
PY
}

TICKET_TEXT="$(
  awk -v ticket="$TICKET" '
    $0 ~ "^## " ticket ":" { found = 1 }
    found && /^## / && $0 !~ "^## " ticket ":" { exit }
    found { print }
  ' "$BACKLOG_FILE"
)"

if [ -z "$TICKET_TEXT" ]; then
  echo "FAILED: Could not extract ticket text for $TICKET."
  exit 1
fi

git fetch origin "$BASE_BRANCH"
git checkout "$BASE_BRANCH"
git pull --ff-only origin "$BASE_BRANCH"

SLUG="$(echo "$TICKET" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-|-$//g')"
BRANCH="agent/${SLUG}-$(date +%Y%m%d-%H%M%S)"
git checkout -b "$BRANCH"

AIDER_MODEL="ollama_chat/$MODEL"

IMPLEMENT_PROMPT="$(
  cat <<EOF
Implement this ticket in the Kotlin Multiplatform Compose app.

Ticket:
$TICKET_TEXT

Rules:
- Edit the repository files directly.
- Keep the change minimal.
- Do not commit.
- Do not add persistence unless the ticket asks for it.
- Do not add new dependencies unless the ticket asks for them.
- Do not create placeholder classes.
- Do not leave TODO comments.
- You must change app source code under composeApp/src/commonMain.
EOF
)"

echo "Asking Aider to implement $TICKET..."

run_aider \
  --model "$AIDER_MODEL" \
  --no-show-model-warnings \
  --yes-always \
  --no-auto-commits \
  --message "$IMPLEMENT_PROMPT"

if git diff --quiet -- composeApp/src/commonMain; then
  echo "FAILED: Aider did not change app source code under composeApp/src/commonMain."
  exit 1
fi

BUILD_LOG="$(mktemp)"
attempt=0

while true; do
  if ./gradlew --no-daemon build >"$BUILD_LOG" 2>&1; then
    break
  fi

  attempt=$((attempt + 1))

  if [ "$MAX_FIX_ATTEMPTS" != "0" ] && [ "$attempt" -gt "$MAX_FIX_ATTEMPTS" ]; then
    echo "FAILED: Gradle build still failing after $MAX_FIX_ATTEMPTS fix attempts."
    cat "$BUILD_LOG"
    exit 1
  fi

  echo "Build failed. Asking Aider to fix it. Attempt $attempt."

  FIX_PROMPT="$(
    cat <<EOF
The Gradle build failed while implementing $TICKET.

Fix the build without expanding the ticket scope.

Build output:
$(tail -n 200 "$BUILD_LOG")
EOF
  )"

  run_aider \
    --model "$AIDER_MODEL" \
    --no-show-model-warnings \
    --yes-always \
    --no-auto-commits \
    --message "$FIX_PROMPT"

  if git diff --quiet -- composeApp/src/commonMain; then
    echo "FAILED: Aider did not change app source code under composeApp/src/commonMain."
    exit 1
  fi
done

cat > AGENT_RESULT.md <<EOF
# Agent Result

Ticket implemented: $TICKET

Model: $MODEL

Checks run:
- Gradle build
EOF

git add -A
git commit -m "Implement $TICKET"
git push -u origin "$BRANCH"

gh pr create \
  --base "$BASE_BRANCH" \
  --head "$BRANCH" \
  --title "Implement $TICKET" \
  --body-file AGENT_RESULT.md

mark_ticket_done

git add "$BACKLOG_FILE"
git commit -m "Mark $TICKET done"
git push

record_ticket_done_locally