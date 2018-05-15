echo off
SET wdir=%~dp0
SET wdirslash=%wdir:\=/%
SET pidfile=%wdir%bin\app.pid

SET springParams=--spring.profiles.active=prod
SET springParams=%springParams% --spring.datasource.url=jdbc:hsqldb:file:%wdirslash%db/db;shutdown=true

if [%1]==[] GOTO tryToFindJar
IF %1==stop GOTO stop

SET jarfile=%1
IF EXIST %jarfile% GOTO run
GOTO tryToFindJar


:stop
IF EXIST %pidfile% SET /p apppid=<%pidfile%
IF DEFINED apppid taskkill /PID %apppid% /F /T
GOTO :eof

:tryToFindJar
SET jarfile="%wdir%mysql-backup-boot.jar"
echo try to find jar in %jarfile%  ......
IF EXIST %jarfile% (
	echo found %jarfile%, and start it.....
 	GOTO run
)
SET jarfile="%wdir%build\libs\mysql-backup-boot.jar"
echo try to find jar in %jarfile% ......
IF EXIST %jarfile% (
	echo found %jarfile%, and start it......
 	GOTO run
)
GOTO :eof

:run
echo "run command java -jar %jarfile% %springParams%"
java -jar %jarfile% %springParams%
 
REM echo %mypath:~0,-1%
REM set arg1= %1
REM set arg2= %2
REM shift
REM shift
REM echo  "/u %arg1% /p %arg2% %*"

REM @echo off
REM setlocal
REM set "list=a b c d"

 REM for %%i in (%*) do (
REM   echo(%%i
  REM echo(
 REM )