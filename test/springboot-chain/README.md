# springboot-chain

jar-analyzer 黄金测试目标项目（DFS 调用链 / EL 搜索）。

设计的两条链路（SINK 均为 `me/n1ar4/chain/util/CmdUtil.exec`）：

- 链路 A（精确）：`web/DirectController.exec` → `service/CmdService.handle` → `util/CmdUtil.exec`
- 链路 B（接口）：`web/IfaceController.exec` → `service/GadgetService.trigger`（接口）→ `service/GadgetServiceImpl.trigger` → `gadgetWork` → `util/CmdUtil.exec`

由 `.github/workflows/test-golden-chain.yml` 构建为 `chain-test.jar` 供 `ChainGoldenTest` 使用。
