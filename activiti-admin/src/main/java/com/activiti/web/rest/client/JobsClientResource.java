/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.JobService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class JobsClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(JobsClientResource.class);
    
    @Autowired
    protected JobService jobService;

    /**
     * GET  /rest/activiti/jobs -> Get a list of jobs.
     */
    @RequestMapping(value = "/rest/activiti/jobs",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public JsonNode listJobs(HttpServletRequest request) {
        log.debug("REST request to get a list of jobs");
        ServerConfig serverConfig = retrieveServerConfig(request);
        Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);
        
        try {
	        return jobService.listJobs(serverConfig, parameterMap);
        } catch (ActivitiServiceException e) {
        	throw new BadRequestException(e.getMessage());
        }
    }
}
