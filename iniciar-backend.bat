@echo off
echo ========================================
echo   CitaFacil EsSalud - BACKEND (puerto 8080)
echo ========================================
cd /d "%~dp0backend"
call mvnw.cmd spring-boot:run
pause
