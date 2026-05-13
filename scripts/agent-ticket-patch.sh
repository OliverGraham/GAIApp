#!/usr/bin/env bash
set -euo pipefail

BACKLOG_FILE="tickets/backlog.md"
DONE_TICKETS_FILE=".agent/done-tickets.txt"
MODEL="gemini/gemini-2.5-flash"

if [ ! -f "$BACKLOG_FILE" ]; then
  echo "FAILED: $BACKLOG_FILE not found."
  exit 1
fi

TICKET_ID="$(grep -m 1 '^## TODO-[0-9][0-9][0-9]:' "$BACKLOG_FILE" | sed 's/^## \(TODO-[0-9][0-9][0-9]\):.*/\1/')"

if [ -z "$TICKET_ID" ]; then
  echo "No TODO tickets left in $BACKLOG_FILE."
  exit 0
fi

if [ -z "${GEMINI_API_KEY:-}" ]; then
  echo "FAILED: GEMINI_API_KEY is not set."
  exit 1
fi

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BRANCH_NAME="agent/$(echo "$TICKET_ID" | tr '[:upper:]' '[:lower:]')-$TIMESTAMP"

AIDER_LOG="/tmp/gaiapp-aider-$TICKET_ID.log"
BUILD_LOG="/tmp/gaiapp-gradle-build-$TICKET_ID.log"
SPOTLESS_LOG="/tmp/gaiapp-spotless-$TICKET_ID.log"

echo "Preparing branch for $TICKET_ID..."

git checkout main
git pull --ff-only origin main
git checkout -b "$BRANCH_NAME"

mkdir -p "$(dirname "$DONE_TICKETS_FILE")"
touch "$DONE_TICKETS_FILE"

if [ ! -f .gitignore ]; then
  touch .gitignore
fi

if ! grep -qxF ".aider*" .gitignore; then
  echo ".aider*" >> .gitignore
fi

if ! grep -qxF "aider.chat.history.md" .gitignore; then
  echo "aider.chat.history.md" >> .gitignore
fi

get_aider_files() {
  git ls-files \
    "*.kt" \
    "*.kts" \
    "*.toml" \
    "gradle.properties" \
    "$BACKLOG_FILE" \
    "$DONE_TICKETS_FILE" \
    ".gitignore"
}

rate_limit_seen() {
  grep -Ei 'RateLimitError|RESOURCE_EXHAUSTED|quota|rate limited|GenerateRequestsPerDay|GenerateRequestsPerMinute|requests per minute|requests per day' "$AIDER_LOG" >/dev/null 2>&1
}

AIDER_FILES="$(get_aider_files)"

if [ -z "$AIDER_FILES" ]; then
  echo "FAILED: No editable project files found for Aider."
  exit 1
fi

AIDER_PROMPT="$(cat <<EOF
Implement $TICKET_ID from $BACKLOG_FILE.

Edit repository files directly.
Do not ask follow-up questions.
Do not ask me to add files.
Do not only describe changes.
Make the best reasonable implementation choice if there is ambiguity.
Use the Kotlin Multiplatform project as needed, including commonMain, androidMain, iosMain, Gradle files, version catalogs, and shared app wiring.
If you need to use icons in any UI, draw them yourself, do not try to import icons.
If you make a @Preview for a @Composable function, use import androidx.compose.ui.tooling.preview.Preview
Do not change versions in versions.toml if they are working. Do not change the Room version for example.
Do not touch or replace versions in build.gradle.kts nor in libs.versions.toml.
Do not add any new versions or linbraries for sql for iOS. For everything related to Room, it is working perfectly and you should not change anything related to Room for toml versions or in build.gradle.kts
Use the existing package name com.gainus.gaiapp.
Keep the implementation minimal but real.
Do not mark the ticket done.
Do not update $DONE_TICKETS_FILE.
Do not commit changes.
Stop after code changes.

The backlog entry to implement is $TICKET_ID.
EOF
)"

echo "Running Aider once for $TICKET_ID with $MODEL..."
rm -f "$AIDER_LOG"

set +e
python3 -m aider $AIDER_FILES \
  --model "$MODEL" \
  --edit-format diff \
  --no-restore-chat-history \
  --chat-history-file /tmp/gaiapp-aider.chat.history.md \
  --input-history-file /tmp/gaiapp-aider.input.history \
  --yes-always \
  --no-auto-commits \
  --message "$AIDER_PROMPT" 2>&1 | tee "$AIDER_LOG"
AIDER_STATUS="${PIPESTATUS[0]}"
set -e

if rate_limit_seen; then
  echo "FAILED: Aider hit a Gemini quota/rate limit."
  echo "Aider log: $AIDER_LOG"
  exit 1
fi

if [ "$AIDER_STATUS" -ne 0 ]; then
  echo "FAILED: Aider exited with status $AIDER_STATUS."
  echo "Aider log: $AIDER_LOG"
  exit 1
fi

if git diff --quiet && git diff --cached --quiet; then
  echo "FAILED: Aider did not change project files."
  echo "Aider log: $AIDER_LOG"
  exit 1
fi

echo "Running Spotless apply if available..."

set +e
./gradlew spotlessApply > "$SPOTLESS_LOG" 2>&1
SPOTLESS_STATUS="$?"
set -e

if [ "$SPOTLESS_STATUS" -eq 0 ]; then
  echo "Spotless apply completed."
elif grep -Ei "Task 'spotlessApply' not found|Task \"spotlessApply\" not found|Cannot locate tasks that match 'spotlessApply'|task .*spotlessApply.* not found" "$SPOTLESS_LOG" >/dev/null 2>&1; then
  echo "Spotless task not found. Skipping formatting repair."
else
  echo "FAILED: Spotless apply failed."
  echo "Spotless log: $SPOTLESS_LOG"
  tail -n 200 "$SPOTLESS_LOG"
  exit 1
fi

echo "Running Gradle build once..."

if ! ./gradlew build > "$BUILD_LOG" 2>&1; then
  echo "FAILED: Gradle build failed."
  echo "Build log: $BUILD_LOG"
  tail -n 200 "$BUILD_LOG"
  exit 1
fi

echo "Gradle build passed."
echo "Marking $TICKET_ID done..."

sed -i.bak "s/^## $TICKET_ID:/## ${TICKET_ID/TODO/DONE}:/" "$BACKLOG_FILE"
rm -f "$BACKLOG_FILE.bak"

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