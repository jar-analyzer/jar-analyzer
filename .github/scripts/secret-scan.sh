#!/usr/bin/env bash
set -Eeuo pipefail
IFS=$'\n\t'
shopt -s globstar extglob nullglob

readonly SCRIPT_NAME="secret-scan.sh"
readonly REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
readonly PATTERN_FILE="${REPO_ROOT}/.github/secret-patterns.txt"
readonly ALLOWLIST_FILE="${REPO_ROOT}/.github/secret-allowlist.txt"

ALLOW_LINE_RE=""
ALLOW_PATH_GLOBS=()

HIT_COUNT=0
REPORT_FILE="$(mktemp -t secret-scan-report.XXXXXX)"
trap "rm -f '${REPORT_FILE}'" EXIT

err()  { printf '::error::%s\n' "$*" >&2; }
warn() { printf '::warning::%s\n' "$*" >&2; }
info() { printf '%s\n' "$*"; }

require_file() {
  local f=$1
  if [[ ! -f "$f" ]]; then
    err "missing required file: $f"
    exit 2
  fi
}

require_file "$PATTERN_FILE"
require_file "$ALLOWLIST_FILE"

load_allowlist() {
  local line trimmed
  local line_re_parts=()
  while IFS= read -r line || [[ -n "$line" ]]; do
    trimmed="${line#"${line%%[![:space:]]*}"}"
    [[ -z "$trimmed" || "$trimmed" == \#* ]] && continue

    if [[ "$trimmed" == FILE:* ]]; then
      ALLOW_PATH_GLOBS+=("${trimmed#FILE:}")
    else
      line_re_parts+=("$trimmed")
    fi
  done < "$ALLOWLIST_FILE"

  if (( ${#line_re_parts[@]} > 0 )); then
    local joined=""
    local p
    for p in "${line_re_parts[@]}"; do
      if [[ -z "$joined" ]]; then
        joined="(${p})"
      else
        joined="${joined}|(${p})"
      fi
    done
    ALLOW_LINE_RE="$joined"
  fi
}

path_allowlisted() {
  local path=$1
  local g re
  for g in "${ALLOW_PATH_GLOBS[@]}"; do
    re="$(glob_to_regex "$g")"
    if [[ "$path" =~ ^${re}$ ]]; then
      return 0
    fi
  done
  return 1
}

glob_to_regex() {
  local s=$1
  local out="" i ch nxt
  for (( i=0; i<${#s}; i++ )); do
    ch="${s:i:1}"
    nxt="${s:i+1:1}"
    case "$ch" in
      '*')
        if [[ "$nxt" == '*' ]]; then
          out+='.*'
          i=$((i+1))
        else
          out+='[^/]*'
        fi
        ;;
      '?')
        out+='[^/]'
        ;;
      '.'|'+'|'('|')'|'{'|'}'|'['|']'|'^'|'$'|'|'|'\\')
        out+="\\${ch}"
        ;;
      *)
        out+="$ch"
        ;;
    esac
  done
  printf '%s' "$out"
}

line_allowlisted() {
  local content=$1
  if [[ -z "$ALLOW_LINE_RE" ]]; then
    return 1
  fi
  if printf '%s' "$content" | grep -E -i -q -- "$ALLOW_LINE_RE"; then
    return 0
  fi
  return 1
}

declare -a RULE_NAMES=()
declare -a RULE_REGEXES=()

load_rules() {
  local line name regex
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line#"${line%%[![:space:]]*}"}"
    [[ -z "$line" || "$line" == \#* ]] && continue
    if [[ "$line" != *"|"* ]]; then
      warn "ignored malformed rule line: $line"
      continue
    fi
    name="${line%%|*}"
    regex="${line#*|}"
    if [[ -z "$name" || -z "$regex" ]]; then
      warn "ignored empty rule: $line"
      continue
    fi
    RULE_NAMES+=("$name")
    RULE_REGEXES+=("$regex")
  done < "$PATTERN_FILE"

  if (( ${#RULE_NAMES[@]} == 0 )); then
    err "no rules loaded from $PATTERN_FILE"
    exit 2
  fi
  info "loaded ${#RULE_NAMES[@]} rules"
}

apply_rules() {
  local source_tag=$1
  local input_file=$2
  local i name regex hits_for_rule

  for (( i=0; i<${#RULE_NAMES[@]}; i++ )); do
    name="${RULE_NAMES[$i]}"
    regex="${RULE_REGEXES[$i]}"

    if ! hits_for_rule="$(grep -E -i -- "$regex" "$input_file" 2>/dev/null || true)"; then
      hits_for_rule=""
    fi
    [[ -z "$hits_for_rule" ]] && continue

    while IFS= read -r matched_line; do
      [[ -z "$matched_line" ]] && continue
      local path lineno content
      path="${matched_line%%	*}"
      local rest="${matched_line#*	}"
      lineno="${rest%%	*}"
      content="${rest#*	}"

      if path_allowlisted "$path"; then
        continue
      fi
      if line_allowlisted "$content"; then
        continue
      fi

      HIT_COUNT=$((HIT_COUNT + 1))
      {
        printf '\n--- HIT #%d ---\n' "$HIT_COUNT"
        printf 'rule    : %s\n' "$name"
        printf 'source  : %s\n' "$source_tag"
        printf 'path    : %s\n' "$path"
        printf 'line    : %s\n' "$lineno"
        if (( ${#content} > 240 )); then
          printf 'content : %s...(truncated)\n' "${content:0:240}"
        else
          printf 'content : %s\n' "$content"
        fi
      } >> "$REPORT_FILE"
    done <<< "$hits_for_rule"
  done
}

scan_tree() {
  info "scanning working tree (tracked files)..."
  local tmp
  tmp="$(mktemp -t secret-scan-tree.XXXXXX)"
  trap "rm -f '$tmp' '$REPORT_FILE'" EXIT

  while IFS= read -r -d '' file; do
    if [[ ! -f "$file" ]]; then continue; fi
    local size
    size=$(wc -c < "$file" 2>/dev/null || echo 0)
    if (( size > 5 * 1024 * 1024 )); then
      continue
    fi
    if file -b --mime "$file" 2>/dev/null | grep -q 'charset=binary'; then
      continue
    fi
    awk -v path="$file" 'BEGIN{OFS="\t"} {print path, NR, $0}' "$file" >> "$tmp"
  done < <(git -C "$REPO_ROOT" ls-files -z)

  apply_rules "tree" "$tmp"
  rm -f "$tmp"
}

scan_history() {
  local since_ref="${1:-}"
  local range_args=()
  if [[ -n "$since_ref" ]]; then
    range_args=("${since_ref}..HEAD")
  fi

  info "scanning git history${since_ref:+ since $since_ref}..."

  local tmp
  tmp="$(mktemp -t secret-scan-history.XXXXXX)"
  trap "rm -f '$tmp' '$REPORT_FILE'" EXIT

  local commit_list
  commit_list="$(git -C "$REPO_ROOT" log --no-merges --format='%H' "${range_args[@]}" 2>/dev/null || true)"

  if [[ -z "$commit_list" ]]; then
    info "no commits to scan."
    return 0
  fi

  local sha
  while IFS= read -r sha; do
    [[ -z "$sha" ]] && continue
    : > "$tmp"

    git -C "$REPO_ROOT" show --no-color --unified=0 --format= "$sha" 2>/dev/null | \
      awk -v sha="$sha" '
        /^diff --git a\// {
          path = $0
          sub(/.*[[:space:]]b\//, "", path)
          lineno = 0
          next
        }
        /^@@ / {
          if (match($0, /\+[0-9]+/)) {
            lineno = substr($0, RSTART+1, RLENGTH-1) + 0
            lineno = lineno - 1
          }
          next
        }
        /^\+\+\+ / { next }
        /^\+/ {
          lineno++
          line = substr($0, 2)
          if (path != "" && line != "") {
            printf "%s\t%d\t%s\n", path, lineno, line
          }
        }
        /^[^+]/ { next }
      ' >> "$tmp"

    if [[ -s "$tmp" ]]; then
      apply_rules "$sha" "$tmp"
    fi
  done <<< "$commit_list"

  rm -f "$tmp"
}

scan_diff() {
  local base=$1
  local head=$2

  info "scanning diff: ${base}..${head}"

  local tmp
  tmp="$(mktemp -t secret-scan-diff.XXXXXX)"
  trap "rm -f '$tmp' '$REPORT_FILE'" EXIT

  git -C "$REPO_ROOT" diff --no-color --unified=0 "${base}...${head}" 2>/dev/null | \
    awk '
      /^diff --git a\// {
        path = $0
        sub(/.*[[:space:]]b\//, "", path)
        lineno = 0
        next
      }
      /^@@ / {
        if (match($0, /\+[0-9]+/)) {
          lineno = substr($0, RSTART+1, RLENGTH-1) + 0
          lineno = lineno - 1
        }
        next
      }
      /^\+\+\+ / { next }
      /^\+/ {
        lineno++
        line = substr($0, 2)
        if (path != "" && line != "") {
          printf "%s\t%d\t%s\n", path, lineno, line
        }
      }
    ' >> "$tmp"

  apply_rules "diff:${base}..${head}" "$tmp"
  rm -f "$tmp"
}

main() {
  load_rules
  load_allowlist

  local mode="${1:-tree}"
  case "$mode" in
    tree)
      scan_tree
      ;;
    history)
      scan_history "${2:-}"
      ;;
    diff)
      if (( $# < 3 )); then
        err "usage: $SCRIPT_NAME diff <base> <head>"
        exit 2
      fi
      scan_diff "$2" "$3"
      ;;
    *)
      err "unknown mode: $mode (expected: tree | history | diff)"
      exit 2
      ;;
  esac

  if (( HIT_COUNT > 0 )); then
    {
      echo ""
      echo "==============================================="
      echo "  SECRET SCAN FAILED: ${HIT_COUNT} hit(s) found"
      echo "==============================================="
    } | tee -a "$REPORT_FILE"
    cat "$REPORT_FILE"

    if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
      {
        echo "## :rotating_light: Secret Scan: ${HIT_COUNT} hit(s)"
        echo ""
        echo '<details><summary>Click to expand report</summary>'
        echo ""
        echo '```'
        cat "$REPORT_FILE"
        echo '```'
        echo ""
        echo '</details>'
      } >> "$GITHUB_STEP_SUMMARY"
    fi
    exit 1
  fi

  info "no secrets found."
  if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
    echo "## :white_check_mark: Secret Scan: clean" >> "$GITHUB_STEP_SUMMARY"
  fi
  exit 0
}

main "$@"
