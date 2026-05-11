#!/usr/bin/env bash
set -euo pipefail

TICKET_ID="${1:-}"

if [ -z "$TICKET_ID" ]; then
  echo "Usage: scripts/agent-ticket-patch.sh TODO-XXX"
  exit 1
fi

BACKLOG_FILE="tickets/backlog.md"
SOURCE_DIR="composeApp/src/commonMain/kotlin"
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

if [ ! -d "$SOURCE_DIR" ]; then
  echo "FAILED: $SOURCE_DIR not found."
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
Create, modify, and delete files as needed under $SOURCE_DIR.
Use the existing app structure and package names.
Keep the implementation minimal but real.
Do not mark the ticket done.
Do not commit changes.
Stop after code changes.

The backlog entry to implement is $TICKET_ID.
EOF
)"

echo "Asking Aider to implement $TICKET_ID..."

BEFORE_STATUS="$(git status --porcelain "$SOURCE_DIR" "$BACKLOG_FILE" || true)"

python3 -m aider \
  --model ollama_chat/qwen3:14b \
  --no-restore-chat-history \
  --chat-history-file /tmp/gaiapp-aider.chat.history.md \
  --input-history-file /tmp/gaiapp-aider.input.history \
  --yes-always \
  --no-auto-commits \
  --message "$AIDER_PROMPT" \
  "$SOURCE_DIR" \
  "$BACKLOG_FILE"

AFTER_STATUS="$(git status --porcelain "$SOURCE_DIR" "$BACKLOG_FILE" || true)"

if [ "$BEFORE_STATUS" = "$AFTER_STATUS" ]; then
  echo "FAILED: Aider did not change app source code under $SOURCE_DIR."
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
Modify files under $SOURCE_DIR or Gradle files only if needed.
Do not mark the ticket done.
Do not commit changes.
Stop after fixing the build.
EOF
)"

  python3 -m aider \
    --model ollama_chat/qwen3:14b \
    --no-restore-chat-history \
    --chat-history-file /tmp/gaiapp-aider.chat.history.md \
    --input-history-file /tmp/gaiapp-aider.input.history \
    --yes-always \
    --no-auto-commits \
    --message "$FIX_PROMPT" \
    "$SOURCE_DIR" \
    "$BACKLOG_FILE" \
    "build.gradle.kts" \
    "composeApp/build.gradle.kts" \
    "settings.gradle.kts"

  ./gradlew build
fi

echo "Marking $TICKET_ID done..."

sed -i.bak "s/^## $TICKET_ID:/## ${TICKET_ID/TODO/DONE}:/" "$BACKLOG_FILE"
rm -f "$BACKLOG_FILE.bak"

git add "$SOURCE_DIR" "$BACKLOG_FILE" build.gradle.kts composeApp/build.gradle.kts settings.gradle.kts 2>/dev/null || true

if git diff --cached --quiet; then
  echo "FAILED: No changes staged."
  exit 1
fi

git commit -m "Implement $TICKET_ID"

git push -u origin "$BRANCH_NAME"

if command -v gh >/dev/null 2>&1; then
  gh pr create \
    --base main \
    --head "$BRANCH_NAME" \
    --title "Implement $TICKET_ID" \
    --body "Implements $TICKET_ID from $BACKLOG_FILE."
else
  echo "gh not found; branch pushed but PR was not created."
fi

echo "Done: $TICKET_ID"