#!/bin/sh

if [ -n "$JAVA8_HOME" ]; then
    JAVA_HOME="$JAVA8_HOME"
elif [ -x /usr/libexec/java_home ]; then
    JAVA_HOME=$(/usr/libexec/java_home -v 1.8 2>/dev/null)
fi

if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
    echo "java 8 not found, set JAVA8_HOME or JAVA_HOME first"
    exit 1
fi
export JAVA_HOME

echo "JAVA_HOME=$JAVA_HOME"
"$JAVA_HOME/bin/java" -version 2>&1

exec mvn versions:display-dependency-updates
