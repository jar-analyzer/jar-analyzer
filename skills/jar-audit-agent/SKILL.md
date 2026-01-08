---
name: jar-audit-agent
description: 基于 jar-analyzer（SQLite + 内置 MCP）的证据驱动 Java 安全审计技能。核心目标：把“结论”变成“可复现证据 + 可度量覆盖率”。
---
# jar-audit-agent（Fail-Closed / Evidence+Coverage）

## 数据源
- **DB(SQLite)**：Freeze/coverage/entry/graph 输入（必须）
- **MCP(:20032 SSE)**：推荐用于取证（agent 可直接调用 `get_code_cfr/get_code_fernflower`）

## 禁止事项（防止“自作聪明”导致调用错误）
- **禁止agent写 Python/正则/临时脚本**去解析 curl 输出、拼 SQL、拼 HTTP URL 例如python3 - <<；只能调用已经存在的 `python3 scripts/cli.py <subcommand>`。
- **禁止手写 curl/HTTP 调用**：本技能不使用 HTTP 通道；取证只允许走 MCP。
- **禁止用 grep/自写脚本从 MCP tool-results 抽代码**：必须用 `evidence --mcp-tool-result-file <path>`（优先）或 `--code-json-file/--code-text-file`。
- **禁止在错误目录执行 cli**：任何 `python3 scripts/cli.py ...` 之前，必须先进入技能根目录。
- **禁止“猜路径/临时注入”取证**：禁止扫描 `/tmp`/`/var/folders`、禁止 `find` 猜 tool-results、禁止 `/dev/stdin`/here-doc/echo 注入 JSON；一旦出现 “Do you want to proceed?” / “allow reading from tmp/ …” 立即取消。取证只允许两条路径：`--mcp-tool-result-file <Claude 提示的绝对路径>` 或把 JSON 写入 `runs/<run_id>/inputs/` 再 `--code-json-file`。
- **遇到工具不支持/候选未覆盖时必须停下**：只能输出“需要扩展 skill（新增向量 sink/规则）”，不得写脚本绕过、不得手改 `verify_*.jsonl`。

## 操作细则（避免卡点）
- 详细操作门禁（MCP 文件路径/anchor/SAFE-NEEDS_DEEPER 边界/扩展 sink/结束判定/工作目录）见：
  - `references/TACTICS_LIBRARY.md`

## MCP 取证（唯一推荐）
- MCP 工具名：
  - `get_code_cfr` / `get_code_fernflower`（返回 JSON，含 `fullClassCode`）
- 取证方式：
  - 先调用 MCP `get_code_*`（不要阅读/解析返回内容；输出过大时系统会自动落盘到 tool-results 文件）
  - 立刻调用 `python3 scripts/cli.py evidence --mcp-tool-result-file <tool-results/xxx.txt>`（自动抽 `fullClassCode` 并完成切片+hash 落盘）
  - 注意：`evidence` **不会自己去找代码**；如果没提供 `--mcp-tool-result-file/--code-json-file/--code-text-file`，工具会 fail-closed 直接报 “missing code input”
  - 若 MCP 输出未自动落盘且可手动保存：将 MCP 返回 JSON 保存到 `runs/<run_id>/inputs/mcp_<candidate_id>.json`，然后用 `--code-json-file`

## 铁律
- **Fact**：节点=四元组 `(jar_id,class,method,desc)`；禁止只用名字 join/追链
- **Evidence**：submit 必须带 `snippet_ref` 或 `chain_trace` 或 `sql_proof`，否则拒绝写入
- **Report**：报告只读 artifacts 编译；`--strict` 未完成 coverage 直接 fail

## 状态机（唯一入口）
用 `python3 scripts/cli.py`（等价 `scripts/jaudit.py`）：
- `init`：创建 `runs/<id>/session.json inventory.json rules/ graph_cache/ ...`
- `profile --cwd <jar-analyzer>`：写 profile 并快照 `vulnerability.yaml/dfs-sink.json` 到 `runs/<id>/rules/`
- `freeze --vector <v> --cwd <jar-analyzer>`：圈地+降噪+sink_resolution
- `graph`→`reach --vector <v>`：可达性/最短链回写 candidates
- `next --vector <v> --limit 5`：生成 batch（默认按向量 `batch.size`，推荐 5）；**NEEDS_DEEPER 优先**
- `evidence --candidate-id <CID> --batch-id <BID> --code-json-file <mcp.json> --auto-fallback`：WARN/BAD 自动 expand→method
- `submit --candidate-id <CID> --batch-id <BID> --status VULN|SAFE|NEEDS_DEEPER`：严格模式 VULN 需 GOOD 证据
- `report [--strict]`：只读编译 `audit_report.md`（含 Remaining TopK 下一批计划）
- `status`：机器可算进度（final=VULN/SAFE；NEEDS_DEEPER 仍算 remaining）

## 强制运行规程（防“一条就结束”）
你是驾驶员：**必须按批次循环**，直到满足停止条件才允许 `report`。

### Full Audit（覆盖表 67-84 对应的全套类别）
- **入口**：优先用默认队列（`vectors/default_queue.yaml`），不要只跑单个向量就下结论。
- **流程**：
  - `init`
  - `profile --cwd <jar-analyzer>`
  - `graph`（每个 run 只需一次）
  - 对默认队列里每个 `vector` 执行：
    - `freeze --vector <v> --cwd <jar-analyzer>`
    - `reach --vector <v>`
    - **VERIFY LOOP（必须循环，不许只做一条）**：
      - `next --vector <v> --limit 5`
      - 对 batch 里的每个 candidate：`evidence ... --auto-fallback` → `submit ...`
      - 继续 `next` 取下一批，直到：
        - 该向量没有可取候选（next 输出为空/或触发 no candidates），或
        - 你已经把所有候选都提交为 final（VULN/SAFE），或
        - 你把剩余的全部标记为 NEEDS_DEEPER（并在 reasoning 里写明阻断原因：例如入口不可达/证据不可用/缺少环境信息）。
  - 全部向量跑完后再 `report`（必要时 `--strict`）

### 什么时候允许生成报告
- 只有在 **至少完成一次完整的 VERIFY LOOP（next→evidence→submit→next…）** 后才允许 `report`。
- `report` 不是“结束”，只是“当前台账快照”；如果存在 Remaining TopK，你必须继续循环。

