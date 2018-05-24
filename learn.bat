::netstat -aon | find /i "8080"
::taskkill /PID 24548 /F
SET _lport=8080
:: FOR /F .\build\libs\ %%G IN (*boot.jar) DO SET jf=%%G

SET _dir=.\build\libs\*boot.jar

SET jarfile=abchello
echo %jarfile%
SET jarfile=%jarfile:abc=%

echo %jarfile%

FOR /F "tokens=4" %%G IN ('dir %_dir%') DO IF NOT %jarfile:bootjar=% == %jarfile% SET jarfile=%%G

ECHO %jarfile%

::FOR /f "tokens=5" %%G IN ('netstat -aon ^| find "%_lport%"') DO ECHO %%G