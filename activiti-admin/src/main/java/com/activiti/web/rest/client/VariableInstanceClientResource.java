/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.VariableInstanceService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class VariableInstanceClientResource extends AbstractClientResource {
	
	@Autowired
	protected VariableInstanceService clientService;
	
	
	@RequestMapping(value = "/rest/activiti/variable-instances/{variableId}/data", method = RequestMethod.GET)
	@Timed
	public JsonNode getVariable(@PathVariable String variableId, @RequestParam(required=true) long serverId, HttpServletResponse httpResponse) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		
		try {
			return clientService.getVariableData(serverConfig, variableId, httpResponse);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	

}
