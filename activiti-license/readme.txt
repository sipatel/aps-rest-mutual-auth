Install

Make sure there's a license.properties and secring.gpg in the same folder as license.jar.
These files can be downloaded from Alfresco TS:

https://ts.alfresco.com/share/page/site/activiti/documentlibrary?file=password#filter=path%7C%2FActiviti%2520license%2520generator%7C&page=1

Execute license generator

Expects license file information in the following format:

java -jar license.jar -file FILE_NAME -holder CUSTOMER_NAME -version APP_VERSION -start VALID_FROM (yyyyMMdd) -end VALID_UNTIL (yyyyMMdd) -numberOfLicenses 1 -numberOfProcesses 100 -numberOfEditors 5 -numberOfAdmins 1 -numberOfApps 1 -multiTenant false -defaultTenant alfresco.com
java -jar license.jar -f FILE_NAME -h CUSTOMER_NAME -v APP_VERSION -s VALID_FROM (yyyyMMdd) -e VALID_UNTIL (yyyyMMdd) -numberOfLicenses 1 -numberOfProcesses 100 -numberOfEditors 5 -numberOfAdmins 1 -multiTenant false -defaultTenant alfresco.com

example:

java -jar license.jar -f johndoe.lic -h John Doe Ltd -v 1.0ent -s 20140401 -e 20150401 -numberOfLicenses 2 -multiTenant false -defaultTenant alfresco

The file name (-file or -f), customer name (-holder or -h), application version (-version or -v) and end date (-end or -e) are required.
The default values of the other parameters are:

- valid from --> today
- numberOfLicenses --> 1
- numberOfProcesses --> 100
- numberOfEditors --> 5 
- numberOfAdmins --> 1
- numberOfApps --> 0
- multiTenant --> false
- defaultTenant --> test
