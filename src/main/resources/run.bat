@echo off
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar ytm-addon-java-1.0.4.jar
pause