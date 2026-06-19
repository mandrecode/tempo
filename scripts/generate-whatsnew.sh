#!/usr/bin/env bash
# generate-whatsnew.sh
#
# Extracts the latest version's entries from CHANGELOG.md and generates
# a user-friendly Play Store "What's New" text in distribution/whatsnew/.
#
# Usage: ./scripts/generate-whatsnew.sh [version]
#   version  Optional. If omitted, extracts the first (latest) version block.
#
# The output is written to distribution/whatsnew/whatsnew-en-US
# and distribution/whatsnew/whatsnew-es-ES (Spanish headers, same entries).
#
# Play Store limit: 500 characters per locale.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CHANGELOG="$REPO_ROOT/CHANGELOG.md"
WHATSNEW_DIR="$REPO_ROOT/distribution/whatsnew"
OUTPUT_EN="$WHATSNEW_DIR/whatsnew-en-US"
OUTPUT_ES="$WHATSNEW_DIR/whatsnew-es-ES"
MAX_CHARS=490

if [ ! -f "$CHANGELOG" ]; then
  echo "Error: CHANGELOG.md not found at $CHANGELOG" >&2
  exit 1
fi

mkdir -p "$WHATSNEW_DIR"

VERSION="${1:-}"

# Extract the changelog block for the target version.
# Each version starts with "## [x.y.z]" and ends before the next "## [".
if [ -n "$VERSION" ]; then
  # Extract specific version block
  block=$(awk "/^## \[$VERSION\]/{found=1; next} found && /^## \[/{exit} found{print}" "$CHANGELOG")
else
  # Extract the first (latest) version block
  block=$(awk '/^## \[/{if(found) exit; found=1; next} found{print}' "$CHANGELOG")
fi

if [ -z "$block" ]; then
  echo "Warning: No changelog entries found. Writing default message." >&2
  echo "• Bug fixes and performance improvements" > "$OUTPUT_EN"
  echo "• Correcciones y mejoras de rendimiento" > "$OUTPUT_ES"
  exit 0
fi

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
    | sed -E 's/\*\*#[0-9]+:\*\* *//g' \
    | sed -E 's/\*\*[a-zA-Z_-]+:\*\* *//g' \
    | sed -E 's/^\* /• /' \
    | sed -E 's/  +/ /g' \
    | sed -E 's/ +([,.;:!?])/\1/g' \
    | sed -E 's/ +$//' \
    | awk 'substr($0, 1, 2) == "• " { print "• " toupper(substr($0, 3, 1)) substr($0, 4); next } { print }'
}

# Map CHANGELOG section headers to user-friendly labels
section_label() {
  case "$1" in
    "### Features")                echo "✨ NEW" ;;
    "### Bug Fixes")               echo "🐛 FIXES" ;;
    "### Performance Improvements") echo "⚡ PERFORMANCE" ;;
    "### ⚠ BREAKING CHANGES")     echo "⚠️ BREAKING" ;;
    *)                             echo "" ;;
  esac
}

# Map CHANGELOG section headers to Spanish labels
section_label_es() {
  case "$1" in
    "### Features")                echo "✨ NOVEDADES" ;;
    "### Bug Fixes")               echo "🐛 CORRECCIONES" ;;
    "### Performance Improvements") echo "⚡ RENDIMIENTO" ;;
    "### ⚠ BREAKING CHANGES")     echo "⚠️ CAMBIOS IMPORTANTES" ;;
    *)                             echo "" ;;
  esac
}

# Build sectioned output from the extracted block
user_friendly=""
user_friendly_es=""
current_section=""
current_entries=""
breaking_entries=""

contains_entry() {
  local entries="$1"
  local needle="$2"
  while IFS= read -r entry; do
    if [ "$entry" = "$needle" ]; then
      return 0
    fi
  done <<< "$entries"
  return 1
}

append_entry() {
  local entries="$1"
  local entry="$2"
  if [ -n "$entries" ]; then
    printf '%s\n%s' "$entries" "$entry"
  else
    printf '%s' "$entry"
  fi
}

