@echo off
title Todo List Application
echo ========================================
echo       TODO LIST APPLICATION
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed!
    echo Please install Java JDK 8 or higher
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Check if MySQL connector exists
if not exist "lib\mysql-connector-java-8.0.33.jar" (
    echo ERROR: MySQL Connector JAR not found!
    echo Please download from: https://dev.mysql.com/downloads/connector/j/
    echo and place in lib/ folder
    pause
    exit /b 1
)

echo Compiling Java files...
javac -cp "lib/*" -d bin src/*.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Running Todo List Application...
echo ========================================
echo.
java -cp "bin;lib/*" Main

pause