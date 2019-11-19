package com.activiti.performance;

import com.activiti.impl.bpmn.behavior.SecureScriptTaskActivityBehaviorConfigurator;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joram Barrez
 */
public class SecureScriptTaskExecution05 {

    public static void main(String[] args) {

        // Enable class white listing + max execution time + for each byte code instruction a check + max stack depth

        // Seems to run faster? Wtf? Optimization level?

        SecureScriptTaskActivityBehaviorConfigurator configurator = new SecureScriptTaskActivityBehaviorConfigurator()
                .setEnableClassWhiteListing(true)
                .addWhiteListedClass("org.activiti.engine.impl.persistence.entity.ExecutionEntity")
                .setMaxScriptExecutionTime(2000L)
                .setNrOfInstructionsBeforeStateCheckCallback(1)
                .setMaxStackDepth(4);

        ProcessEngine processEngine = new StandaloneInMemProcessEngineConfiguration()
                .addConfigurator(configurator)
                .buildProcessEngine();

        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.createDeployment().addClasspathResource("performance/performance.bpmn20.xml").deploy();

        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("a", 123);
        variables.put("b", 456);

        int nrOfRuns = 1000;
        long start = System.currentTimeMillis();
        for (int i=0; i<nrOfRuns; i++) {
            runtimeService.startProcessInstanceByKey("default", variables);
        }
        long end = System.currentTimeMillis();
        long totalTime = end-start;
        System.out.println("Total time = " + totalTime + " ms");
        System.out.println("Avg time/process instance = " + ( (double)totalTime/(double)nrOfRuns) + " ms");
    }
}
