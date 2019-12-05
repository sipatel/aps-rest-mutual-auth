package com.activiti.extension.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.activiti.extension.bean.RestCallBeanExtension;
import com.activiti.runtime.activiti.bean.RestCallBean;


@Configuration
public class CustomConfiguration {


	@Autowired
	ApplicationContext context;
	
	@Autowired
	ConfigurableApplicationContext configurableApplicationContext;
	
	
	@Bean
	@DependsOn({"activiti_restCallDelegate","custom_activiti_restCallDelegate"})
	public RestCallBean getRestCallBean()
	{
		
		BeanDefinitionRegistry factory = (BeanDefinitionRegistry) context.getAutowireCapableBeanFactory();
		
		factory.removeBeanDefinition("activiti_restCallDelegate");
		configurableApplicationContext.getBeanFactory().registerSingleton("activiti_restCallDelegate", context.getBean("custom_activiti_restCallDelegate", RestCallBeanExtension.class));
		
		return null;
		
	}
}
