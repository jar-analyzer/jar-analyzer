#!/bin/sh

# set file name
jar_analyzer="jar-analyzer-2.1-beta.jar"

# env
command="free -m | awk 'NR==2{print \$7}'"
jre_bin="jre/bin/java"
cur_dir="$(dirname "$0")"
jre_bin_abs="$cur_dir/$jre_bin"
jar_file="lib/$jar_analyzer"
jar_file_abs="$cur_dir/$jar_file"

# get free memory
m=$(eval $command)

# use 2/3 free memory
heapsize=$((m * 2 / 3))

# jvm args
gc_args="-XX:+PrintGC -XX:+PrintGCTimeStamps"
other_args="-Dfile.encoding=UTF-8"
java_args="$gc_args -Xmx${heapsize}M -Xms${heapsize}M $other_args"

# start jar
"$jre_bin_abs" $java_args -jar "$jar_file_abs"