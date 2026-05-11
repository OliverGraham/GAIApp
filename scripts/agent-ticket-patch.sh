#!/usr/bin/env bash
set -euo pipefail

TICKET_ID="${1:-}"

if [ -z "$TICKET_ID" ]; then
  echo "Usage: scripts/agent-ticket-patch.sh TODO-XXX"
  exit 1
fi

if [ -z "${GEMINI_API_KEY:-}" ]; then
  echo "FAILED: GEMINI_API_KEY is not set."
  exit 1
fi

BACKLOG_FILE="tickets/backlog.md"
DONE_TICKETS_FILE=".agent/done-tickets.txt"
MODEL="gemini/gemini-2.5-flash"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BRANCH_NAME="agent/$(echo "$TICKET_ID" | tr '[:upper:]' '[:lower:]')-$TIMESTAMP"

if [ ! -f "$BACKLOG_FILE" ]; then
  echo "FAILED: $BACKLOG_FILE not found."
  exit 1
fi

if ! grep -q "^## $TICKET_ID:" "$BACKLOG_FILE"; then
  echo "FAILED: $TICKET_ID not found in $BACKLOG_FILE."
  exit 1
fi

echo "Preparing branch for $TICKET_ID..."

git checkout main
git pull origin main
git checkout -b "$BRANCH_NAME"

AIDER_PROMPT="$(cat <<EOF
Implement $TICKET_ID from $BACKLOG_FILE.

Edit the repository directly.
Do not ask me to add files.
Do not only describe changes.
Use the whole Kotlin Multiplatform project as needed, including commonMain, androidMain, iosMain, Gradle files, and shared app wiring.
Use the existing package name com.gainus.gaiapp.
Keep the implementation minimal but real.
Do not mark the ticket done.
Do not commit changes.
Stop after code changes.

The backlog entry to implement is $TICKET_ID.
EOF
)"

echo "Asking Aider to implement $TICKET_ID with $MODEL..."

python3 -m aider . \
  --model "$MODEL" \
  --no-restore-chat-history \
  --chat-history-file /tmp/gaiapp-aider.chat.history.md \
  --input-history-file /tmp/gaiapp-aider.input.history \
  --yes-always \
  --no-auto-commits \
  --message "$AIDER_PROMPT"

if git diff --quiet && git diff --cached --quiet; then
  echo "FAILED: Aider did not change project files."
  exit 1
fi

echo "Running Gradle build..."

if ! ./gradlew build; then
  echo "Gradle build failed. Asking Aider to fix it..."

  FIX_PROMPT="$(cat <<EOF
The Gradle build failed after implementing $TICKET_ID.

Fix the build by editing the repository directly.
Do not ask me to add files.
Do not only describe changes.
Use the whole Kotlin Multiplatform project as needed, including commonMain, androidMain, iosMain, Gradle files, and shared app wiring.
Do not mark the ticket done.
Do not commit changes.
Stop after fixing the build.
EOF
)"

  python3 -m aider . \
    --model "$MODEL" \
    --no-restore-chat-history \
    --chat-history-file /tmp/gaiapp-aider.chat.history.md \
    --input-history-file /tmp/gaiapp-aider.input.history \
    --yes-always \
    --no-auto-commits \
    --message "$FIX_PROMPT"

  ./gradlew build
fi

echo "Marking $TICKET_ID done..."

sed -i.bak "s/^## $TICKET_ID:/## ${TICKET_ID/TODO/DONE}:/" "$BACKLOG_FILE"
rm -f "$BACKLOG_FILE.bak"

mkdir -p "$(dirname "$DONE_TICKETS_FILE")"
touch "$DONE_TICKETS_FILE"

if ! grep -qx "$TICKET_ID" "$DONE_TICKETS_FILE"; then
  echo "$TICKET_ID" >> "$DONE_TICKETS_FILE"
fi

git add .

git reset -- .aider* aider.chat.history.md .aider.chat.history.md 2>/dev/null || true

if git diff --cached --quiet; then
  echo "FAILED: No changes staged."
  exit 1
fi

git commit -m "Implement $TICKET_ID"

git push -u origin "$BRANCH_NAME"

if command -v gh >/dev/null 2>&1; then
  PR_URL="$(gh pr create \
    --base main \
    --head "$BRANCH_NAME" \
    --title "Implement $TICKET_ID" \
    --body "Implements $TICKET_ID from $BACKLOG_FILE.")"

  echo "$PR_URL"

  gh pr merge "$BRANCH_NAME" \
    --auto \
    --squash \
    --delete-branch
else
  echo "gh not found; branch pushed but PR was not created."
fi

echo "Done: $TICKET_ID"