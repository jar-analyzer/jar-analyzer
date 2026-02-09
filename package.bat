@echo off
setlocal
set "JAVA_HOME=C:\\Program Files\\Java\\jdk-1.8"

set "MAVEN_OPTS=-Dorg.slf4j.simpleLogger.defaultLogLevel=warn"
mvn -B clean package -DskipTests --file pom.xml

endlocal
