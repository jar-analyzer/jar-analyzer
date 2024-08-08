@echo off
setlocal

set "jar-analyzer=jar-analyzer-2.24.jar"

rem jvm args
set "other_args=-Dfile.encoding=UTF-8"
set "java_args=-XX:+UseG1GC -Xms2g -XX:MaxGCPauseMillis=200 %other_args%"
set "java_cp=lib\%jar-analyzer%;lib\tools.jar"
set "main_class=me.n1ar4.jar.analyzer.starter.Application"

rem support default metal win win-classic motif mac gtk cross aqua nimbus
set "theme_name=default"
rem http api server port
set "api_server_port=10032"

rem get java home
if "%JAVA_HOME%"=="" (
    echo [-] JAVA_HOME NOT SET
    goto :end
) else (
    echo [*] JAVA_HOME : %JAVA_HOME%
)

rem start jar
"%JAVA_HOME%\bin\java.exe" %java_args% -cp %java_cp% %main_class% gui -t %theme_name% -p %api_server_port%

:end
endlocal