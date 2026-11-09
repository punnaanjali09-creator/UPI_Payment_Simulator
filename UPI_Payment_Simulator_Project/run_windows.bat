@echo off
if not exist out mkdir out
javac -d out src\com\upisimulator\Main.java src\com\upisimulator\model\*.java src\com\upisimulator\service\*.java src\com\upisimulator\exception\*.java src\com\upisimulator\ui\*.java src\com\upisimulator\util\*.java
if errorlevel 1 pause & exit /b 1
java -cp out com.upisimulator.Main
pause
