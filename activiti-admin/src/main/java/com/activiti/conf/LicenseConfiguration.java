/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.activiti.service.ActivitiEndpointLicenseService;

@Configuration
public class LicenseConfiguration {

    private final Logger log = LoggerFactory.getLogger(LicenseConfiguration.class);

	@Autowired
	private ActivitiEndpointLicenseService endpointLicenseService;
	
	@Scheduled(fixedDelay = 360000)
	public void checkEndpointLicenses() {
		try {
			endpointLicenseService.checkServerConfigLicenses();
			
		} catch (Throwable t) {
			// Any exception is considered as an invalid license
			log.error("Unexpected error while checking Activiti endpoint licenses: " + t.getMessage(), t);
		}
		
	}
}

