@echo off
setlocal

rem set file name
set "jar-analyzer=jar-analyzer-2.24.jar"

rem env
set "jre_bin=jre\bin\java.exe"
set "cur_dir=%~dp0%"
set "jre_bin_abs="%cur_dir%%jre_bin%""

rem jvm args
set "other_args=-Dfile.encoding=UTF-8"
set "java_cp=lib\%jar-analyzer%"
set "main_class=me.n1ar4.jar.analyzer.starter.Application"
set "jvm_gc=-XX:+UseZGC -XX:+UseCompressedOops"

rem support default metal win win-classic motif mac gtk cross aqua nimbus
set "theme_name=default"
rem http api server port
set "api_server_port=10032"

rem start jar
%jre_bin_abs% %jvm_gc% %other_args% -cp %java_cp% %main_class% gui -t %theme_name% -p %api_server_port%

endlocal