package com.activiti.r_and_d;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

/**
 * @author Joram Barrez
 */
public class SecureScripting05 {

    public static void main(String[] args) {

        // In this example (infinite loop) the SafeScriptTask05 has been modified
        // to use the instructionCount to stop execution after 3 seconds.
        //
        // Note that the 'instructionCount' is the count of instructions for the *byte code* instructions
        // so for one line of scripting there will be a lot (hundreds, even thousands) byte code instructions.

        ProcessEngine processEngine = createProcessEngine();
        processEngine.getRepositoryService().createDeployment().addClasspathResource("r_and_d/05.bpmn20.xml").deploy();
		processEngine.getRuntimeService().startProcessInstanceByKey("ScriptingTest05");
		System.out.println("SecureScripting05 DONE");

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
