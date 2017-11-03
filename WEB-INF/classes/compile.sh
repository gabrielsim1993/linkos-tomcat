#!/usr/bin/env bash
echo Compiling classes...
javac -classpath .:/usr/local/Cellar/tomcat/8.5.23/libexec/lib/*:/usr/local/Cellar/tomcat/8.5.23/libexec/webapps/workato/WEB-INF/lib/ZSDK_API.jar *.java
echo Done!
