@echo off
setlocal

set "jar-analyzer=jar-analyzer-3.1.jar"

rem jvm args
set "other_args=-Dfile.encoding=UTF-8"
set "java_args=-XX:+UseG1GC -Xms2g -XX:MaxGCPauseMillis=200 %other_args%"
set "java_cp=lib\%jar-analyzer%;lib\tools.jar"
set "main_class=me.n1ar4.jar.analyzer.starter.Application"

rem support default metal win win-classic motif mac gtk cross aqua nimbus
set "theme_name=default"
rem http api server port
set "api_server_port=10032"
rem log level (debug info warn error)
set "log_level=info"
rem program args
set "program_args=--theme %theme_name% --port %api_server_port% --log-level %log_level%"

rem get java home
if "%JAVA_HOME%"=="" (
    echo [-] JAVA_HOME NOT SET
    goto :end
) else (
    echo [*] JAVA_HOME : %JAVA_HOME%
)

rem start jar
echo [*] JVM ARGS: %java_args%
"%JAVA_HOME%\bin\java.exe" %java_args% -cp %java_cp% %main_class% gui %program_args%

:end
endlocal