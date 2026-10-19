@echo off
chcp 65001 >nul
:: ==========================================================
:: Script khoi chay nhanh DHOPM Stream Visualizer tren Windows
:: Truong Dai Hoc Da Lat - Khoa CNTT
:: Sinh vien: Nguyen Thanh Tam - MSSV: 2312741
:: ==========================================================

cd /d "%~dp0"

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [LOI] Khong tim thay lenh 'mvn' (Apache Maven) trong he thong!
    echo Vui long cai dat Maven va them vao PATH hoac su dung IDE (IntelliJ IDEA / Eclipse / VS Code).
    echo Huong dan cai Maven tren Windows: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

if "%1"=="test" (
    echo [INFO] Dang chay 41 Unit Tests kiem thu tu dong...
    mvn test
    pause
    exit /b 0
)

if "%1"=="verify" (
    echo [INFO] Dang chay Lab 1 Verification Runner (doi chieu so lieu)...
    mvn compile exec:java -Dexec.mainClass="vn.edu.dlu.dhopm.core.Lab1VerificationRunner"
    pause
    exit /b 0
)

echo [INFO] Dang khoi chay giao dien DHOPM Stream Visualizer (JavaFX)...
mvn javafx:run

if %errorlevel% neq 0 (
    echo.
    echo [CHU Y] Neu gap loi JavaFX hoac JDK, hay chac chan may da cai JDK 17 tro len.
    pause
)
