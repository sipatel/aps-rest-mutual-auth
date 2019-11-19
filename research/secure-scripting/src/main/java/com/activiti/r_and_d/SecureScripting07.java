package com.activiti.r_and_d;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

/**
 * @author Joram Barrez
 */
public class SecureScripting07 {

    public static void main(String[] args) {

        // In this example, we try to grab a lot of memory.
        // To fix it, we need to execute the logic on a separate thread, as in the JDK
        // the only thing where you can check the allocated bytes is a Thread. We're using
        // the MxBean to get the allocated bytes on the instruction count callback.
        //
        // Note that we have to cast the MxBean to com.sun.management.ThreadMXBean,
        // as the regular one does not have the getThreadAllocatedBytes() method.
        // It seems OpenJDK has this method too.
        //
        // Note that having a separate thread does have a performance impact of course.
        //

        ProcessEngine processEngine = createProcessEngine();
        processEngine.getRepositoryService().createDeployment().addClasspathResource("r_and_d/07.bpmn20.xml").deploy();
		processEngine.getRuntimeService().startProcessInstanceByKey("ScriptingTest07");
		System.out.println("SecureScripting07 DONE");

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
