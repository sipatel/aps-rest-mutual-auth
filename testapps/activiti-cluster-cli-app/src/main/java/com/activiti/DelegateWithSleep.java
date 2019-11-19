package com.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class DelegateWithSleep implements JavaDelegate {
	
	public void execute(DelegateExecution execution) throws Exception {
		// Deliberatly wait a long time, so the queue gets flooded
		Thread.sleep(5000L);
	}

}
