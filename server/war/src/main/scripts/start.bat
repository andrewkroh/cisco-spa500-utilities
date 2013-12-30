@echo off

rem Change directory and drive to script location:
cd /d %~dp0

set "SCRIPT_HOME=%CD%"

wildfly-8.0.0.CR1\bin\standalone.bat ^
    -P phones.properties ^
    -Dphone.config.dir=%SCRIPT_HOME%\config ^
    -Djboss.bind.address=0.0.0.0
