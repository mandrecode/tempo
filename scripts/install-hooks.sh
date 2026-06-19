#!/usr/bin/env bash
# Installs Git hooks from scripts/hooks/ into .git/hooks/
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOOKS_SRC="$REPO_ROOT/scripts/hooks"

GIT_DIR=$(cd "$REPO_ROOT" && git rev-parse --absolute-git-dir 2>/dev/null) || {
  echo "❌ Not a git repository: $REPO_ROOT" >&2
  exit 1
}
HOOKS_DST="$GIT_DIR/hooks"
mkdir -p "$HOOKS_DST"

for hook in "$HOOKS_SRC"/*; do
  hook_name="$(basename "$hook")"
  cp "$hook" "$HOOKS_DST/$hook_name"
  chmod +x "$HOOKS_DST/$hook_name"
  echo "✅ Installed $hook_name hook"
done
