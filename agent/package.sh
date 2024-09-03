set MAVEN_OPTS="-Dorg.slf4j.simpleLogger.defaultLogLevel=warn"
mvn -B clean package -DskipTests --file pom.xml
