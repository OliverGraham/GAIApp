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
FIX_MODEL="gemini/gemini-2.5-flash"
MAX_IMPLEMENT_ATTEMPTS=3
MAX_FIX_ATTEMPTS=5
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BRANCH_NAME="agent/$(echo "$TICKET_ID" | tr '[:upper:]' '[:lower:]')-$TIMESTAMP"
BUILD_LOG="/tmp/gaiapp-gradle-build-$TICKET_ID.log"
TASKS_LOG="/tmp/gaiapp-gradle-tasks-$TICKET_ID.log"
SPOTLESS_LOG="/tmp/gaiapp-spotless-$TICKET_ID.log"

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

mkdir -p "$(dirname "$DONE_TICKETS_FILE")"
touch "$DONE_TICKETS_FILE"

get_aider_files() {
  git ls-files \
    "*.kt" \
    "*.kts" \
    "*.toml" \
    "gradle.properties" \
    "$BACKLOG_FILE" \
    "$DONE_TICKETS_FILE"
}

gradle_related_files_changed() {
  git diff --name-only | grep -E '(^gradle/libs\.versions\.toml$|build\.gradle\.kts$|settings\.gradle\.kts$|gradle\.properties$)' >/dev/null 2>&1
}

spotless_available() {
  ./gradlew tasks --all > "$TASKS_LOG" 2>&1
  grep -q "spotlessApply" "$TASKS_LOG"
}

run_spotless_if_available() {
  if spotless_available; then
    echo "Running Spotless apply..."
    ./gradlew spotlessApply > "$SPOTLESS_LOG" 2>&1
    echo "Spotless apply completed."
    return 0
  fi

  echo "Spotless task not found. Skipping formatting repair."
  return 0
}

build_failure_looks_like_formatting() {
  grep -Ei 'spotless|ktlint|format|formatting|lint found errors|run.*spotlessApply|spotlessApply' "$BUILD_LOG" >/dev/null 2>&1
}

run_aider() {
  PROMPT="$1"
  SELECTED_MODEL="$2"
  AIDER_FILES="$(get_aider_files)"

  if [ -z "$AIDER_FILES" ]; then
    echo "FAILED: No editable project files found for Aider."
    exit 1
  fi

  python3 -m aider $AIDER_FILES \
    --model "$SELECTED_MODEL" \
    --edit-format diff \
    --no-restore-chat-history \
    --chat-history-file /tmp/gaiapp-aider.chat.history.md \
    --input-history-file /tmp/gaiapp-aider.input.history \
    --yes-always \
    --no-auto-commits \
    --message "$PROMPT"
}

IMPLEMENT_ATTEMPT=1

while [ "$IMPLEMENT_ATTEMPT" -le "$MAX_IMPLEMENT_ATTEMPTS" ]; do
  echo "Asking Aider to implement $TICKET_ID with $MODEL. Attempt $IMPLEMENT_ATTEMPT of $MAX_IMPLEMENT_ATTEMPTS..."

  AIDER_PROMPT="$(cat <<EOF
Implement $TICKET_ID from $BACKLOG_FILE.

Edit the repository files directly.
Do not ask follow-up questions.
Do not ask me to add files.
Do not only describe changes.
If there is ambiguity, make the best reasonable implementation choice and edit files.
If a previous attempt partially failed, re-apply the needed changes directly.
Use the Kotlin Multiplatform project as needed, including commonMain, androidMain, iosMain, Gradle files, version catalogs, and shared app wiring.
Use the existing package name com.gainus.gaiapp.
Keep the implementation minimal but real.
Do not mark the ticket done.
Do not update $DONE_TICKETS_FILE.
Do not commit changes.
Stop after code changes.

The backlog entry to implement is $TICKET_ID.
EOF
)"

  run_aider "$AIDER_PROMPT" "$MODEL" || true

  if ! git diff --quiet || ! git diff --cached --quiet; then
    break
  fi

  IMPLEMENT_ATTEMPT=$((IMPLEMENT_ATTEMPT + 1))
done

if git diff --quiet && git diff --cached --quiet; then
  echo "FAILED: Aider did not change project files after $MAX_IMPLEMENT_ATTEMPTS attempts."
  exit 1
