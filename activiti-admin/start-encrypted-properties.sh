export MAVEN_OPTS="$MAVEN_OPTS -Dserver.port=8081 -Xms512m -Xmx1024m -XX:MaxPermSize=512m -Xdebug -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=n"
mvn -Pdev -DskipTests -Dfile.encoding=UTF-8 clean -Djasypt.encryptor.password=supersecretz -Djasypt.encryptor.algorithm=PBEWITHSHA1ANDDESEDE spring-boot:run -Dlog4j.configuration=log4j.properties
