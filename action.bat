@echo off
setlocal enabledelayedexpansion
set arg=%1

if "%arg%"=="clean" (
    go run .\github\delete-runs\main.go
    go run .\github\delete-builds\main.go
    go run .\github\delete-caches\main.go
)