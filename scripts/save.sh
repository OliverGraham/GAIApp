#!/bin/bash
set -e

MESSAGE="${1:-update project}"

git add .

if git diff --cached --quiet; then
  echo "No changes to commit."
  exit 0
fi

git commit -m "$MESSAGE"

BRANCH=$(git rev-parse --abbrev-ref HEAD)

git push -u origin "$BRANCH"
