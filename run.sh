#!/bin/bash
# Script to compile and run the Java Swing Study Group System with FlatLaf

echo "Compiling Java Swing Application..."
mkdir -p bin
javac -cp "lib/sqlite-jdbc.jar:lib/flatlaf.jar:src" -d bin src/models/*.java src/db/*.java src/ui/*.java src/Main.java

if [ $? -eq 0 ]; then
    echo "Compilation successful. Starting the Swing GUI application..."
    java -cp "bin:lib/sqlite-jdbc.jar:lib/flatlaf.jar" Main
else
    echo "Compilation failed!"
fi
