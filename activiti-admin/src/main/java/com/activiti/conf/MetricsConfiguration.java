/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.conf;


import java.lang.management.ManagementFactory;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.activiti.conf.metrics.DatabaseHealthCheck;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

@Configuration
@EnableMetrics(proxyTargetClass = true)
public class MetricsConfiguration extends MetricsConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(MetricsConfiguration.class);

    @Autowired
    private Environment env;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MetricRegistry metricRegistry;
    @Autowired
    private HealthCheckRegistry healthCheckRegistry;

    @Override
    public MetricRegistry getMetricRegistry() {
        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();

            log.debug("Registring JVM gauges");
            metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
            metricRegistry.register("jvm.garbage", new GarbageCollectorMetricSet());
            metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());
            metricRegistry.register("jvm.files", new FileDescriptorRatioGauge());
            metricRegistry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

        }

        return metricRegistry;

    }

    @Override
    public HealthCheckRegistry getHealthCheckRegistry() {
        if (healthCheckRegistry == null) {
            healthCheckRegistry = new HealthCheckRegistry();
            log.debug("Initializing Metrics healthchecks");
            healthCheckRegistry.register("database", new DatabaseHealthCheck(dataSource));
        }
        return healthCheckRegistry;
    }


    @Override
    public void configureReporters(MetricRegistry metricRegistry) {
        log.info("Initializing Metrics JMX metrics reporting");
        final JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
        jmxReporter.start();
    }
}
