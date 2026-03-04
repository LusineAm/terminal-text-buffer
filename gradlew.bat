@echo off
setlocal

set "SCRIPT_DIR=%~dp0"

java -classpath "%SCRIPT_DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
