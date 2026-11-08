@echo off
cd /d "%~dp0"
call gradlew.bat clean build --console=plain
exit /b %errorlevel%
