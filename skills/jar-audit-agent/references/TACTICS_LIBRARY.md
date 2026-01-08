# TACTICS_LIBRARY（审计动作库：只允许走“证据+覆盖率”）

## 0) 口径（避免糊弄）
- **Verified 口径**：只有 `VULN/SAFE` 计入 Verified；`NEEDS_DEEPER` 仍算 Remaining
- **Fail-Closed**：
  - submit：没有 `snippet_ref/chain_trace/sql_proof` → 拒绝写入
  - strict 报告：`Verified < Emitted` → 直接 fail（用于 CI/验收）

## 1) 标准闭环（每个向量）

- **Freeze**：圈地 + reduction + sink_resolution（禁止 LIKE；desc 为空先 desc resolution）
- **Reach（如果向量需要入口可达）**：entry→candidate 最短链与 entry_binding 回写 candidates
- **Next**：小批次（1~5），并优先拉 `NEEDS_DEEPER` 的 remaining
- **Evidence**：默认 window；若质量 WARN/BAD → `--auto-fallback` 让系统自动 expand→method
- **Submit**：
  - `SAFE`：证据可以是 snippet_ref 或 chain_trace 或 sql_proof
  - `VULN`（strict）：必须有 snippet_ref（且 slice_quality=GOOD）或绑定入口的 chain_trace
- **Report**：只读 artifacts 编译；会给 Remaining TopK 的下一批计划

## 2) Evidence 取证策略（Slicer 容错）

- **优先**：`evidence --auto-fallback`
  - 先 window
  - WARN/BAD 自动 expand
  - 仍不佳自动 method
- **anchor**：默认用 sink method（例如 `exec/execute/openConnection`），用于把窗口落在“危险调用点”附近

## 3) 什么时候标 NEEDS_DEEPER

满足任一条件就用 NEEDS_DEEPER（不要硬判 VULN/SAFE）：
- 证据质量非 GOOD（WARN/BAD）且关键逻辑不在窗口内
- chain_trace 有 entry，但需要跨多段逻辑（过滤/拼接/编码）才能判断可控性
- 明显存在“安全分支/过滤器”，但未能确认是否可绕过

## 4) 0 候选处理（必须留痕）

当 Freeze/Reach 产出 0：
- Freeze 里必须有 `sql_proof` 或 `sink_resolution.misses`（证明“查过且确实未观察到”）
- 报告里仍会显示 coverage=0/0，并保留 rules 快照用于复现

## 5) 操作门禁（常见卡点，禁止走偏）

### 5.1 MCP 结果文件路径从哪来（`--mcp-tool-result-file`）
- 当 MCP 调用返回过大（token 超限）时，Claude 会提示：
  - “Output has been saved to …/tool-results/<file>.txt”
- 这个提示里的绝对路径就是 `--mcp-tool-result-file` 的值。
- 禁止：自己把内容复制到 `/tmp` 再手写解析。
- 禁止：扫描 `/tmp`、`/var/folders`、用 `find` 猜 tool-results 路径（不可靠且会触发交互卡死）。
- 如果 MCP 没有给出 “saved to …/tool-results/xxx”：
  - 说明本次输出没有自动落盘；不要去搜系统目录
  - 若输出足够小：将 MCP 返回内容保存为 run 内文件，再用 `--code-json-file` 输入
  - 若输出过大无法手动保存：重新调用 MCP 直到出现自动落盘提示；若始终不落盘，按 fail-closed 停止并说明“需要工具支持输出到文件”

### 5.2 anchor 怎么填（不知道变量名也能取证）
- 默认 anchor：用 sink 的方法名（例如 `exec/parseObject/openConnection/readObject/setViewName`）。
- 不确定时：不填 `--anchor` + 必开 `--auto-fallback`（window→expand→method）。
- 禁止：为了找 anchor 去 grep/写脚本扫大文件。

### 5.3 SAFE 的“间接数据流”reasoning 怎么写（reasoning 只有字符串）
按固定结构写（便于验收复盘）：
- `TAINT: source=<user_input|db|const|unknown> sink=<...> path=<direct|indirect>`
- `WHY_SAFE: <一句话说明为何不可控/不可达/被白名单约束>`
- `RISK_NOTE: <若依赖 DB 字段，写明“若攻击者可写 DB 字段则可能升级”>`
- `EVIDENCE: sha256:<...> entry:<METHOD PATH> auth_hints:<...>`

### 5.4 “手工发现但不在 candidates”怎么走（禁止手改 JSONL）
唯一允许路径：
- 扩展向量 sink（改 `vectors/*.yaml`）→ 重跑该向量 `freeze -> reach -> next` → 进入 candidates → 再 `evidence/submit`。
- 禁止：`cat >> verify_*.jsonl` / 捏 `candidate_id` / 绕过工具链。

### 5.5 NEEDS_DEEPER vs SAFE（硬边界）
- SAFE：证据足以给出明确不可利用原因（输入不可控/过滤可见/不可达有链路证明）。
- NEEDS_DEEPER：存在疑点但证据不足以判 SAFE/VULN（证据质量非 GOOD、跨多段逻辑未覆盖、攻击者是否可写入 DB 不清楚）。

### 5.6 sink 定义不全怎么办（如何扩展）
- 扩展入口只在 `vectors/*.yaml` 的 `discovery.sinks`：
  - 优先写 `{class, method, desc: null}`，让 Freeze 用 DB 做 desc resolution 展开重载。
- 扩展后必须重跑该向量（旧 run 的 candidates 不会自动更新）。

### 5.7 batch 取完了怎么判断“这个向量结束”
必须同时满足：
- `next --vector <v>` 返回空/无可取候选，并且
- `status --vectors <v>` 显示 `remaining = 0`（Verified=VULN/SAFE 覆盖全部 emitted）

### 5.8 工作目录门禁（防止 `can't open file .../scripts/cli.py`）
每次执行任何 `python3 scripts/cli.py ...` 前：
- `pwd` 必须以 `.../jar-audit-agent` 结尾

### 5.9 报告里的 PoC（HTTP raw, SIMULATED）输出口径（少输出，但要直白）

- **定位**：SIMULATED 只是“如何利用/如何打点的提示”，不是证据；默认只给最小骨架，避免长 payload/大量字节。
- **通用规则**：
  - 能短则短（优先“无害探测/可识别回显”的短输入，符合实际http请求包）
  - 一旦会变长/二进制/需要多段链路，就用占位符：`<RCE_PAYLOAD>` / `<XXE_PAYLOAD>` / `<BINARY_OR_LARGE_PAYLOAD>` / `<SQLI_PAYLOAD>`
- **LFI/Traversal**：用“短路径穿越”指向本地敏感文件的占位表达：`<TRAVERSAL_TO_SENSITIVE_FILE>`
- **SSRF**：用“本机/内网探测地址”的占位表达：`<SSRF_PROBE_URL>`
- **上传**：固定小哑元即可：文件名 `a.txt`，内容 `1234xx`
- **RCE**：能用短“无害命令探测”表达就用，否则占位：`cmd=<RCE_PAYLOAD>`
- **XXE**：能用最小 XML/DOCTYPE 表达就用，否则占位：`xml=<XXE_PAYLOAD>`

