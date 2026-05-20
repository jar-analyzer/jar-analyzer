#!/usr/bin/env bash
# Self-hosted secret scanner for GitHub Actions.
#
# Usage:
#   secret-scan.sh tree            # 扫描当前工作树
#   secret-scan.sh history [base]  # 扫描提交历史 (base 可省略，默认扫全部历史)
#   secret-scan.sh diff  <base> <head>  # 仅扫两个 ref 之间的新增内容（PR 模式）
#
# Files (相对仓库根):
#   .github/secret-patterns.txt    扫描规则
#   .github/secret-allowlist.txt   误报豁免
#
# Exit codes:
#   0  无泄漏
#   1  发现泄漏
#   2  脚本错误（配置缺失/参数非法等）

set -Eeuo pipefail
IFS=$'\n\t'

# ---------------------------------------------------------------------
# 常量与基础校验
# ---------------------------------------------------------------------

readonly SCRIPT_NAME="secret-scan.sh"
readonly REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
readonly PATTERN_FILE="${REPO_ROOT}/.github/secret-patterns.txt"
readonly ALLOWLIST_FILE="${REPO_ROOT}/.github/secret-allowlist.txt"

# 行级 / 路径级豁免规则将被加载到下面两个临时文件
ALLOW_LINE_RE=""
ALLOW_PATH_GLOBS=()

# 报告输出
HIT_COUNT=0
REPORT_FILE="$(mktemp -t secret-scan-report.XXXXXX)"
# shellcheck disable=SC2064
trap "rm -f '${REPORT_FILE}'" EXIT

err() { printf '::error::%s\n' "$*" >&2; }
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

# ---------------------------------------------------------------------
# 加载白名单
# ---------------------------------------------------------------------

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
    # 拼成单一 ERE：(part1)|(part2)|...
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

# 用 bash 内建的 [[ == glob ]] 做路径匹配；返回 0 表示路径在白名单。
# $1: 相对仓库根的路径
path_allowlisted() {
  local path=$1
  local g
  for g in "${ALLOW_PATH_GLOBS[@]}"; do
    # shellcheck disable=SC2053
    if [[ "$path" == $g ]]; then
      return 0
    fi
  done
  return 1
}

# 行级豁免：返回 0 表示该行属于白名单。
line_allowlisted() {
  local content=$1
  if [[ -z "$ALLOW_LINE_RE" ]]; then
    return 1
  fi
  if printf '%s' "$content" | grep -E -q -- "$ALLOW_LINE_RE"; then
    return 0
  fi
  return 1
}

# ---------------------------------------------------------------------
# 扫描核心：对一段 "stream" 应用所有规则
#
# 输入流的每一行格式约定（由各扫描函数构造）：
#   <SOURCE>\t<PATH>\t<LINENO>\t<CONTENT>
# 其中 SOURCE 可以是 "tree" / 一个 commit SHA。
#
# 输出：发现的命中追加到 REPORT_FILE，并累加 HIT_COUNT。
# ---------------------------------------------------------------------

# 加载规则：rule_name|regex
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

