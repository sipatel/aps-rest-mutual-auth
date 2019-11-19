set JAVA_OPTS=-Xms256m -Xmx1024m -XX:PermSize=256m
cd ./h2
start "H2 Database" start-h2.bat
cd ../apache-tomcat/bin
start "Activiti Admin App" catalina.bat run
cd ../../jars
start "Activiti Test App" start-activiti-testapp.bat
