package com.activiti.test.secure_scripting;

import com.activiti.impl.bpmn.behavior.SecureScriptCapableActivityBehaviorFactory;
import com.activiti.impl.bpmn.behavior.SecureScriptClassShutter;
import com.activiti.impl.bpmn.behavior.SecureScriptTaskActivityBehaviorConfigurator;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Joram Barrez
 */
public abstract class SecureScriptingBaseTest {

    protected ProcessEngine processEngine;
    protected RuntimeService runtimeService;
    protected RepositoryService repositoryService;
    protected TaskService taskService;

    @Before
    public void initProcessEngine() {

        SecureScriptTaskActivityBehaviorConfigurator configurator = new SecureScriptTaskActivityBehaviorConfigurator()
                .setWhiteListedClasses(new HashSet<>(Arrays.asList("java.util.ArrayList")))
                .setMaxStackDepth(10)
                .setMaxScriptExecutionTime(3000L)
                .setMaxMemoryUsed(3145728L);

        this.processEngine = new StandaloneInMemProcessEngineConfiguration()
                .addConfigurator(configurator)
                .setDatabaseSchemaUpdate("create-drop")
                .buildProcessEngine();

        this.runtimeService = processEngine.getRuntimeService();
        this.repositoryService = processEngine.getRepositoryService();
        this.taskService = processEngine.getTaskService();
    }

    @After
    public void shutdownProcessEngine() {

        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }

        this.taskService = null;
        this.repositoryService = null;
        this.runtimeService = null;

        this.processEngine.close();
        this.processEngine = null;
    }

    protected void deployProcessDefinition(String classpathResource) {
        repositoryService.createDeployment().addClasspathResource(classpathResource).deploy();
    }

    protected void enableSysoutsInScript() {
        addWhiteListedClass("java.lang.System");
        addWhiteListedClass("java.io.PrintStream");
    }

    protected void addWhiteListedClass(String whiteListedClass) {
        SecureScriptClassShutter secureScriptClassShutter =
                SecureScriptCapableActivityBehaviorFactory.getSecureScriptContextFactory().getClassShutter();
        secureScriptClassShutter.addWhiteListedClass(whiteListedClass);
    }

}
