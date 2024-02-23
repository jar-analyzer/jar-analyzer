#!/bin/sh

# set file name
jar_analyzer="jar-analyzer-2.11.jar"

# env
command="free -m | awk 'NR==2{print \$7}'"

# get free memory
m=$(eval $command)

# use 2/3 free memory
heapsize=$((m * 2 / 3))

# jvm args
gc_args="-XX:+PrintGC -XX:+PrintGCTimeStamps"
no_agent_args="-XX:+DisableAttachMechanism"
other_args="-Dfile.encoding=UTF-8"
main_class="me.n1ar4.jar.analyzer.starter.Application"
java_cp="lib/$jar_analyzer:lib/jar-analyzer-rasp-agent-jar-with-dependencies.jar"
java_args="$gc_args $no_agent_args -Xmx${heapsize}M -Xms${heapsize}M $other_args"
java_agent="-javaagent:lib/jar-analyzer-rasp-agent-jar-with-dependencies.jar"

# start jar
java $java_agent $java_args -cp $java_cp $main_class gui