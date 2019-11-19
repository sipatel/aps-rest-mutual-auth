/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.TaskService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class TasksClientResource extends AbstractClientResource {

	@Autowired
	protected TaskService clientService;

	/**
	 * GET /rest/authenticate -> check if the user is authenticated, and return
	 * its login.
	 */
	@RequestMapping(value = "/rest/activiti/tasks", method = RequestMethod.POST, produces = "application/json")
	@Timed
	public JsonNode listTasks(@RequestBody ObjectNode requestNode) {
		ServerConfig serverConfig = retrieveServerConfig(requestNode);
		JsonNode resultNode;
		try {
			resultNode = clientService.listTasks(serverConfig, requestNode);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}

		if(resultNode == null) {
			throw new BadRequestException("Empty result returned from activiti");
		}
		return resultNode;
	}
}
