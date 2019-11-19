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
import com.activiti.service.activiti.DeploymentService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class DeploymentClientResource extends AbstractClientResource {

	@Autowired
	protected DeploymentService clientService;

	/**
	 * GET /rest/authenticate -> check if the user is authenticated, and return its login.
	 */
	@RequestMapping(value = "/rest/activiti/deployments/{deploymentId}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public JsonNode getDeployment(@PathVariable String deploymentId, 
			@RequestParam(required=true) long serverId) throws BadRequestException {
		
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getDeployment(serverConfig, deploymentId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/deployments/{deploymentId}", method = RequestMethod.DELETE)
	@Timed
	public void deleteDeployment(@PathVariable String deploymentId, HttpServletResponse httpResponse, 
	        @RequestParam(required=true, defaultValue="false") boolean cascade, @RequestParam(required=true) long serverId) {
	    clientService.deleteDeployment(retrieveServerConfig(serverId), httpResponse, deploymentId, cascade);
	}
}
