# RELEASE DOC

Use `Eclipse Temurin JRE 8`

- https://adoptium.net/zh-CN/temurin/releases/?os=windows&arch=x64&package=jre&version=8

Use `Eclipse Temurin JRE 25`

- https://adoptium.net/zh-CN/temurin/releases/?version=25&package=jre&os=windows&arch=x64

Jar Analyzer Workflow JRE Repo

- https://github.com/jar-analyzer/workflow_jre

Steps:

- Sync `CHANGELOG.MD` file to `src/main/resources`
- Edit `thanks.txt` and `thanks.md`
- Run `check-version.bat` and update `pom.xml`
- Check `me/n1ar4/jar/analyzer/starter/Const.java` version
- Check `build/*.bat` files
- Check `pom.xml` version tag
- Check `build.py` VERSION
- Check `build.yml` VERSION
- Run git commit and push
- Github Action Build
- 严格测试步骤：确保可用性，然后进行正式发布
- Change `version.txt` on `OSS Browser`
- 更新 `RELEASE` 内容到蓝奏云
- UPDATE `README` version count
- 更新 `README` 最新版蓝奏云地址
- 更新官方文档：版本数量，蓝奏云地址，修改时间

Test Steps:

- Analyze `jar-analyzer.jar`
- 一键 `SINK RCE/JNDI` 验证：确保有输出
- `Chains` 空 `SOURCE` 列举看最长
- `EL` 搜索 `Runtime Exec` 确认有效
- `DIFF` 两版本 `TOMCAT` 确认有效
