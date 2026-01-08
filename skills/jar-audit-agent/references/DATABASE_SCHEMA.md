# DATABASE_SCHEMA（jar-audit-agent 用到的 DB 事实基线）

本技能只把 SQLite 当作**事实基线**：Freeze/coverage/入口/调用边/字符串与成员线索都必须来自 DB（可复现、可计数）。

## 关键表（本技能会用到）

### `method_call_table`（调用边）
- **用途**：Freeze（精确 sink callers）、构图输入、可达性（graph_cache）
- **关键字段**：
  - caller_*：`caller_jar_id`, `caller_class_name`, `caller_method_name`, `caller_method_desc`
  - callee_*：`callee_jar_id`, `callee_class_name`, `callee_method_name`, `callee_method_desc`

### `spring_method_table`（HTTP 入口面 / entry binding）
- **用途**：reachability 的 entry mapping（method-level）
- **关键字段**：`jar_id`, `class_name`, `method_name`, `method_desc`, `restful_type`, `path`

### `spring_controller_table`（控制器规模/前缀猜测）
- **用途**：profile 的 entry_surface 与 app_prefix_guess
- **关键字段**：`class_name`, `jar_id`

### `string_table`（硬编码字符串）
- **关键字段**（以实际 DB 为准）：
  - `jar_id`, `class_name`, `method_name`, `method_desc`, `value`

### `member_table`（字段/成员）
- **关键字段**：
  - `jar_id`, `class_name`, `member_name`, `value`, `type_class_name`, `method_desc`, `method_signature`

## Fact Contract（方法节点唯一性）

任何方法节点必须用四元组唯一标识：

- `(jar_id, class_name, method_name, method_desc)`

并生成稳定 NodeID（示例）：

- `node_id = "{jar_id}::{class_name}::{method_name}::{sha256(method_desc)[0:12]}"`

DB Join / Graph 节点禁止只用 `class_name/method_name`（会串台，尤其是重载）。

## 常用 SQL（用于 Proof / Freeze / Coverage）

### 1) desc resolution（禁止 LIKE 的前提）

```sql
SELECT DISTINCT callee_method_desc AS desc
FROM method_call_table
WHERE callee_class_name = ? AND callee_method_name = ?;
```

### 2) 精确 sink callers（Freeze 基线）

```sql
SELECT DISTINCT
  caller_jar_id, caller_class_name, caller_method_name, caller_method_desc,
  callee_jar_id, callee_class_name, callee_method_name, callee_method_desc
FROM method_call_table
WHERE callee_class_name = ? AND callee_method_name = ? AND callee_method_desc = ?;
```

### 3) 入口枚举（Reachability entry）

```sql
SELECT jar_id, class_name, method_name, method_desc, restful_type, path
FROM spring_method_table;
```

