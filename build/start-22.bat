@echo off
setlocal

rem set file name
set "jar-analyzer=jar-analyzer-3.1.jar"

rem env
set "jre_bin=jre\bin\java.exe"
set "cur_dir=%~dp0%"
set "jre_bin_abs="%cur_dir%%jre_bin%""

rem jvm args
set "other_args=-Dfile.encoding=UTF-8"
set "java_args=-XX:+UseZGC -Xms2g %other_args%"
set "java_cp=lib\%jar-analyzer%"
set "main_class=me.n1ar4.jar.analyzer.starter.Application"

rem support default metal win win-classic motif mac gtk cross aqua nimbus
set "theme_name=default"
rem http api server port
set "api_server_port=10032"
rem log level (debug info warn error)
set "log_level=info"
rem program args
set "program_args=--theme %theme_name% --port %api_server_port% --log-level %log_level%"

rem start jar
echo [*] RUN %jar-analyzer% ON JAVA 22
echo [*] JVM ARGS: %java_args%
%jre_bin_abs% %java_args% -cp %java_cp% %main_class% gui %program_args%

endlocal