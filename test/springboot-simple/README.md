# springboot-simple

jar-analyzer 黄金测试目标项目（字符串搜索 / Spring 入口发现）。

- 常量 `GOLDEN-SECRET-1984` 出现在 `util/SecretHolder` 的 `<clinit>` 与 `leak()` 两处
- `web/InfoController` 的 `/info` 是唯一的 Spring 入口

由 `.github/workflows/test-golden-simple.yml` 构建为 `simple-test.jar` 供 `SimpleGoldenTest` 使用。
