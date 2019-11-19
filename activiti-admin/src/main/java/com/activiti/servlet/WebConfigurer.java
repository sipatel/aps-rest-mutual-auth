/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.servlet;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;


/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
public class WebConfigurer implements ServletContextInitializer {

    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    private final EnumSet<DispatcherType> DISPATCHER_TYPE = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

    @Autowired
    private ApplicationContext context;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        log.debug("Configuring Spring root application context");


        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);


        initSpring(servletContext, context);
        initMetrics(servletContext, context);

        log.debug("Web application fully configured");
    }

    /**
     * Initializes Spring and Spring MVC.
     */
    private void initSpring(ServletContext servletContext, ApplicationContext rootContext) {
        log.debug("Configuring Spring Web application context");
        log.info("Context root is "+servletContext.getContextPath());
        AnnotationConfigWebApplicationContext dispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
        dispatcherServletConfiguration.setParent(rootContext);
        dispatcherServletConfiguration.register(DispatcherServletConfiguration.class);

        log.debug("Registering Spring MVC Servlet");
        ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", new DispatcherServlet(dispatcherServletConfiguration));
        dispatcherServlet.addMapping("/app/*");
        dispatcherServlet.setLoadOnStartup(1);
        dispatcherServlet.setAsyncSupported(true);

        log.debug("Registering API Servlet");
        AnnotationConfigWebApplicationContext apiDispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
        apiDispatcherServletConfiguration.setParent(rootContext);
        apiDispatcherServletConfiguration.register(ApiDispatcherServletConfiguration.class);

        ServletRegistration.Dynamic apiDispatcherServlet = servletContext.addServlet("apiDispatcher",
            new DispatcherServlet(apiDispatcherServletConfiguration));
        apiDispatcherServlet.addMapping("/api/*");
        apiDispatcherServlet.setLoadOnStartup(1);
        apiDispatcherServlet.setAsyncSupported(true);
    }

    /**
     * Initializes Metrics.
     */
    private void initMetrics(ServletContext servletContext, ApplicationContext annotationConfigWebApplicationContext) {
        MetricRegistry metricRegistry = annotationConfigWebApplicationContext.getBean(MetricRegistry.class);
        HealthCheckRegistry healthCheckRegistry = annotationConfigWebApplicationContext.getBean(HealthCheckRegistry.class);

        log.debug("Initializing Metrics registries");
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, metricRegistry);
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
        servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthCheckRegistry);

        log.debug("Registering Metrics Filter");
        FilterRegistration.Dynamic metricsFilter = servletContext.addFilter("webappMetricsFilter", new InstrumentedFilter());

        metricsFilter.addMappingForUrlPatterns(DISPATCHER_TYPE, true, "/*");
        metricsFilter.setAsyncSupported(true);

        log.debug("Registering Metrics Admin Servlet");
        ServletRegistration.Dynamic metricsAdminServlet =servletContext.addServlet("metricsAdminServlet", new AdminServlet());

        metricsAdminServlet.addMapping("/metrics/*");
        metricsAdminServlet.setLoadOnStartup(2);
    }



}
