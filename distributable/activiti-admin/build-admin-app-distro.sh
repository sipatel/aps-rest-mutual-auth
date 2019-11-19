#!/bin/bash

rootFolder=$(pwd)
sourcesFolder=$rootFolder/build
now=$(date +"%Y-%m-%d__%H_%M_%S") 

tempFolder=temp
targetFolder=target/$now
jarsFolder=$rootFolder/$targetFolder/jars
tomcatFolder=$rootFolder/$targetFolder/apache-tomcat
webappsFolder=$tomcatFolder/webapps
tomcatLibFolder=$tomcatFolder/lib
docsFolder=$rootFolder/$targetFolder/docs

activitiAdminFolder=$rootFolder/../../activiti-admin
testAppFolder=$rootFolder/../../testapps/activiti-cluster-cli-app

echo "Deleting temp folder"
cd $rootFolder
rm -rf $tempFolder


echo "Creating temp folder"
cd $rootFolder
mkdir $tempFolder
cd $tempFolder
tempfolder=$(pwd)


echo "Creating target folders "
cd $rootFolder
mkdir -p $targetFolder
cd $targetFolder
targetFolder=$(pwd)
cd $rootFolder
mkdir $jarsFolder
mkdir $jarsFolder/lib
cd $rootFolder
mkdir $docsFolder


echo "Copying Apache tomcat"
cd $rootFolder
mkdir $tomcatFolder
cp -r $sourcesFolder/apache-tomcat-7.0.53/* $tomcatFolder


echo "Generating trial licence"
cd $rootFolder
if [[ ! -f $HOME/.activiti/license-generator/license.properties ]]; then
	echo "Need license.properties in ./activiti/license-generator, but couldn't find it. Halting."
	exit -1
fi   
if [[ ! -f $HOME/.activiti/license-generator/secring.gpg ]]; then
	echo "Need secring.gpg in ./activiti/license-generator, but couldn't find it. Halting."
	exit -1
fi     
cp ../../activiti-license/license.jar $tempFolder
cp $HOME/.activiti/license-generator/license.properties $tempFolder
cp $HOME/.activiti/license-generator/secring.gpg $tempFolder
cd $tempFolder
startDate=`date +%Y%m%d`
endDate=`date -v +2m +%Y%m%d`
echo "Start date = $startDate, endDate = $endDate"
java -jar license.jar -f activiti.lic -h Alfresco -v 1.0ent -s $startDate -e $endDate -numberOfLicenses 4 -numberOfProcesses 1000 -numberOfEditors 10 -numberOfAdmins 3
STATUS=$?
if [ $STATUS -ne 0 ] 
then
   	echo "Error while creating license. Halting."
    exit -1
fi     
if [[ ! -f activiti.lic ]]; then
	echo "Error while creating license. Halting."
	exit -1
fi
cp activiti.lic $tomcatLibFolder


echo "Building Activiti Enterprise REST app from source"
cd $sourcesFolder
if [ -d "activiti-enterprise-source" ]; then
	cd activiti-enterprise-source
	git pull	
fi
if [ ! -d "activiti-enterprise-source" ]; then
	echo "Activiti Enterprise REST not yet cloned, cloning it now. Next time, a pull will be enough."
	git clone https://github.com/Alfresco/activiti-engine.git activiti-enterprise-source
	cd activiti-enterprise-source
fi
rm modules/activiti-rest/src/main/resources/*.lic
rm modules/activiti-engine/src/main/resources/*.lic
mvn -T 1C -Pcheck clean install -DskipTests
cd modules/activiti-webapp-rest2
mvn -T 1C clean package -DskipTests
cd target
find . -name '*.war' -execdir mv {} activiti-rest.war \;
cp activiti-rest.war $webappsFolder


echo "Generating Enterprise docs"
cd $sourcesFolder
cd activiti-enterprise-source
cd enterprise-docs
asciidoc --backend install bootstrap-3.3.0.zip
chmod +x *.sh
./generate-html.sh
cp *.html $docsFolder
cp -r images $docsFolder/images


echo "Copying Start scripts"
cd $rootFolder
cp $sourcesFolder/start-activiti.* $targetFolder
cp $sourcesFolder/start-activiti-*.sh $jarsFolder
cp $sourcesFolder/start-activiti-*.bat $jarsFolder


echo "Copying H2 database"
cd $rootFolder
mkdir $targetFolder/h2
cp $sourcesFolder/h2*.jar $targetFolder/h2
cp $sourcesFolder/start-h2.* $targetFolder/h2


echo "Copying configuration files"
cd $rootFolder
cp $sourcesFolder/activiti-admin.properties $tomcatFolder/lib
cp $sourcesFolder/activiti-rest.properties $tomcatFolder/lib
cp $sourcesFolder/activiti-cluster.properties $tomcatFolder/lib
cp $sourcesFolder/activiti-test-app.properties $jarsFolder

echo "Copying docs"
cd $rootFolder
cp $sourcesFolder/*.pdf $targetFolder


echo "Building Activiti Admin App"
cd $rootFolder
cp -r $activitiAdminFolder $tempFolder/activiti-admin
cd $tempFolder/activiti-admin
CURRENT_COMMIT=$(git log --pretty=format:'%h' -n 1)
rm src/main/resources/activiti.lic
./build-app.sh
STATUS=$?
if [ $STATUS -ne 0 ] 
then
    echo "Error while building Activiti Admin App. Halting."
    exit -1
else
	echo "Build succeeded. Copying files to $jarsFolder."    
	cp target/*.war $webappsFolder
	cd 
fi    


echo "Building Cluster Test Application"   
cd $rootFolder/$tempFolder
cd $tempFolder
cp activiti.lic $testAppFolder/src/main/resources
cd $rootFolder
cd $testAppFolder
mvn clean package
STATUS=$?
if [ $STATUS -ne 0 ] 
then
   	echo "Error while building Cluster test app. Halting."
    exit -1
fi     
cp target/activiti-cluster-cli-app.jar $jarsFolder/activiti-test-app.jar
rm $testAppFolder/src/main/resources/*.lic


echo "Zipping it"
cd /$targetFolder
cd ..
zip -r $now $now
mv $now.zip alfresco-activiti-5.15.1.$CURRENT_COMMIT.zip


echo "Deleting temp folder"
cd $rootFolder
rm -rf $tempFolder


echo "Done!"