# 对 "PATH<TAB>LINENO<TAB>CONTENT" 形式的内容应用所有规则
# $1: 数据来源标签 (tree / commit sha)
apply_rules() {
  local source_tag=$1
  local input_file=$2
  local i name regex hits_for_rule

  for (( i=0; i<${#RULE_NAMES[@]}; i++ )); do
    name="${RULE_NAMES[$i]}"
    regex="${RULE_REGEXES[$i]}"

    # 用 grep -E 在中间结果上做匹配。grep -E 的 -- 防止 regex 以 - 开头被误当选项。
    # 输出仍保持 PATH\tLINENO\tCONTENT 三列。
    if ! hits_for_rule="$(grep -E -- "$regex" "$input_file" 2>/dev/null || true)"; then
      hits_for_rule=""
    fi
    [[ -z "$hits_for_rule" ]] && continue

    # 逐行处理命中，做白名单过滤
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
        # 截断超长行，避免日志爆炸
        if (( ${#content} > 240 )); then
          printf 'content : %s...(truncated)\n' "${content:0:240}"
        else
          printf 'content : %s\n' "$content"
        fi
      } >> "$REPORT_FILE"
    done <<< "$hits_for_rule"
  done
}

# ---------------------------------------------------------------------
# 模式 1：tree —— 扫描当前工作树（已被 git 跟踪的文件）
# ---------------------------------------------------------------------

scan_tree() {
  info "scanning working tree (tracked files)..."
  local tmp
  tmp="$(mktemp -t secret-scan-tree.XXXXXX)"
  # shellcheck disable=SC2064
  trap "rm -f '$tmp' '$REPORT_FILE'" EXIT

  # 用 git grep 一次性产出 PATH:LINENO:CONTENT，再转换成 PATH\tLINENO\tCONTENT。
  # -I 跳过二进制；--no-color；-n 行号；-e '' 配合 --line-number=. 不可用，
  # 我们用一个永远会触发的正则空匹配代替：先用 cat-file 列出全部行更稳。
  #
  # 这里改用更可控的方式：git ls-files -z 列出文件 -> 逐文件 awk 输出，
  # 文件名/换行/特殊字符都安全（用 NUL 分隔）。
  while IFS= read -r -d '' file; do
    # 跳过明显的二进制、超大文件
    if [[ ! -f "$file" ]]; then continue; fi
    # 大小限制：> 5MB 的文件跳过（密钥极少出现在大二进制里）
    local size
    size=$(wc -c < "$file" 2>/dev/null || echo 0)
    if (( size > 5 * 1024 * 1024 )); then
      continue
    fi
    # 用 file -b 检测，跳过二进制（mime 类含 charset=binary 即跳过）
    if file -b --mime "$file" 2>/dev/null | grep -q 'charset=binary'; then
      continue
    fi
    # 输出 path\tlineno\tcontent，用 awk 严格控制
    awk -v path="$file" 'BEGIN{OFS="\t"} {print path, NR, $0}' "$file" >> "$tmp"
  done < <(git -C "$REPO_ROOT" ls-files -z)

  apply_rules "tree" "$tmp"
  rm -f "$tmp"
}

# ---------------------------------------------------------------------
# 模式 2：history —— 扫描提交历史（每个 commit 的 patch 中“新增的行”）
# ---------------------------------------------------------------------

scan_history() {
  local since_ref="${1:-}"
  local range_args=()
  if [[ -n "$since_ref" ]]; then
    range_args=("${since_ref}..HEAD")
  fi

  info "scanning git history${since_ref:+ since $since_ref}..."

  local tmp
  tmp="$(mktemp -t secret-scan-history.XXXXXX)"
  # shellcheck disable=SC2064
  trap "rm -f '$tmp' '$REPORT_FILE'" EXIT

  # 用 git log -p 拿到所有提交的 patch；按 commit 边界拆开后逐 commit 扫
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

    # 提取该 commit 的 unified diff，仅保留新增行（以 + 开头但不是 +++）
    # 以 "diff --git a/<x> b/<x>" 标记切换文件
    git -C "$REPO_ROOT" show --no-color --unified=0 --format= "$sha" 2>/dev/null | \
      awk -v sha="$sha" '
        /^diff --git a\// {
          # 取 "b/<path>"
          path = $0
          sub(/.*[[:space:]]b\//, "", path)
          lineno = 0
          next
        }
        /^@@ / {
          # @@ -a,b +c,d @@
          if (match($0, /\+[0-9]+/)) {
            lineno = substr($0, RSTART+1, RLENGTH-1) + 0
            lineno = lineno - 1
          }
          next
        }
        /^\+\+\+ / { next }
        /^\+/ {
          lineno++
          # 去掉首字符 +
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

# ---------------------------------------------------------------------
# 模式 3：diff —— 仅扫两 ref 之间的新增（PR 模式）
# ---------------------------------------------------------------------

scan_diff() {
  local base=$1
  local head=$2

  info "scanning diff: ${base}..${head}"

  local tmp
  tmp="$(mktemp -t secret-scan-diff.XXXXXX)"
  # shellcheck disable=SC2064
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

# ---------------------------------------------------------------------
# main
# ---------------------------------------------------------------------

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

    # GitHub Actions Job Summary
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
