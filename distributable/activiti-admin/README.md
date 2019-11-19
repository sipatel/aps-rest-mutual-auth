# BUILD INSTRUCTIONS: ACTIVITI ADMIN DISTRIBUTABLE 

## Contents

The Activiti Admin demo distributable contains following items

* An in-memory H2 database (backed by a file, no data is lost on shutdown)
* An Apache Tomcat webcontainer with
	* The Alfresco Activiti Admin App
	* The Activiti REST server (Enterprise), exposing the data in the Activiti database
	* A 60-day trial license for both in /lib
* A Cluster test application that mimics real-life usage of the Activiti engine. Multiple instances of this test application can be started to test drive the cluster functionality in the Alfresco Activiti Admin application.

### The Script

The script _build-admin-app-distro.sh_ build all pieces described above. After it is finished, a new folder with the timestamp of the build will be created and zipped in the _target_ folder.

It will do the following

* Copy Apache Tomcat
* Generate a trial license that is 60 days valid
* Clone (or update) the Activiti enterprise engine 
* Remove the development license file
* Build the enterprise rest app
* Copy all start scripts
* Copy properties files to /lib of Tomcat
* Copy docs to root
* Removes the development license from the Admin app source
* Build the Admin app
* Build the cluster test app
* Zip it all
 
### Argument when running the executable jars

* -httpPort=8081 for the Activiti Admin App
* -extractDirectory=activiti-rest-extracted for (or clash between extracted folders otherwise)
* -resetExtract to be sure





