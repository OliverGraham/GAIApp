#!/usr/bin/env bash
set -euo pipefail

BACKLOG_FILE="tickets/backlog.md"
PATCH_SCRIPT="scripts/agent-ticket-patch.sh"

if [ ! -f "$BACKLOG_FILE" ]; then
  echo "FAILED: $BACKLOG_FILE not found."
  exit 1
fi

if [ ! -x "$PATCH_SCRIPT" ]; then
  echo "FAILED: $PATCH_SCRIPT is not executable."
  echo "Run: chmod +x $PATCH_SCRIPT"
  exit 1
fi

while true; do
  TICKET_ID="$(grep -m 1 '^## TODO-[0-9][0-9][0-9]:' "$BACKLOG_FILE" | sed 's/^## \(TODO-[0-9][0-9][0-9]\):.*/\1/')"

  if [ -z "$TICKET_ID" ]; then
    echo "No TODO tickets left in $BACKLOG_FILE."
    exit 0
  fi

  echo "Running agent for next ticket: $TICKET_ID"

  "$PATCH_SCRIPT" "$TICKET_ID"

  echo "Completed $TICKET_ID. Returning to main before checking next ticket..."

  git checkout main
  git pull origin main
done