fi

FIX_ATTEMPT=0

while true; do
  if gradle_related_files_changed; then
    echo "Gradle-related files changed. Running Gradle configuration check..."

    if ! ./gradlew tasks > "$BUILD_LOG" 2>&1; then
      FIX_ATTEMPT=$((FIX_ATTEMPT + 1))

      echo "Gradle configuration failed. Fix attempt $FIX_ATTEMPT of $MAX_FIX_ATTEMPTS."
      tail -n 160 "$BUILD_LOG"

      if [ "$FIX_ATTEMPT" -gt "$MAX_FIX_ATTEMPTS" ]; then
        echo "FAILED: Gradle configuration still fails after $MAX_FIX_ATTEMPTS fix attempts."
        echo "Build log: $BUILD_LOG"
        exit 1
      fi

      FIX_PROMPT="$(cat <<EOF
The Gradle configuration failed after implementing $TICKET_ID.

Fix the Gradle files by editing repository files directly.
Do not ask follow-up questions.
Do not ask me to add files.
Do not only describe changes.
If a version catalog entry is wrong, fix gradle/libs.versions.toml.
If a plugin alias is wrong, fix the plugin declaration.
If a dependency alias is wrong, fix the dependency declaration.
If a plugin or dependency is unnecessary, remove it.
Keep the Kotlin Multiplatform project valid.
Do not mark the ticket done.
Do not update $DONE_TICKETS_FILE.
Do not commit changes.
Stop after fixing the Gradle configuration.

Recent Gradle failure output:

$(tail -n 180 "$BUILD_LOG")
EOF
)"

      run_aider "$FIX_PROMPT" "$FIX_MODEL" || true
      continue
    fi
  fi

  run_spotless_if_available

  echo "Running Gradle build..."

  if ./gradlew build > "$BUILD_LOG" 2>&1; then
    echo "Gradle build passed."
    break
  fi

  if build_failure_looks_like_formatting; then
    echo "Build failure appears formatting-related. Running Spotless apply before using Aider..."
    tail -n 160 "$BUILD_LOG"

    run_spotless_if_available

    echo "Retrying Gradle build after Spotless..."

    if ./gradlew build > "$BUILD_LOG" 2>&1; then
      echo "Gradle build passed after Spotless."
      break
    fi
  fi

  FIX_ATTEMPT=$((FIX_ATTEMPT + 1))

  echo "Gradle build failed. Fix attempt $FIX_ATTEMPT of $MAX_FIX_ATTEMPTS."
  tail -n 160 "$BUILD_LOG"

  if [ "$FIX_ATTEMPT" -gt "$MAX_FIX_ATTEMPTS" ]; then
    echo "FAILED: Gradle build still fails after $MAX_FIX_ATTEMPTS fix attempts."
    echo "Build log: $BUILD_LOG"
    exit 1
  fi

  FIX_PROMPT="$(cat <<EOF
The Gradle build failed after implementing $TICKET_ID.

Fix the build by editing repository files directly.
Do not ask follow-up questions.
Do not ask me to add files.
Do not only describe changes.
Do not manually fix formatting-only errors if Spotless can fix them.
If imports are missing, add the required imports.
If an import is unresolved or unused, remove it unless the symbol is actually used and required.
Do not add speculative imports.
Do not import symbols that are not already available in the project dependencies.
If symbols are unresolved, inspect the existing project structure and correct the references.
If package names are wrong, use com.gainus.gaiapp.
If Gradle files or version catalog entries are wrong, fix them.
If a dependency is unnecessary, remove it.
If there is ambiguity, make the best reasonable implementation choice and edit files.
Use the Kotlin Multiplatform project as needed, including commonMain, androidMain, iosMain, Gradle files, version catalogs, and shared app wiring.
Do not mark the ticket done.
Do not update $DONE_TICKETS_FILE.
Do not commit changes.
Stop after fixing the build.

Recent Gradle failure output:

$(tail -n 180 "$BUILD_LOG")
EOF
)"

  run_aider "$FIX_PROMPT" "$FIX_MODEL" || true
done

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