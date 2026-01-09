#!/bin/bash

echo "========================================"
echo "       TODO LIST APPLICATION"
echo "========================================"
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed!"
    echo "Please install Java JDK 8 or higher"
    echo "Ubuntu/Debian: sudo apt install openjdk-11-jdk"
    echo "macOS: brew install openjdk"
    exit 1
fi

# Check if MySQL connector exists
if [ ! -f "lib/mysql-connector-java-8.0.33.jar" ]; then
    echo "ERROR: MySQL Connector JAR not found!"
    echo "Please download from: https://dev.mysql.com/downloads/connector/j/"
    echo "and place in lib/ folder"
    exit 1
fi

echo "Compiling Java files..."
mkdir -p bin
javac -cp "lib/*" -d bin src/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo
echo "========================================"
echo "Running Todo List Application..."
echo "========================================"
echo
java -cp "bin:lib/*" Main