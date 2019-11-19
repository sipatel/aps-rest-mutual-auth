package com.activiti.r_and_d;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

/**
 * @author Joram Barrez
 */
public class SecureScripting03 {

    public static void main(String[] args) {

        // Same setup as previous example
        // This example simply demonstrates infinite loops

        ProcessEngine processEngine = createProcessEngine();
        processEngine.getRepositoryService().createDeployment().addClasspathResource("r_and_d/03.bpmn20.xml").deploy();
		processEngine.getRuntimeService().startProcessInstanceByKey("ScriptingTest03");
		System.out.println("SecureScripting03 DONE");

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
