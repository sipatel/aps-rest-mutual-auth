package com.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class DelegateWithException implements JavaDelegate {
	
	public void execute(DelegateExecution execution) throws Exception {
		throw new RuntimeException();
	}

}
