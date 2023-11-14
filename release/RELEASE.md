# RELEASE DOC

Steps:
- Run `check-version.bat` and update `pom.xml`
- Check `me/n1ar4/jar/analyzer/starter/Const.java` version
- Check build bat/sh files
- Check `pom.xml` version tag
- Check `build.py` VERSION
- Run `mvn -B package -DskipTests --file pom.xml`
- Run `python build.py`
- Copy `JRE` to `embed` type
- Change `version.txt` on server