while IFS= read -r line; do
  if [[ "$line" =~ ^###\  ]]; then
    current_section="$line"
  elif [[ "$line" =~ ^\*\  ]] && [ "$current_section" = "### ⚠ BREAKING CHANGES" ]; then
    cleaned=$(clean_entry "$line")
    if ! contains_entry "$breaking_entries" "$cleaned"; then
      breaking_entries=$(append_entry "$breaking_entries" "$cleaned")
    fi
  fi
done <<< "$block"

current_section=""
current_entries=""

while IFS= read -r line; do
  if [[ "$line" =~ ^###\  ]]; then
    # Flush previous section
    if [ -n "$current_section" ] && [ -n "$current_entries" ]; then
      label=$(section_label "$current_section")
      label_es=$(section_label_es "$current_section")
      if [ -n "$label" ]; then
        if [ -n "$user_friendly" ]; then
          user_friendly="$user_friendly
"
          user_friendly_es="$user_friendly_es
"
        fi
        user_friendly="$user_friendly$label
$current_entries"
        user_friendly_es="$user_friendly_es$label_es
$current_entries"
      fi
    fi
    current_section="$line"
    current_entries=""
  elif [[ "$line" =~ ^\*\  ]]; then
    cleaned=$(clean_entry "$line")
    if [ "$current_section" != "### ⚠ BREAKING CHANGES" ] &&
      contains_entry "$breaking_entries" "$cleaned"; then
      continue
    fi

    current_entries=$(append_entry "$current_entries" "$cleaned")
  fi
done <<< "$block"

# Flush the last section
if [ -n "$current_section" ] && [ -n "$current_entries" ]; then
  label=$(section_label "$current_section")
  label_es=$(section_label_es "$current_section")
  if [ -n "$label" ]; then
    if [ -n "$user_friendly" ]; then
      user_friendly="$user_friendly
"
      user_friendly_es="$user_friendly_es
"
    fi
    user_friendly="$user_friendly$label
$current_entries"
    user_friendly_es="$user_friendly_es$label_es
$current_entries"
  fi
fi

if [ -z "$user_friendly" ]; then
  echo "Warning: Could not parse entries. Writing default message." >&2
  echo "• Bug fixes and performance improvements" > "$OUTPUT_EN"
  echo "• Correcciones y mejoras de rendimiento" > "$OUTPUT_ES"
  exit 0
fi

# Truncate to Play Store limit (500 chars) by dropping lines from the end
truncate_text() {
  local text="$1"
  local char_count
  char_count=$(printf '%s' "$text" | wc -m)
  if [ "$char_count" -gt $MAX_CHARS ]; then
    local truncated=""
    while IFS= read -r line; do
      local candidate="${truncated:+$truncated
}$line"
      local candidate_len
      candidate_len=$(printf '%s' "$candidate" | wc -m)
      if [ "$candidate_len" -gt $(( MAX_CHARS - 3 )) ]; then
        break
      fi
      truncated="$candidate"
    done <<< "$text"
    text="${truncated}..."
    echo "Warning: What's New text exceeds $MAX_CHARS chars, truncated." >&2
  fi
  printf '%s' "$text"
}

user_friendly=$(truncate_text "$user_friendly")
user_friendly_es=$(truncate_text "$user_friendly_es")

printf '%s' "$user_friendly" > "$OUTPUT_EN"
printf '%s' "$user_friendly_es" > "$OUTPUT_ES"

char_count_en=$(printf '%s' "$user_friendly" | wc -m)
char_count_es=$(printf '%s' "$user_friendly_es" | wc -m)
echo "Generated $OUTPUT_EN ($char_count_en chars)"
echo "Generated $OUTPUT_ES ($char_count_es chars)"
echo "---"
cat "$OUTPUT_EN"
echo "---"
cat "$OUTPUT_ES"
echo "---"
