package com.activiti;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generates test data for the Activiti Admin App.
 * Since the cluster-addon is a depenency, should be auto-clustered.
 * 
 * @author jbarrez
 */
public class GenerateRandomData {
	
	private static final Logger logger = LoggerFactory.getLogger(GenerateRandomData.class);

	protected static Random random = new Random();
	
	protected static ProcessEngine processEngine;
	protected static RuntimeService runtimeService;
	protected static TaskService taskService;
	protected static ManagementService managementService;
	
	protected static String[] processKeys = new String[] {"acquisition", "approval", "accountOpening"};
	
	protected boolean running = true;

	public void start() throws Exception {
		
		// If properties file can be found, use it, otherwise use the default engine
		Properties properties = new Properties();
		boolean propertiesLoaded = false;
		FileInputStream fis = null;
		try {
			String path = "./activiti-test-app.properties";
		    fis = new FileInputStream(path);
		    properties.load(fis);
			propertiesLoaded = true;
		} catch (Exception e) {
			propertiesLoaded = false;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		if (!propertiesLoaded || properties == null || properties.isEmpty()) {
			logger.info("No 'activiti-test-app.properties' found, using default configuration.");
			processEngine = ProcessEngineConfiguration
		        .createStandaloneInMemProcessEngineConfiguration()
		        .setJobExecutorActivate(true)
		        .buildProcessEngine();
		} else {
			processEngine = ProcessEngineConfiguration
			        .createStandaloneInMemProcessEngineConfiguration()
			        .setJdbcUrl(properties.getProperty("jdbc.url"))
			        .setJdbcDriver(properties.getProperty("jdbc.driver"))
			        .setJdbcUsername(properties.getProperty("jdbc.username"))
			        .setJdbcPassword(properties.getProperty("jdbc.password"))
			        .setJobExecutorActivate(true)
			        .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
			        .buildProcessEngine();
		}
		
		RepositoryService repositoryService = processEngine.getRepositoryService();
		
		if (repositoryService.createDeploymentQuery().count() == 0) {
			
			logger.info("Deploying demo processes");
			
			repositoryService.createDeployment().addClasspathResource("acquisition.bpmn20.xml").name("Acquisition Process").deploy();
		
			// 2 versions
			repositoryService.createDeployment().addClasspathResource("accountOpening.bpmn20.xml").name("Open Customer Account").deploy();
			repositoryService.createDeployment().addClasspathResource("accountOpening.bpmn20.xml").name("Open Customer Account").deploy();
			
			// 3 versions for approval (they are all the same)
			repositoryService.createDeployment().addClasspathResource("approval.bpmn20.xml").name("Internal Document Approval").deploy();
			repositoryService.createDeployment().addClasspathResource("approval.bpmn20.xml").name("Internal Document Approval").deploy();
			repositoryService.createDeployment().addClasspathResource("approval.bpmn20.xml").name("Internal Document Approval").deploy();
			
		}
		
		runtimeService = processEngine.getRuntimeService();
		taskService = processEngine.getTaskService();
		managementService = processEngine.getManagementService();
		
		while (running) {
			int randomNr = random.nextInt(3);
			try {
				if (randomNr == 0) {
					startRandomProcessInstance();
				} else if (randomNr == 1) {
					executeSomeTasks();
				} else {
					executeAJob();
				}
			} catch (ActivitiObjectNotFoundException e) {
				// do nothing, probably a collision between test apps
			}
			
			int sleep = random.nextInt(10000);
			Thread.sleep(sleep);
		}
		
	}

	public static void startRandomProcessInstance() {
		logger.info("Starting some random process instances");
		for (int i=0; i<5; i++) {
			int nr = random.nextInt(processKeys.length);
			Map<String, Object> vars = new HashMap<String, Object>();
			if (random.nextBoolean()) {
				vars.put("customerId", random.nextInt(100000));
			}
			if (random.nextBoolean()) {
				vars.put("responsibleManager", "Jack Sparrow");
			}
			if (random.nextBoolean()) {
				vars.put("responsibleManager", "John Connor");
			}
			if (random.nextBoolean()) {
				vars.put("responsibleManager", "Sarah Connor");
			}
			if (random.nextBoolean()) {
				vars.put("monetaryAmount", random.nextInt(100000));
			}
			String businessKey = "" + random.nextInt(100000);
			runtimeService.startProcessInstanceByKey(processKeys[nr], vars);
		}
	}
	
	public static void executeSomeTasks() {
		
		logger.info("Executing some random user tasks");
		
		Task task = null;
		if (taskService.createTaskQuery().count() > 0) {
			task = taskService.createTaskQuery().list().get(0);
		}
		
		for (int i=0; i<random.nextInt(5); i++) {

			if (task != null) {
				taskService.complete(task.getId());
			}
			
			if (taskService.createTaskQuery().count() > 0) {
				task = taskService.createTaskQuery().list().get(0);
			}
		}
	}
	
	public static void executeAJob() {
		logger.info("Executing a random job");
		
		Job job = null;
		if (managementService.createJobQuery().count() > 0) {
			List<Job> jobs = managementService.createJobQuery().list();
			if (jobs.size() > 0) {
				job = jobs.get(0);
			}
		}
		
		if (job!= null) {
			managementService.executeJob(job.getId());
		}
		
	}
	
	public void stop() {
		this.running = false;
	}
			

}
