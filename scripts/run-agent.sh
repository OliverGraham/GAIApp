#!/usr/bin/env bash
set -euo pipefail

BACKLOG_FILE="${BACKLOG_FILE:-tickets/backlog.md}"
DONE_FILE="${DONE_FILE:-.agent/done-tickets.txt}"
MODEL="${MODEL:-qwen3:14b}"

TICKET="$(
  awk -v done_file="$DONE_FILE" '
    BEGIN {
      while ((getline line < done_file) > 0) {
        done[line] = 1
      }
    }

    /^## TODO-[0-9]+:/ {
      line = $0
      sub(/^## /, "", line)
      sub(/:.*/, "", line)

      if (!(line in done)) {
        print line
        exit
      }
    }
  ' "$BACKLOG_FILE"
)"

if [ -z "$TICKET" ]; then
  echo "FAILED: No open TODO ticket found in $BACKLOG_FILE"
  exit 1
fi

echo "Running agent for next ticket: $TICKET"

MODEL="$MODEL" BACKLOG_FILE="$BACKLOG_FILE" DONE_FILE="$DONE_FILE" scripts/agent-ticket-patch.sh "$TICKET"