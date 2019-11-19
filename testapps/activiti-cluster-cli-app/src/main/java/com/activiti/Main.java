package com.activiti;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Clock;
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
public class Main {
	
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	protected static Random random = new Random();
	
	protected static ProcessEngine processEngine;
	protected static RuntimeService runtimeService;
	protected static TaskService taskService;
	protected static ManagementService managementService;
	
	protected static Clock clock;
	protected static Date maxHistoricalDate;
	
	protected static String[] processKeys = new String[] {"acquisition", "approval", "accountOpening"};

	public static void main(String[] args) throws Exception {
		
		if (args.length > 0 && args[0].equals("help")) {
			System.out.println("To configure the Cluster Test App, add a files named 'activiti-test-app.properties' next to the jar.");
			System.out.println();
			System.out.println("Following properties can be set:");
			System.out.println();
			System.out.println("jdbc.url: The url to the database");
			System.out.println("jdbc.driver: The url to the database");
			System.out.println("jdbc.username: The url to the database");
			System.out.println("jdbc.password: The url to the database");
			System.out.println();
			System.out.println("max.wait.time: The maximum wait time, in milliseconds, to wait between data generation operations (defaults to 10 sec)");
			System.out.println("max.historical.date: The date (formatted as 'yyyy-mm-dd') which is the maximum historical time to generate data for (default to current time)");
			System.out.println();
			System.out.println("enable.clustering: Manually override for all automatic cluster config detections.");
			System.out.println();
			return;
		}
		
		// If properties file can be found, use it, otherwise use the default engine
		Properties properties = new Properties();
		boolean propertiesLoaded = false;
		FileInputStream fis = null;
		try {
				String path = "./activiti-test-app.properties";
		    fis = new FileInputStream(path);
		    properties.load(fis);
		    propertiesLoaded = true;
		    logger.info("Property file 'activiti-test-app.properties' found. using values from this file.");
		} catch (Exception e) {
			propertiesLoaded = false;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		
		String enableClusteringString = properties.getProperty("enable.clustering");
		boolean enableClustering = true; 
		if (enableClusteringString != null) {
			System.out.println("Enable clustering = " + enableClusteringString);
			enableClustering = Boolean.valueOf(enableClusteringString);
		}
		
		ProcessEngineConfiguration processEngineConfiguration = null;
		if (!propertiesLoaded || properties == null || properties.isEmpty()) {
			logger.info("No 'activiti-test-app.properties' found, using default configuration.");
			 processEngineConfiguration = ProcessEngineConfiguration
		        .createStandaloneInMemProcessEngineConfiguration()
		        .setJobExecutorActivate(false);
		} else {
			System.out.println("jdbc.url = " + properties.getProperty("jdbc.url"));
			System.out.println("jdbc.driver = " + properties.getProperty("jdbc.driver"));
			System.out.println("jdbc.user = " + properties.getProperty("jdbc.username"));
			System.out.println("jdbc.password = " + properties.getProperty("jdbc.password"));
			processEngineConfiguration = ProcessEngineConfiguration
			        .createStandaloneInMemProcessEngineConfiguration()
			        .setJdbcUrl(properties.getProperty("jdbc.url"))
			        .setJdbcDriver(properties.getProperty("jdbc.driver"))
			        .setJdbcUsername(properties.getProperty("jdbc.username"))
			        .setJdbcPassword(properties.getProperty("jdbc.password"))
			        .setJobExecutorActivate(false)
			        .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		}
		
		if (enableClustering) {
			processEngineConfiguration.enableClusterConfig();
		}
		processEngine = processEngineConfiguration.buildProcessEngine();
		
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
		clock = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getClock();
		
		int maxWaitTime = 10000;
		String maxWaitTimeString = properties.getProperty("max.wait.time");
		if (maxWaitTimeString != null) {
			maxWaitTime = Integer.valueOf(maxWaitTimeString);
		}
		
		String maxHistoricalDateString = properties.getProperty("max.historical.date");
		maxHistoricalDate = null;
		if (maxHistoricalDateString != null) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd");
			maxHistoricalDate = simpleDateFormat.parse(maxHistoricalDateString);
		}
		
		
		
		String threadedModeString = properties.getProperty("threaded.mode.enabled");
		boolean threadedModeEnabled = false; 
		if (threadedModeString != null) {
			System.out.println("Enable threading = " + threadedModeString);
			threadedModeEnabled = Boolean.valueOf(threadedModeString);
		}
		
		if (!threadedModeEnabled) {
		
			while (true) {
				
				
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
					// do nothing, probably a collision between different test apps
				}
				
				int sleep = random.nextInt(maxWaitTime);
				Thread.sleep(sleep);
				
			}
			
		} else {
			
			Thread processInstanceThread = new Thread(new Runnable() {
				public void run() {
					while(true) {
						startRandomProcessInstance();
					}
				}
			});
			
			Thread taskThread = new Thread(new Runnable() {
				public void run() {
					while(true) {
						executeSomeTasks();
					}
				}
			});
			
			Thread jobThread = new Thread(new Runnable() {
				public void run() {
					while(true) {
						executeAJob();
						try {
	            Thread.sleep(5000L);
            } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            } // Jobs are not that important
					}
				}
			});
			
			processInstanceThread.start();
			taskThread.start();
			jobThread.start();
			
		}
		
	}

	public static void startRandomProcessInstance() {
		
		if (maxHistoricalDate != null) {
			clock.setCurrentTime(new Date(maxHistoricalDate.getTime() + nextLong((System.currentTimeMillis() - maxHistoricalDate.getTime()))));
		}
		
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
			vars.put("outcome", random.nextBoolean() ? "A" : "B");
			String businessKey = "" + random.nextInt(100000);
			runtimeService.startProcessInstanceByKey(processKeys[nr], businessKey, vars);
		}
		
		clock.setCurrentTime(null);
	}
	
	public static void executeSomeTasks() {
		
		logger.info("Executing some random user tasks");
		
		Task task = null;
		if (taskService.createTaskQuery().count() > 0) {
			task = taskService.createTaskQuery().list().get(0);
		}
		
		for (int i=0; i<random.nextInt(5); i++) {

			if (task != null) {
				
				if (maxHistoricalDate != null) {
					Date createTime = task.getCreateTime();
					clock.setCurrentTime(new Date(createTime.getTime() + nextLong(1296000000))); // random: 14 days
				}
				
				taskService.complete(task.getId(), CollectionUtil.singletonMap("outcome", random.nextBoolean() ? "A" : "B"));
				
				if (maxHistoricalDate != null) {
					clock.setCurrentTime(null);
				}
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
			if (maxHistoricalDate != null) {
				Date createTime = job.getDuedate();
				clock.setCurrentTime(new Date(createTime.getTime() + nextLong(2592000000L))); // random: 30 days
			}
			
			managementService.executeJob(job.getId());
			
			if (maxHistoricalDate != null) {
				clock.setCurrentTime(null);
			}
		}
		
	}
	
	/* HELPER METHODS */
	
	// From http://stackoverflow.com/questions/2546078/java-random-long-number-in-0-x-n-range
	private static long nextLong(long n) {
	   long bits, val;
	   do {
	      bits = (random.nextLong() << 1) >>> 1;
	      val = bits % n;
	   } while (bits-val+(n-1) < 0L);
	   return val;
	}

}
