package com.activiti.r_and_d;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

/**
 * @author Joram Barrez
 */
public class SecureScripting06 {

    public static void main(String[] args) {

        // In this example we keep calling a method, thus resulting in a deep nested stack.
        // We'll use the Rhino feature to limit the stack here to 10.
        //
        // The exception is 'Exceeded maximum stack depth'

        ProcessEngine processEngine = createProcessEngine();
        processEngine.getRepositoryService().createDeployment().addClasspathResource("r_and_d/06.bpmn20.xml").deploy();
		processEngine.getRuntimeService().startProcessInstanceByKey("ScriptingTest06");
		System.out.println("SecureScripting06 DONE");

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
