::netstat -aon | find /i "8080"
::taskkill /PID 24548 /F
SET _lport=8080

IF NOT [%1] == [] SET _lport=%1
ECHO %_lport%
FOR /f "tokens=5" %%G IN ('netstat -aon ^| find "%_lport%"') DO taskkill /PID %%G /F /T
::FOR /f "tokens=5" %%G IN ('netstat -aon ^| find "%_lport%"') DO ECHO %%G