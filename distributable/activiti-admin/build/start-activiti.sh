#!/bin/sh
JAVA_OPTS="-Xms256m -Xmx1024m -XX:PermSize=256m"
export JAVA_OPTS
trap "kill 0" SIGINT SIGTERM EXIT
	cd ./h2
	sh ./start-h2.sh &
	cd ../apache-tomcat/bin
	sh ./catalina.sh run &
	cd ../../jars
	sh ./start-activiti-testapp.sh &
    wait
