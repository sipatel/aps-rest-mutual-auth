/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import com.activiti.service.activiti.ProcessEngineInfoService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Frederik Heremans
 */
@RestController
public class ProcessEngineInfoClientResource extends AbstractClientResource {

	@Autowired
	protected ProcessEngineInfoService clientService;
	
	@Autowired
    protected Environment env;

	@RequestMapping(value = "/rest/activiti/engine-info", method = RequestMethod.GET)
	@Timed
	public JsonNode getEngineInfo(@RequestParam(value="serverId", required=true) Long serverId) throws BadRequestException {
		try {
			return clientService.getEngineInfo(retrieveServerConfig(serverId));
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	protected boolean isMultiTenantEnabled() {
        return env.getProperty("multi-tenant.enabled", Boolean.class, false);
    }

}
