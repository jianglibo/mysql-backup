echo off

REM https://ss64.com/nt/for_cmd.html
SET _lport=8080

SET wdir=%~dp0
CD /D %wdir% 
SET wdirslash=%wdir:\=/%
SET pidfile=%wdir%bin\app.pid

IF EXIST %pidfile% SET /p apppid=<%pidfile%
IF DEFINED apppid ECHO About to kill process with pid %apppid%
IF DEFINED apppid taskkill /PID %apppid% /F /T
