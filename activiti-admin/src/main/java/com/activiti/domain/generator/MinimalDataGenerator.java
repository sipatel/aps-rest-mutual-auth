/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.domain.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

import com.activiti.service.activiti.ClusterConfigService;

/**
 * Generates the minimal data needed when the application is booted with no data at all.
 *
 * @author jbarrez
 */
public class MinimalDataGenerator implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger log = LoggerFactory.getLogger(MinimalDataGenerator.class);

	@Autowired
	protected ClusterConfigService clusterConfigService;

	@Autowired
	protected Environment environment;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) { // Using Spring MVC, there are multiple child contexts. We only care about the root
            log.info("Verifying if minimal data is present");

            if (clusterConfigService.findAll().size() == 0) {
                log.info("No cluster configuration found, creating default cluster configuration");
                clusterConfigService.createNewClusterConfig("development", "dev", "dev", false);
            }
        }
    }

}
