package com.activiti;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class DemoContextListener implements ServletContextListener {
	
	private ExecutorService executorService;
	
	private GenerateRandomData generateRandomData;

	public void contextInitialized(ServletContextEvent sce) {
		generateRandomData = new GenerateRandomData();
		
		executorService = Executors.newFixedThreadPool(1);
		executorService.execute(new Runnable() {
			
			public void run() {
				try {
	        generateRandomData.start();
        } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
			}
		});
  }

	public void contextDestroyed(ServletContextEvent sce) {
		generateRandomData.stop();
		executorService.shutdown();
		try {
	    executorService.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
  }
	
	

}
