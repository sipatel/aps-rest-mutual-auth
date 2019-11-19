/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author jbarrez
 */
@Service
public class ConfigurationService {

	@Autowired
	private Environment env;
	
	protected String modelerUrl;
	
	public String getModelerUrl() {
		if (modelerUrl == null) {
			this.modelerUrl = env.getProperty("modeler.url");
		}
		return modelerUrl;
	}

}
