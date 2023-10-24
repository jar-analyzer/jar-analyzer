@echo off
setlocal

rem set file name
set "jar-analyzer=jar-analyzer-2.1-beta.jar"

rem env
set "command=wmic os get freephysicalmemory"
set "jre_bin=jre\bin\java.exe"
set "cur_dir=%~dp0%"
set "jre_bin_abs="%cur_dir%%jre_bin%""
set "jar_file=lib\%jar-analyzer%"
set "jar_file_abs="%cur_dir%%jar_file%""

rem get free memory
for /f "skip=1" %%p in ('%command%') do ( 
  set "m=%%p"
  goto :done
)
:done
set /A "freemem = %m% / 1024"

rem use 2/3 free memory
set /A "heapsize = freemem * 2 / 3"

rem jvm args 
set "gc_args=-XX:+PrintGC -XX:+PrintGCTimeStamps"
set "other_args=-Dfile.encoding=UTF-8"
set "java_args=%gc_args% -Xmx%heapsize%M -Xms%heapsize%M %other_args%"

rem start jar
%jre_bin_abs% %java_args% -jar %jar_file_abs%

endlocal