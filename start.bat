ECHO OFF
SETLOCAL

REM https://ss64.com/nt/for_cmd.html
REM http://steve-jansen.github.io/guides/windows-batch-scripting/part-3-return-codes.html
REM http://steve-jansen.github.io/guides/windows-batch-scripting/part-7-functions.html
REM https://ss64.com/nt/setlocal.html
SET _lport=8080

SET wdir=%~dp0
CD /D %wdir% 
SET wdirslash=%wdir:\=/%
SET pidfile=%wdir%bin\app.pid

:loop
SET _up=_upgrade.properties

IF EXIST %_up% (
	FOR /F "tokens=1,2 delims==" %%G IN ('TYPE %_up% ^| FINDSTR /R "^.*=.*$"') DO SET %%G=%%H
)

SET _db=%wdirslash%dbdata/db

:: In addition to application.properties files, profile-specific properties can also be defined by using the following naming convention: application-{profile}.properties. The Environment has a set of default profiles (by default, [default]) that are used if no active profiles are set. In other words, if no profiles are explicitly activated, then properties from application-default.properties are loaded.

:: Profile-specific properties are loaded from the same locations as standard application.properties, with profile-specific files always overriding the non-specific ones, whether or not the profile-specific files are inside or outside your packaged jar.

:: If several profiles are specified, a last-wins strategy applies. For example, profiles specified by the spring.profiles.active property are added after those configured through the SpringApplication API and therefore take precedence.

:: [Note]
:: If you have specified any files in spring.config.location, profile-specific variants of those files are not considered. Use directories in spring.config.location if you want to also use profile-specific properties.

SET springParams=--spring.config.location=classpath:/application.properties,file:./application.properties

:: IF DEFINED upgrade-jar SET _db=%wdirslash%dbdata.prev/db
:: SET springParams=%springParams% ----spring.profiles.active=prod
SET springParams=%springParams% --spring.datasource.url=jdbc:hsqldb:file:%_db%;shutdown=true

SETLOCAL EnableDelayedExpansion
IF DEFINED upgrade-jar (
	ECHO %upgrade-folder%
	CALL :findrun %upgrade-folder%
) ELSE (
	ECHO CALL :findrun %wdir%
	CALL :findrun %wdir%
	IF !ERRORLEVEL! == 33 (
		ECHO CALL :findrun "%wdir%build\libs\" 
		CALL :findrun "%wdir%build\libs\"
	)
)

ECHO exit with code %ERRORLEVEL%

IF %ERRORLEVEL% == 101 (
	ECHO restarting....
	SET "upgrade-jar="
	GOTO :loop
)

EXIT /B %ERRORLEVEL%

REM :tryToFindJar
REM FOR /f "tokens=5" %%G IN ('netstat -aon ^|find /i "listening" ^| find "%_lport%"') DO taskkill /PID %%G /F /T
REM CALL :findrun %wdir%
REM ECHO tryToFindJar %ERRORLEVEL%
REM IF NOT %ERRORLEVEL% == 0 CALL :findrun "%wdir%build\libs\"
REM EXIT /B %ERRORLEVEL%

:findrun
SET jarfile=NOT_EXISTS_FILE
SETLOCAL EnableDelayedExpansion
FOR /F "tokens=4" %%G IN ('dir %~1 ^| FINDSTR /R "mysql-backup-.*-boot.jar$"') DO SET jarfile=%%G
SET jarfile=%~1%jarfile%
IF EXIST %jarfile% (
	ECHO found %jarfile%, and start it......
	ECHO ...................................
	ECHO java -jar %jarfile% %springParams% 
	java -jar %jarfile% %springParams%
	EXIT /B !ERRORLEVEL!
) ELSE (
	EXIT /B 33
)