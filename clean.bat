@echo off
set "TARGET_DIR=release"

echo delete files...
for /r "%TARGET_DIR%" %%F in (*) do (
    if not "%%~nxF"=="RELEASE.md" (
        if not "%%~nxF"==".gitkeep" (
            echo delete "%%F"
            del "%%F" /q
        )
    )
)

echo delete dirs...
for /f "delims=" %%D in ('dir /ad /b /s "%TARGET_DIR%" ^| sort /r') do (
    rd "%%D" 2>nul
)

echo clean finish