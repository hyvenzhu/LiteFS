@echo off
set "JAVA_OPTS= -Xmx1g "
set "DIR=%~dp0%"

echo Using Duser.dir: %DIR%
echo Using CLASSPATH: %DIR%lib
echo Using JAVA_OPTS: %JAVA_OPTS%
echo Using LOG  PATH: %DIR%logs

java -Duser.dir=%DIR% %JAVA_OPTS% -classpath %DIR%LiteFS-1.0.jar com.rdinfo.fs.FileServer 2>&1 >> %DIR%logs\LiteFS.out