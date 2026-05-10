#!/usr/bin/env bash
set -euo pipefail

MODEL="${MODEL:-qwen3:14b}"
BASE_BRANCH="${BASE_BRANCH:-main}"
BACKLOG_FILE="${BACKLOG_FILE:-tickets/backlog.md}"
TICKET="${1:-}"

if [ -z "$TICKET" ]; then
  echo "Usage: MODEL=\"qwen3:14b\" scripts/agent-ticket-patch.sh TODO-001"
  exit 2
fi

if [ ! -f "$BACKLOG_FILE" ]; then
  echo "FAILED: Backlog file not found: $BACKLOG_FILE"
  exit 3
fi

if ! command -v ollama >/dev/null 2>&1; then
  echo "FAILED: ollama command not found."
  exit 4
fi

if ! grep -q "## $TICKET:" "$BACKLOG_FILE"; then
  echo "FAILED: Ticket not found in $BACKLOG_FILE: $TICKET"
  exit 5
fi

TICKET_TEXT="$(
  awk -v ticket="$TICKET" '
    $0 ~ "^## " ticket ":" { found = 1 }
    found && /^## / && $0 !~ "^## " ticket ":" { exit }
    found { print }
  ' "$BACKLOG_FILE"
)"

if [ -z "$TICKET_TEXT" ]; then
  echo "FAILED: Could not extract ticket text for $TICKET."
  exit 6
fi

git fetch origin "$BASE_BRANCH"
git checkout "$BASE_BRANCH"
git pull --ff-only origin "$BASE_BRANCH"

SLUG="$(echo "$TICKET" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-|-$//g')"
BRANCH="agent/${SLUG}-$(date +%Y%m%d-%H%M%S)"
git checkout -b "$BRANCH"

OUT_DIR="composeApp/src/commonMain/kotlin/com/gainus/gaiapp"
OUT_FILE="$OUT_DIR/Task.kt"
TMP_FILE="$(mktemp)"

mkdir -p "$OUT_DIR"

ollama run "$MODEL" <<EOF > "$TMP_FILE"
/no_think

You are implementing one narrow ticket in a Kotlin Multiplatform Compose project.

Project package:
com.gainus.gaiapp

Ticket:
$TICKET_TEXT

Return ONLY the complete Kotlin source code for this file:
$OUT_FILE

Rules:
- No markdown.
- No code fences.
- No explanation.
- No reasoning.
- No analysis.
- No notes before or after the code.
- The first line of your answer must be: package com.gainus.gaiapp
- The file must compile.
- Use package com.gainus.gaiapp.
- Implement only this ticket.
- Do not include TODO comments.
- Do not create placeholders.
EOF

python3 - "$TMP_FILE" "$OUT_FILE" <<'PY'
import re
import sys

tmp_file = sys.argv[1]
out_file = sys.argv[2]

raw = open(tmp_file, "rb").read().decode("utf-8", errors="ignore")

# Remove ANSI escape sequences.
raw = re.sub(r"\x1b\[[0-9;?]*[A-Za-z]", "", raw)

# Remove simple terminal backspace artifacts.
while "\b" in raw:
    raw = re.sub(r".\b", "", raw)

matches = list(re.finditer(r"(?m)^package com\.gainus\.gaiapp\s*$", raw))

if not matches:
    print("FAILED: Model output did not contain package declaration.")
    print(raw)
    sys.exit(10)

# Use the final package block because Qwen may emit a draft, then reasoning, then final code.
code = raw[matches[-1].start():].strip() + "\n"

bad_markers = [
    "\n```",
    "\n...done thinking",
    "\nI think",
    "\nLet me",
    "\nThis code",
    "\nExplanation",
    "\nNotes",
]

for marker in bad_markers:
    idx = code.find(marker)
    if idx != -1:
        code = code[:idx].rstrip() + "\n"

open(out_file, "w", encoding="utf-8").write(code)
PY

if [ ! -s "$OUT_FILE" ]; then
  echo "FAILED: Model did not produce Kotlin file content."
  echo "Raw output:"
  cat "$TMP_FILE"
  exit 10
fi

if [ "$(grep -c "^package com\.gainus\.gaiapp" "$OUT_FILE")" -ne 1 ]; then
  echo "FAILED: Output contains multiple package declarations."
  cat "$OUT_FILE"
  exit 11
fi

if ! grep -q "^package com\.gainus\.gaiapp" "$OUT_FILE"; then
  echo "FAILED: Output has wrong or missing package."
  cat "$OUT_FILE"
  exit 12
fi

if ! grep -q "data class Task" "$OUT_FILE"; then
  echo "FAILED: Output does not contain Task data class."
  cat "$OUT_FILE"
  exit 13
fi

if ! grep -q "val id" "$OUT_FILE"; then
  echo "FAILED: Task is missing id field."
  cat "$OUT_FILE"
  exit 14
fi

if ! grep -q "val title" "$OUT_FILE"; then
  echo "FAILED: Task is missing title field."
  cat "$OUT_FILE"
  exit 15
fi

if ! grep -q "isDone" "$OUT_FILE"; then
  echo "FAILED: Task is missing isDone field."
  cat "$OUT_FILE"
  exit 16
fi

if ! grep -Eq "sample|Sample|createSample" "$OUT_FILE"; then
  echo "FAILED: Output does not appear to include sample task helper."
  cat "$OUT_FILE"
  exit 17
fi

if grep -Eiq "TODO|placeholder|not implemented|throw NotImplementedError" "$OUT_FILE"; then
  echo "FAILED: Placeholder output detected."
  cat "$OUT_FILE"
  exit 18
fi

if grep -Eiq "wait,|probably|better|simplicity|straightforward|I think|Let me|done thinking|reasoning|analysis|I need|No persistence|That should meet" "$OUT_FILE"; then
  echo "FAILED: Model reasoning leaked into source file."
  cat "$OUT_FILE"
  exit 19
fi

cat > AGENT_RESULT.md <<EOF
# Agent Result

Ticket implemented: $TICKET

Model: $MODEL

Files changed:
- $OUT_FILE

Checks run:
- Gradle build attempted by script
EOF

if [ -x ./gradlew ]; then
  ./gradlew --no-daemon build
else
  echo "WARNING: ./gradlew not found or not executable; skipping build."
fi

if git diff --quiet; then
  echo "FAILED: No changes produced."
  exit 20
fi

git add -A
git commit -m "Implement $TICKET"
git push -u origin "$BRANCH"

if command -v gh >/dev/null 2>&1; then
  gh pr create \
    --base "$BASE_BRANCH" \
    --head "$BRANCH" \
    --title "Implement $TICKET" \
    --body-file AGENT_RESULT.md
else
  echo "WARNING: gh command not found; branch was pushed but PR was not created."
fi