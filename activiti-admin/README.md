# Process Services Admin App

## Development

### Running

If running for the first time then a new database `activitiadmin` with `utf8_bin` collation should be created.

Run the *start.sh* script

By default application is configured to run at port `8081`, 
you can access admin app by navigating to:

```text
http://localhost:8081/activiti-admin
```

### Integrating with Activiti Instance

If you are running `activiti-bpm-suit` locally with default settings then you might need changing admin configuration.


Go to the engine `Configuration` page (`http://localhost:8081/activiti-admin/#/engine`) 
and click `Edit Activiti REST endpoint`.

Ensure the settings are:

| Name | Value |
| --- | --- |
| Server address | http://localhost |
| Server port | 9999 |
| Context root | activiti-app |
| REST root | api |
| Username | admin@app.activiti.com (default Activiti admin login) |
| Password | k1ngk0ng (default Activiti admin password) |
| URL preview | http://localhost:9999/activiti-app/api |

Click `Save endpoint configuration` to apply changes.

You can also use `Check Activiti REST endpoint` button on `Configuration` page in order to check updated configuration.

## Build the war

Run the *build-app.sh* script.

After successful build, the war can be found in _/target_.

This folder also contains an executable jar. Run it as follows

> java -jar activiti-admin-1.0-SNAPSHOT-war-exec.jar -httpPort=9000
