#!/usr/bin/env bash
# generate-whatsnew.sh
#
# Play Store "What's new" notes, written by hand per release and per locale
# in distribution/whatsnew/<version>/whatsnew-<locale>. See
# distribution/whatsnew/README.md for the flow.
#
# Usage:
#   ./scripts/generate-whatsnew.sh <version>
#       Validate the notes for <version> and copy them to build/whatsnew/
#       (override with WHATSNEW_OUT_DIR), the directory publish.yml uploads.
#   ./scripts/generate-whatsnew.sh --check <version>
#       Validate the notes for <version> without writing anything.
#   ./scripts/generate-whatsnew.sh --check-all
#       Validate every version directory under distribution/whatsnew/.
#   ./scripts/generate-whatsnew.sh --draft <version>
#       Write a starting en-US draft for <version> from CHANGELOG.md, with
#       non-user-facing entries filtered out. Never overwrites, never writes
#       es-ES: the draft is for a person to rewrite and translate.
#
# A release fails validation when a locale's file is missing or empty, is over
# Play's 500-character limit, contains GitHub issue/PR links, or when es-ES is
# identical to en-US (an untranslated copy).

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CHANGELOG="$REPO_ROOT/CHANGELOG.md"
NOTES_ROOT="$REPO_ROOT/distribution/whatsnew"
OUT_DIR="${WHATSNEW_OUT_DIR:-$REPO_ROOT/build/whatsnew}"
LOCALES=(en-US es-ES)
MAX_CHARS=500

usage() {
  sed -n '2,24p' "$0" | sed -E 's/^# ?//' >&2
  exit 2
}

# Character count as Play counts it (code points, not bytes), independent of
# the runner's locale.
char_count() {
  python3 -c 'import sys; print(len(sys.stdin.read()))' < "$1"
}

require_version() {
  if [[ ! "${1:-}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: expected a version like 1.18.1, got '${1:-}'" >&2
    exit 2
  fi
}

# Validate one version's notes. Prints every problem, returns 1 if any.
check_version() {
  local version="$1"
  local dir="$NOTES_ROOT/$version"
  local failed=0

  for locale in "${LOCALES[@]}"; do
    local file="$dir/whatsnew-$locale"
    local rel="${file#"$REPO_ROOT"/}"
    if [ ! -f "$file" ]; then
      echo "::error file=$rel::Missing $locale release notes for $version"
      failed=1
      continue
    fi
    if ! grep -q '[^[:space:]]' "$file"; then
      echo "::error file=$rel::$locale release notes for $version are empty"
      failed=1
      continue
    fi
    local count
    count=$(char_count "$file")
    if [ "$count" -gt $MAX_CHARS ]; then
      echo "::error file=$rel::$locale release notes for $version are $count characters; Play allows $MAX_CHARS"
      failed=1
    fi
    if grep -n -E 'https://github\.com/[^/]+/[^/]+/(issues|pull)/[0-9]+' "$file"; then
      echo "::error file=$rel::$locale release notes for $version contain GitHub issue/PR links"
      failed=1
    fi
  done

  local en="$dir/whatsnew-en-US"
  local es="$dir/whatsnew-es-ES"
  if [ -f "$en" ] && [ -f "$es" ] && cmp -s "$en" "$es"; then
    echo "::error file=${es#"$REPO_ROOT"/}::es-ES release notes for $version are identical to en-US; translate them"
    failed=1
  fi

  if [ "$failed" -ne 0 ]; then
    echo "Release notes for $version are not ready. See distribution/whatsnew/README.md" >&2
    return 1
  fi
  echo "Release notes for $version: OK"
}

check_all() {
  local failed=0
  local found=0
  for dir in "$NOTES_ROOT"/*/; do
    [ -d "$dir" ] || continue
    local version
    version=$(basename "$dir")
    if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
      echo "::error file=distribution/whatsnew/$version::Unexpected directory; expected a version like 1.18.1"
      failed=1
      continue
    fi
    found=1
    check_version "$version" || failed=1
  done
  [ "$found" -eq 1 ] || echo "No release notes found under distribution/whatsnew/"
  return "$failed"
}

generate() {
  local version="$1"
  check_version "$version"
  rm -rf "$OUT_DIR"
  mkdir -p "$OUT_DIR"
  for locale in "${LOCALES[@]}"; do
    cp "$NOTES_ROOT/$version/whatsnew-$locale" "$OUT_DIR/whatsnew-$locale"
    echo "--- $locale ($(char_count "$OUT_DIR/whatsnew-$locale") chars)"
    cat "$OUT_DIR/whatsnew-$locale"
    echo
  done
  echo "---"
  echo "Wrote ${OUT_DIR#"$REPO_ROOT"/}"
}

# --- Drafting from CHANGELOG.md ---------------------------------------------

# Clean a single entry line: strip markdown links, issue refs, scope prefixes
clean_entry() {
  echo "$1" \
    | sed -E 's/,? *closes( \[#[0-9]+\]\([^)]*\))+//g' \
    | sed -E 's/\[#[0-9]+\]\(https:\/\/github\.com\/[^)]*\/(issues|pull)\/[0-9]+\)//g' \
    | sed -E 's#https://github\.com/[^ )]+/(issues|pull)/[0-9]+##g' \
    | sed -E 's#\([^)]*https://github\.com/[^)]*/(issues|pull)/[0-9]+[^)]*\)##g' \
    | sed -E 's/ *\(\[[^]]*\]\([^)]*\)\)//g' \
    | sed -E 's/\[([^]]*)\]\([^)]*\)/\1/g' \
    | sed -E 's/\(\s*\)//g' \
    | sed -E 's/\*\*#[0-9,#]+:\*\* *//g' \
    | sed -E 's/\*\*[a-zA-Z_-]+:\*\* *//g' \
    | sed -E 's/^\* /• /' \
    | sed -E 's/  +/ /g' \
    | sed -E 's/ +([,.;:!?])/\1/g' \
    | sed -E 's/ +$//'
}

