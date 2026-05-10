#!/usr/bin/env bash
set -euo pipefail

BACKLOG_FILE="${BACKLOG_FILE:-tickets/backlog.md}"
MODEL="${MODEL:-qwen3:14b}"

if [ ! -f "$BACKLOG_FILE" ]; then
  echo "FAILED: Backlog file not found: $BACKLOG_FILE"
  exit 2
fi

TICKET="$(
  awk '
    /^## TODO-[0-9]+:/ {
      line = $0
      sub(/^## /, "", line)
      sub(/:.*/, "", line)
      print line
      exit
    }
  ' "$BACKLOG_FILE"
)"

if [ -z "$TICKET" ]; then
  echo "FAILED: No TODO ticket found in $BACKLOG_FILE"
  exit 3
fi

echo "Running agent for next ticket: $TICKET"

MODEL="$MODEL" scripts/agent-ticket-patch.sh "$TICKET"