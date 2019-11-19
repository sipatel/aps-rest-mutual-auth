This is a very simple command line application to test the clustering/monitoring in the Activiti Admin app

# Build test app

`build.sh`

This will build the test app and the activiti-cluster-addon jar. Results are both placed in _/target_ folder.

# Run it

Go to the _/target_ folder. 

Typically I create a subfolder (eg. _dev_ or _prod_) and put the jars in it.
Go to the Activiti Admin app, generate a config jar in the configuration tab, and place it in those subfolders.

Start up the test app:

`java -cp "./*" com.activiti.Main`

This boots up an Activiti Process Engine which every 10 seconds executes some processes and sends the data to the Admin app.

See it in action : [https://github.com/Alfresco/activiti-enterprise/blob/master/addons/docs/Multi-cluster-demo.mov](https://github.com/Alfresco/activiti-enterprise/blob/master/addons/docs/Multi-cluster-demo.mov)




