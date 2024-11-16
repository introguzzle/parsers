@echo off

IF EXIST "apidocs" (
    rmdir /S /Q "apidocs"
)

robocopy "target\reports\apidocs" "apidocs" /E
