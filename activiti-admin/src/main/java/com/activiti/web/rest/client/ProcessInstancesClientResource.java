/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.ProcessInstanceService;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class ProcessInstancesClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(ProcessInstancesClientResource.class);

    @Autowired
    protected ProcessInstanceService clientService;
    
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET  /rest/authenticate -> check if the user is authenticated, and return its login.
     */
    @RequestMapping(value = "/rest/activiti/process-instances",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json")
    @Timed
    public JsonNode listProcessInstances(@RequestBody ObjectNode bodyNode) {
        log.debug("REST request to get a list of process instances");
        
        JsonNode resultNode = null;
        try {
            ServerConfig serverConfig = retrieveServerConfig(bodyNode);
	        resultNode = clientService.listProcesInstances(bodyNode, serverConfig);
			
		} catch (Exception e) {
			log.error("Error processing process instance list request", e);
			throw new BadRequestException(e.getMessage());
		}
        
        return resultNode;
    }
}
