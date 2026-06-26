@echo off
echo ========================================
echo   CitaFacil EsSalud - FRONTEND (puerto 5173)
echo ========================================
cd /d "%~dp0frontend"
call npm run dev
pause
