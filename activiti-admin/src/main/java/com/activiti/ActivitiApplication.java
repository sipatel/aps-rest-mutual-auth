/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti;

import com.activiti.servlet.WebConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;


@SpringBootApplication
@ServletComponentScan
@ComponentScan(basePackages = {
        "com.activiti.conf",
        "com.activiti.repository",
        "com.activiti.service",
        "com.activiti.security",
        "com.activiti.license"}) // For all external beans (eg ReportGenerators)
public class ActivitiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(new Class[]{ActivitiApplication.class}, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ActivitiApplication.class);
    }

    @Bean
    public WebConfigurer executorListener() {
        return new WebConfigurer();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
