@echo off

:: To change the server port depending on you system requirement and --server.port=[port] on the next line. For example if you want to use port 9090 the command will have the following
:: form:
:: start cmd /k "cd lib && java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -jar demo-0.0.1-SNAPSHOT.jar -Xmx=1G --server.port=9090"

start cmd /k "cd lib && java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -jar demo-0.0.1-SNAPSHOT.jar -Xmx=1G"
ping -n 10 127.0.0.1 >nul
start http://localhost:8080


