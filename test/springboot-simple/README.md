# springboot-simple

jar-analyzer 黄金测试目标项目（字符串搜索 / Spring 入口发现）。

- 常量 `GOLDEN-MARKER-1984` 被 javac 内联进 `util/SecretHolder` 的 `leak()` 的 LDC 中
- `web/InfoController` 的 `/info` 是唯一的 Spring 入口

由 `.github/workflows/test-golden-simple.yml` 构建为 `simple-test.jar` 供 `SimpleGoldenTest` 使用。
