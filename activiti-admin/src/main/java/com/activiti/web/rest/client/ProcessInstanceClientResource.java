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
import com.activiti.service.activiti.ProcessInstanceService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class ProcessInstanceClientResource extends AbstractClientResource {

	@Autowired
	protected ProcessInstanceService clientService;

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public JsonNode getTask(@PathVariable String processInstanceId, @RequestParam(required=true) long serverId,
			@RequestParam(required=false, defaultValue="false") boolean runtime) throws BadRequestException {

		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getProcessInstance(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/tasks", method = RequestMethod.GET)
	@Timed
	public JsonNode getSubtasks(@PathVariable String processInstanceId, @RequestParam(required=true) long serverId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getTasks(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/variables", method = RequestMethod.GET)
	@Timed
	public JsonNode getVariables(@PathVariable String processInstanceId, @RequestParam(required=true) long serverId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getVariables(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

    @RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/variables/{variableName}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @Timed
    public void updateVariable(@PathVariable String processInstanceId, @PathVariable String variableName, @RequestBody ObjectNode body) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig(body);
        try {
            clientService.updateVariable(serverConfig, processInstanceId, variableName, body);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/variables", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @Timed
    public void createVariable(@PathVariable String processInstanceId, @RequestBody ObjectNode body) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig(body);
        try {
            clientService.createVariable(serverConfig, processInstanceId, body);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }


    @RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/variables/{variableName}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @Timed
    public void deleteVariable(@PathVariable String processInstanceId, @PathVariable String variableName, @RequestParam(required=true) long serverId) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            clientService.deleteVariable(serverConfig, processInstanceId, variableName);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/subprocesses", method = RequestMethod.GET)
	@Timed
	public JsonNode getSubProcesses(@PathVariable String processInstanceId, @RequestParam(required=true) long serverId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getSubProcesses(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/jobs", method = RequestMethod.GET)
	@Timed
	public JsonNode getJobs(@PathVariable String processInstanceId, @RequestParam(required=true) long serverId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getJobs(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	@Timed
	public void executeAction(@PathVariable String processInstanceId, @RequestParam(required=true) long serverId, @RequestBody JsonNode actionBody) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			clientService.executeAction(serverConfig, processInstanceId, actionBody);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
}
