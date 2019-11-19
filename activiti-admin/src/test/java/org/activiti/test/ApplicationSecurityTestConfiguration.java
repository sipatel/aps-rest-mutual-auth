/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.test;

import com.activiti.conf.AsyncConfiguration;
import com.activiti.conf.DatabaseConfiguration;
import com.activiti.conf.JacksonConfiguration;
import com.activiti.conf.LicenseConfiguration;
import com.activiti.conf.SecurityConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources({
    @PropertySource(value = "classpath:/META-INF/activiti-admin/TEST-db.properties"),
	@PropertySource(value = "classpath:/META-INF/activiti-admin/TEST-activiti-admin.properties"),
    @PropertySource(value = "classpath:/META-INF/activiti-admin/TEST-activiti-app-security.properties", ignoreResourceNotFound = false)
})
@ComponentScan(basePackages = {
    "com.activiti.repository",
    "com.activiti.service",
    "org.activiti.test.security",
    "com.activiti.security",
    "com.activiti.license"})
@Import(value = {
        SecurityConfiguration.class,
        AsyncConfiguration.class,
        DatabaseConfiguration.class,
        LicenseConfiguration.class,
        JacksonConfiguration.class})
public class ApplicationSecurityTestConfiguration {

    /**
     * This is needed to make property resolving work on annotations ...
     * (see http://stackoverflow.com/questions/11925952/custom-spring-property-source-does-not-resolve-placeholders-in-value)
     *
     * @Scheduled(cron="${someProperty}")
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
