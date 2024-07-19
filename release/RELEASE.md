# RELEASE DOC

Use `Zulu JRE 8`
- https://www.azul.com/downloads/?version=java-8-lts&os=windows&architecture=x86-64-bit&package=jre#zulu

Use `Zulu JRE 21`
- https://www.azul.com/downloads/?version=java-21-lts&os=windows&architecture=x86-64-bit&package=jre#zulu

Steps:
- Run `check-version.bat` and update `pom.xml`
- Run `rasp/check-version.bat` and update `pom.xml`
- Check `me/n1ar4/jar/analyzer/starter/Const.java` version
- Check `build/*.bat` files
- Check `pom.xml` version tag
- Check `build.py` VERSION
- Check `build.yml` VERSION
- Github build
- Change `version.txt` on `OSS Browser`
