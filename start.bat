echo off
SET workingdir=%~dp0

SET javaParams=--spring.profiles.active=prod --spring.datasource.url=jdbc:hsqldb:file:%workdingdir%db/db;shutdown=true
 
if [%1]==[] goto tryToFindJar

SET jarfile=%1
IF EXIST %jarfile% (
	GOTO run
)

:tryToFindJar
SET jarfile="%workingdir%mysql-backup-boot.jar"
echo try to find jar in %jarfile%  ......
IF EXIST %jarfile% (
	echo found %jarfile%, and start it.....
 	GOTO run
)
SET jarfile="%workingdir%build\libs\mysql-backup-boot.jar"
echo try to find jar in %jarfile% ......
IF EXIST %jarfile% (
	echo found %jarfile%, and start it......
 	GOTO run
)
goto :eof

:run
echo "run command java -jar %jarfile% %javaParams%"
java -jar %jarfile% %javaParams%
 
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