@echo off
REM Script to compile and run the Java Swing Study Group System on Windows

echo Compiling Java Swing Application...
if not exist bin mkdir bin

javac -cp "lib/sqlite-jdbc.jar;lib/flatlaf.jar;src" -d bin src/models/*.java src/db/*.java src/ui/*.java src/Main.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful. Starting application...
    java -cp "bin;lib/sqlite-jdbc.jar;lib/flatlaf.jar" Main
) else (
    echo Compilation failed!
    pause
)