# Upper-case the first letter after the bullet (byte-safe for BSD awk).
capitalise_entry() {
  local rest="${1#• }"
  printf '• %s%s' "$(printf '%s' "${rest:0:1}" | tr '[:lower:]' '[:upper:]')" "${rest:1}"
}

# Entries that only matter to developers: tooling scopes and wording such as
# "DX fixes" or "address code-review findings".
is_internal() {
  local raw="$1"
  local cleaned="$2"
  if [[ "$raw" =~ \*\*(ci|build|deps|dx|test|tests|chore|refactor|release|docs):\*\* ]]; then
    return 0
  fi
  echo "$cleaned" | grep -q -i -E '\b(dx|ci|code[- ]review|lint|ktlint|detekt|refactor(ing)?|tests?|dependenc(y|ies)|deps|gradle|workflows?|pipeline|baseline|screenshot tests?)\b'
}

section_label() {
  case "$1" in
    "### Features")                 echo "✨ New" ;;
    "### Bug Fixes")                echo "🐛 Fixes" ;;
    "### Performance Improvements") echo "⚡ Faster" ;;
    "### ⚠ BREAKING CHANGES")       echo "⚠️ Changed" ;;
    *)                              echo "" ;;
  esac
}

draft() {
  local version="$1"
  local dir="$NOTES_ROOT/$version"
  local file="$dir/whatsnew-en-US"

  if [ -e "$file" ]; then
    echo "Error: ${file#"$REPO_ROOT"/} already exists; not overwriting" >&2
    exit 1
  fi
  if [ ! -f "$CHANGELOG" ]; then
    echo "Error: CHANGELOG.md not found at $CHANGELOG" >&2
    exit 1
  fi

  # The version's block runs from "## [x.y.z]" to the next "## [".
  local block
  block=$(awk -v v="$version" '
    index($0, "## [" v "]") == 1 { found = 1; next }
    found && /^## \[/ { exit }
    found { print }
  ' "$CHANGELOG")

  local output=""
  local section=""
  local label=""
  local seen=""
  local dropped=0
  while IFS= read -r line; do
    if [[ "$line" =~ ^###\  ]]; then
      section="$line"
      label=$(section_label "$section")
    elif [[ "$line" =~ ^\*\  ]] && [ -n "$label" ]; then
      local cleaned
      cleaned=$(capitalise_entry "$(clean_entry "$line")")
      if is_internal "$line" "$cleaned"; then
        dropped=$((dropped + 1))
        continue
      fi
      # Breaking changes are repeated under their own type; keep the first.
      if printf '%s\n' "$seen" | grep -q -x -F -- "$cleaned"; then
        continue
      fi
      seen=$(printf '%s\n%s' "$seen" "$cleaned")
      if [[ "$output" != *"$label"* ]]; then
        output="${output:+$output
}$label"
      fi
      output="$output
$cleaned"
    fi
  done <<< "$block"

  if [ -z "$output" ]; then
    echo "Warning: no user-facing CHANGELOG entries for $version; writing a generic line." >&2
    output="• Bug fixes and performance improvements"
  fi

  mkdir -p "$dir"
  printf '%s\n' "$output" > "$file"
  echo "Wrote draft ${file#"$REPO_ROOT"/} ($(char_count "$file") chars, $dropped internal entries dropped)"
  echo "Rewrite it in plain words for people using the app, then write whatsnew-es-ES."
  echo "---"
  cat "$file"
}

case "${1:-}" in
  --check)     require_version "${2:-}"; check_version "$2" ;;
  --check-all) check_all ;;
  --draft)     require_version "${2:-}"; draft "$2" ;;
  -h|--help|"") usage ;;
  -*)          usage ;;
  *)           require_version "$1"; generate "$1" ;;
esac
