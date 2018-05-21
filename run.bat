echo off

REM https://ss64.com/nt/for_cmd.html
SET _lport=8080

SET wdir=%~dp0
CD /D %wdir% 
SET wdirslash=%wdir:\=/%
SET pidfile=%wdir%bin\app.pid

SET springParams=--spring.config.location=classpath:/application.yml,file:./application.yml

if [%1]==[] GOTO tryToFindJar
IF %1==stop GOTO stop

SET jarfile=%1
IF EXIST %jarfile% GOTO run
GOTO tryToFindJar


:stop
IF EXIST %pidfile% SET /p apppid=<%pidfile%
IF DEFINED apppid ECHO About to kill process with pid %apppid%
IF DEFINED apppid taskkill /PID %apppid% /F /T
GOTO :eof

:tryToFindJar
FOR /f "tokens=5" %%G IN ('netstat -aon ^|find /i "listening" ^| find "%_lport%"') DO taskkill /PID %%G /F /T
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
java -jar %jarfile% %springParams% --debug
 
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
 
 
REM FOR /f "tokens=5" %%G IN ('netstat -aon ^|find /i "listening" ^| find "%_lport%"') DO taskkill /PID %%G /F /T
REM FOR /f "tokens=4 delims=(=" %%G IN ('%_ping_cmd% ^|find "Ping"') DO echo Result is [%%G]
::echo on
:: FOR /f "tokens=5" %%G IN ('netstat -aon ^|find /i "listening" ^| find "8080"') DO SET _listening=%%G

::IF [%_listening%]!=[] ECHO "hello"
::echo %_listening%

::GOTO :eof

::SET springParams=%springParams% --spring.datasource.url=jdbc:hsqldb:file:%wdirslash%db/db;shutdown=true

::spring.config.name
::spring.config.location
::SET springParams=%springParams% --spring.config.name=application.yml

:: SET springParams=--spring.profiles.active=prod



::SET springParams=%springParams% --myapp.ssh.sshIdrsa=G:/cygwin64/home/Administrator/.ssh/id_rsa
::SET springParams=%springParams% --myapp.ssh.knownHosts=G:/cygwin64/home/Administrator/.ssh/known_hosts
