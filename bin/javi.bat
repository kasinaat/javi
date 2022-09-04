@echo off
set CLASSPATH=..\lib\*;
if EXIST "%JAVA_HOME%" set JAVA=%JAVA_HOME%\bin\javaw
echo %1
if [%1] == [] goto END_USAGE

"%JAVA%" -cp %CLASSPATH% org.taanisak.javi.Main %1
GOTO END
:END_USAGE
echo "Usage javi <filename>"

:END