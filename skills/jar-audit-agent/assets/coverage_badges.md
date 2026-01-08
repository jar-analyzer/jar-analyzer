# Coverage / INCOMPLETE Badges (Optional)

> 这些片段用于报告模板或人工复核时的“统一标记”。本文件本身不会被引擎强依赖。

## INCOMPLETE（未完成覆盖）

> ❗ **INCOMPLETE**：本报告未完成覆盖（Verified < Emitted）。
> 
> - 规则：只有 `status=VULN/SAFE` 才计入 Verified；`NEEDS_DEEPER` 仍算 Remaining。
> - 下一步：按报告中的 **Remaining TopK** 执行 `next → evidence --auto-fallback → submit` 直到 Remaining=0，或在 strict=false 下保留未完成标记。

## COMPLETE（已完成覆盖）

> ✅ **COMPLETE**：本报告已完成覆盖（Verified == Emitted）。

## COVERAGE（进度行）

- Coverage = Verified / Emitted
- Reachable 仅作优先级参考，不影响 Coverage 口径


