package com.activiti.r_and_d;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

/**
 * @author Joram Barrez
 */
public class SecureScripting02 {

    public static void main(String[] args) {

        // In this example process, instead of using the default Nashorn scripting engine,
        // we instantiate the Nashorn scripting engine ourselves in the SafeScriptTask02
        // (not the use of the usage of jdk.nashorn.* package - not really nice).
        // We follow the docs from https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/api.html
        // to make the script execution more secure by adding a 'ClassFilter' to the Nashorn engine.
        // This effectively acts as a white-list of approved classes that can be used in the script.
        //
        // When executed, the script won't be executed, as the exception thrown says
        // 'Exception in thread "main" java.lang.RuntimeException: java.lang.ClassNotFoundException: java.lang.System.out.println'
        //
        // Note that the ClassFilter is only available from JDK 1.8.0_40 (quite recent)
        //
        // It's a nice thing, but it does not solve the infinite loop/memory usage problems.

        ProcessEngine processEngine = createProcessEngine();
        processEngine.getRepositoryService().createDeployment().addClasspathResource("r_and_d/02.bpmn20.xml").deploy();
		processEngine.getRuntimeService().startProcessInstanceByKey("ScriptingTest02");
		System.out.println("SecureScripting02 DONE");

    }

    private static ProcessEngine createProcessEngine() {
        return ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
                .setJdbcDriver("org.h2.Driver")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .buildProcessEngine();
    }

}
