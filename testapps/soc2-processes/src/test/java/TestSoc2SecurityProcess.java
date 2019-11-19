import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.mock.MockServiceTask;
import org.activiti.engine.test.mock.MockServiceTasks;
import org.activiti.engine.test.mock.NoOpServiceTasks;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class TestSoc2SecurityProcess {
	
	private static Clock clock;
	private static RepositoryService repositoryService;
	private static RuntimeService runtimeService;
	private static TaskService taskService;
	private static HistoryService historyService;
	private static ManagementService managementService;
	
	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();
	
	private Calendar testStartDate;
	
	@Before
	public void before() {
		clock = activitiRule.getProcessEngine().getProcessEngineConfiguration().getClock();
		repositoryService = activitiRule.getProcessEngine().getRepositoryService();
		runtimeService = activitiRule.getProcessEngine().getRuntimeService();
		taskService = activitiRule.getProcessEngine().getTaskService();
		historyService = activitiRule.getProcessEngine().getHistoryService();
		managementService = activitiRule.getProcessEngine().getManagementService();
		
		Map<Object,Object> mockBeans = new HashMap<Object, Object>();
		mockBeans.put("auditLogBean", new MockBean());
		((ProcessEngineConfigurationImpl) activitiRule.getProcessEngine().getProcessEngineConfiguration()).getExpressionManager().setBeans(mockBeans);
		
		Calendar date = Calendar.getInstance();
	    date.set(Calendar.DAY_OF_YEAR, 1);
	    date.set(Calendar.MONTH, Calendar.JANUARY);
	    date.set(Calendar.YEAR, 2015);
	    date.set(Calendar.HOUR_OF_DAY, 1);
	    date.set(Calendar.MINUTE, 0);
	    date.set(Calendar.SECOND, 0);
	    date.set(Calendar.MILLISECOND, 0);
	    testStartDate = date;
	    activitiRule.getProcessEngine().getProcessEngineConfiguration().getClock().setCurrentCalendar(date);
		repositoryService.createDeployment().addClasspathResource("soc2_security.bpmn20.xml").deploy();
	}
	
	@After
	public void cleanup() {
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}
	
	@Test
	public void testCronExpression() {
		
		// After deploy, 1 job is expected planned for 9:00 Thursday 1 january
		Job job = managementService.createJobQuery().singleResult();
		assertEquals(job.getDuedate(), createDate(testStartDate, 8, 0, 0));

		
		// Move to 9:01. Job should be executable. Executing it, should trigger new one
		clock.setCurrentTime(createDate(testStartDate, 8, 1, 0));
		job = managementService.createJobQuery().executable().singleResult();
		managementService.executeJob(job.getId());
		
		// Process is now started. This will also create job (timer on first task), so killing it immediately
		assertEquals(1L, runtimeService.createProcessInstanceQuery().count());
		runtimeService.deleteProcessInstance(runtimeService.createProcessInstanceQuery().singleResult().getId(), null);
		
		job = managementService.createJobQuery().executable().singleResult();
		assertNull(job);
		
		job = managementService.createJobQuery().singleResult();
		assertEquals(job.getDuedate(), createDate(testStartDate, 32, 0, 0));
		
		// Move to the next day. But then it's weekend! So needs to be saturday
		clock.setCurrentTime(createDate(testStartDate, 32, 5, 0));
		job = managementService.createJobQuery().executable().singleResult();
		managementService.executeJob(job.getId());
		
		runtimeService.deleteProcessInstance(runtimeService.createProcessInstanceQuery().singleResult().getId(), null);
		job = managementService.createJobQuery().singleResult();
		assertEquals(job.getDuedate(), createDate(testStartDate, 104, 0, 0));
		
	}
	
	@Test
	public void testNoWorkDoneAtAll() {
		
		// Start process
		clock.setCurrentTime(createDate(testStartDate, 8, 1, 0));
		Job job = managementService.createJobQuery().executable().singleResult();
		managementService.executeJob(job.getId());
		
		// Move the clock 3 hours
		clock.setCurrentTime(createDate(testStartDate, 11, 1, 0));
		assertEquals(1, runtimeService.createProcessInstanceQuery().count());
		
		// Move clock another hour
		clock.setCurrentTime(createDate(testStartDate, 12, 1, 0));
		assertEquals(1, runtimeService.createProcessInstanceQuery().count());
		
		// Move to 17:01 (process should be cancelled)
		clock.setCurrentTime(createDate(testStartDate, 16, 2, 0));
		managementService.executeJob(managementService.createJobQuery().orderByJobDuedate().desc().list().get(1).getId()); // index 0 -> job for next timer start, index 1 -> 17:00 timer
		assertEquals(0, runtimeService.createProcessInstanceQuery().count());
	}
	
	@Test
	public void testScriptCheckForYesterday() {
		
		// Start and abort process
		clock.setCurrentTime(createDate(testStartDate, 8, 1, 0));
		Job job = managementService.createJobQuery().executable().singleResult();
		managementService.executeJob(job.getId());
		clock.setCurrentTime(createDate(testStartDate, 16, 1, 0));
		managementService.executeJob(managementService.createJobQuery().orderByJobDuedate().desc().list().get(1).getId()); // index 0 -> job for next timer start, index 1 -> 17:00 timer
		assertEquals(0, runtimeService.createProcessInstanceQuery().count());
		
		// Start second process
		clock.setCurrentTime(createDate(testStartDate, 32, 1, 0));
		job = managementService.createJobQuery().executable().singleResult();
		managementService.executeJob(job.getId());
		
		// Going through the process until the script
		Task task = taskService.createTaskQuery().singleResult();
		assertEquals("Daily Security Review", task.getName());
		taskService.complete(task.getId());
		
		task = taskService.createTaskQuery().singleResult();
		assertEquals("Reminder to cover previous day", task.getName());
	}
	
	@Test
	@NoOpServiceTasks(ids = {"uploadLogs"})
	public void testRegularPassThrough() {
		
		// Start process
		clock.setCurrentTime(createDate(testStartDate, 8, 1, 0));
		Job job = managementService.createJobQuery().executable().singleResult();
		managementService.executeJob(job.getId());
		assertEquals(1, runtimeService.createProcessInstanceQuery().count());
		
		// Going through the process 
		Task task = taskService.createTaskQuery().singleResult();
		assertEquals("Daily Security Review", task.getName());
		taskService.complete(task.getId());
		
		task = taskService.createTaskQuery().singleResult();
		assertEquals("Reminder to cover previous day", task.getName());
		taskService.complete(task.getId());
		
		task = taskService.createTaskQuery().singleResult();
		assertEquals("IDS Real time alerts?", task.getName());
		taskService.complete(task.getId());
		
		task = taskService.createTaskQuery().singleResult();
		assertEquals("McAffee Real Time Scan Alerst?", task.getName());
		taskService.complete(task.getId());
		
		task = taskService.createTaskQuery().singleResult();
		assertEquals("SYS LOG daily alerts actions needed?", task.getName());
		taskService.complete(task.getId());
		
		Job alfrescoPublishJob = managementService.createJobQuery().messages().singleResult();
		managementService.executeJob(alfrescoPublishJob.getId());
		
		assertEquals(0, runtimeService.createProcessInstanceQuery().count());
		
	}
	
	private Date createDate(Calendar baseDate, int hours, int minutes, int seconds) {
		Calendar result = Calendar.getInstance();
		result.setTime(baseDate.getTime());
		result.add(Calendar.HOUR, hours);
		result.add(Calendar.MINUTE, minutes);
		result.add(Calendar.SECOND, seconds);
		return result.getTime();
	}
	

}
