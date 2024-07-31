@echo off
setlocal

rem set file name
set "jar-analyzer=jar-analyzer-2.24.jar"

rem env
set "command=wmic os get freephysicalmemory"

rem get free memory
for /f "skip=1" %%p in ('%command%') do ( 
  set "m=%%p"
  goto :done
)
:done
set /A "freemem = %m% / 1024"

rem use %50 free memory
set /A "heapsize = freemem / 2"

rem jvm args
set "other_args=-Dfile.encoding=UTF-8"
set "no_agent_args=-XX:+DisableAttachMechanism"
set "java_args=%no_agent_args% -Xmx%heapsize%M -Xms%heapsize%M %other_args%"
set "java_cp=lib\%jar-analyzer%;lib\tools.jar"
set "main_class=me.n1ar4.jar.analyzer.starter.Application"
set "agent_path=lib\rasp.jar"
set "boot_args=-Xbootclasspath/a:%agent_path%"
set "java_agent=-javaagent:%agent_path%"

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
"%JAVA_HOME%\bin\java.exe" %java_agent% %boot_args% %java_args% -cp %java_cp% %main_class% gui -t %theme_name% -p %api_server_port%

:end
endlocal