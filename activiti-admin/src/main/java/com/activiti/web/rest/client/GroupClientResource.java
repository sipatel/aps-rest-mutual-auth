/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.ActivitiIdentityService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
@RestController
public class GroupClientResource extends AbstractClientResource {
	
	@Autowired
	protected ActivitiIdentityService clientService;

	@RequestMapping(value = "/rest/activiti/groups/{groupId}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public JsonNode getGroup(@PathVariable String groupId, HttpServletRequest request) throws BadRequestException {
		
		ServerConfig serverConfig = retrieveServerConfig(request);
		try {
			return clientService.getGroup(serverConfig, groupId, getRequestParametersWithoutServerId(request));
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
}
