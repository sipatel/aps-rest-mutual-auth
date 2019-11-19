/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.TaskService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class TaskClientResource extends AbstractClientResource {
	
	@Autowired
	protected TaskService clientService;

	/**
	 * GET /rest/authenticate -> check if the user is authenticated, and return
	 * its login.
	 */
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public JsonNode getTask(@PathVariable String taskId, @RequestParam(required=true) long serverId,
			@RequestParam(required=false, defaultValue="false") boolean runtime) throws BadRequestException {
		
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getTask(serverConfig, taskId, runtime);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}", method = RequestMethod.DELETE)
	@Timed
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteTask(@PathVariable String taskId, @RequestParam(required=true) long serverId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			clientService.deleteTask(serverConfig, taskId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}", method = RequestMethod.POST)
	@Timed
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void executeTaskAction(@PathVariable String taskId, @RequestBody ObjectNode actionBody) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(actionBody);
		try {
			clientService.executeTaskAction(serverConfig, taskId, actionBody);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}", method = RequestMethod.PUT)
	@Timed
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void updateTask(@PathVariable String taskId, @RequestBody ObjectNode actionBody) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(actionBody);
		try {
			clientService.updateTask(serverConfig, taskId, actionBody);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}/subtasks", method = RequestMethod.GET)
	@Timed
	public JsonNode getSubtasks(@PathVariable String taskId, @RequestParam(required=true) long serverId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getSubTasks(serverConfig, taskId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}/variables", method = RequestMethod.GET)
	@Timed
	public JsonNode getVariables(@PathVariable String taskId, @RequestParam(required=true) long serverId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getVariables(serverConfig, taskId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}/identitylinks", method = RequestMethod.GET)
	@Timed
	public JsonNode getIdentityLinks(@PathVariable String taskId, @RequestParam(required=true) long serverId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getIdentityLinks(serverConfig, taskId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
}
