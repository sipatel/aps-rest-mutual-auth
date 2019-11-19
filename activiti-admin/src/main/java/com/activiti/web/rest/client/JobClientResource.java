/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.JobService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class JobClientResource extends AbstractClientResource {

	@Autowired
	protected JobService clientService;

	/**
	 * GET /rest/activiti/jobs/{jobId} -> return job data
	 */
	@RequestMapping(value = "/rest/activiti/jobs/{jobId}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public JsonNode getJob(@PathVariable String jobId,
			@RequestParam(required=true) long serverId) throws BadRequestException {

		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getJob(serverConfig, jobId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	/**
	 * DELETE /rest/activiti/jobs/{jobId} -> delete job
	 */
	@RequestMapping(value = "/rest/activiti/jobs/{jobId}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	@Timed
	public void deleteJob(@PathVariable String jobId,
			@RequestParam(required=true) long serverId) throws BadRequestException {

		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			clientService.deleteJob(serverConfig, jobId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	/**
	 * POST /rest/activiti/jobs/{jobId} -> execute job
	 */
	@RequestMapping(value = "/rest/activiti/jobs/{jobId}", method = RequestMethod.POST, produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	@Timed
	public void executeJob(@PathVariable String jobId,
			@RequestParam(required=true) long serverId) throws BadRequestException {

		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			 clientService.executeJob(serverConfig, jobId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	/**
	 * GET /rest/activiti/jobs/{jobId}/exception-stracktrace -> return job stacktrace
	 */
	@RequestMapping(value = "/rest/activiti/jobs/{jobId}/stacktrace", method = RequestMethod.GET, produces = "text/plain")
	@Timed
	public String getJobStacktrace(@PathVariable String jobId,
			@RequestParam(required=true) long serverId) throws BadRequestException {

		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			String trace =  clientService.getJobStacktrace(serverConfig, jobId);
			if(trace != null) {
				trace = StringUtils.trim(trace);
			}
			return trace;
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
}
