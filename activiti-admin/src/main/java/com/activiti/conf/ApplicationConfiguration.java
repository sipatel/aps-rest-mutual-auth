/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.conf;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;


@Configuration
@PropertySources({
	@PropertySource(value = "classpath:/META-INF/activiti-admin/activiti-admin.properties"),
	@PropertySource(value = "classpath:activiti-admin.properties", ignoreResourceNotFound = true),
	@PropertySource(value = "file:activiti-admin.properties", ignoreResourceNotFound = true)
})
@ComponentScan(basePackages = {
        "com.activiti.conf",
        "com.activiti.repository",
		"com.activiti.license",
        "com.activiti.service",
        "com.activiti.security"})
public class ApplicationConfiguration {


    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/404.html"));
                container.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html"));
            }

        };
    }

}
