# RELEASE DOC

Use `Eclipse Temurin JRE 8`
- https://adoptium.net/zh-CN/temurin/releases/?os=windows&arch=x64&package=jre&version=8

Use `Eclipse Temurin JRE 21`
- https://adoptium.net/zh-CN/temurin/releases/?os=windows&arch=x64&package=jre&version=21

Jar Analyzer Workflow JRE Repo
- https://github.com/jar-analyzer/workflow_jre

Steps:
- test all functions
- run `go run .\github\main.go proxy`
- Run `me/n1ar4/support/Contributor.java` to generate thanks.txt
- Run `check-version.bat` and update `pom.xml`
- Check `me/n1ar4/jar/analyzer/starter/Const.java` version
- Check `build/*.bat` files
- Check `pom.xml` version tag
- Check `build.py` VERSION
- Check `build.yml` VERSION
- Github build
- Change `version.txt` on `OSS Browser`
