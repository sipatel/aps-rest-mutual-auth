/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.service.ProcessModelerService;
import com.activiti.service.activiti.DeploymentService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.client.AbstractClientResource;
import com.activiti.web.rest.dto.DeployModelerProcessRepresentation;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
@RestController
public class ModelerProcessesResource extends AbstractClientResource {
    
    private final static Logger log = LoggerFactory.getLogger(ModelerProcessesResource.class);

	@Autowired
	protected ProcessModelerService processModelerService;
	
	@Autowired
	protected DeploymentService deploymentService;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * Proxy request to fetch modeler processes. 
	 */
	@Timed
	@RequestMapping(value = "/rest/modeler-processes", method = RequestMethod.GET, produces = "application/json")
	public JsonNode getProcesses(@RequestParam(required=true) String username, @RequestParam(required=true) String password) throws Exception {
		CloseableHttpClient httpClient = processModelerService.createClient(username, password);
		JsonNode processesNode = processModelerService.getProcesses(httpClient);
		return processesNode;
	}

	/**
	 * Proxy request to fetch process model thumbnails
	 */
	@Timed
	@RequestMapping(value = "/rest/modeler-processes/{processModelId}/thumbnail", method = RequestMethod.GET, produces="image/png")
	@ResponseStatus(value = HttpStatus.OK)
	public void getThumbnail(HttpServletResponse response, @PathVariable Long processModelId,
	        @RequestParam(required=true) String username, @RequestParam(required=true) String password) throws Exception {
		
		if (processModelId == null || processModelId <= 0) {
			throw new BadRequestException("Invalid process model id");
		}
		
		CloseableHttpClient httpClient = processModelerService.createClient(username, password);
		try {
    		InputStream thumnailInputStream = processModelerService.getThumbnail(httpClient, processModelId);
    		if (thumnailInputStream != null) {
    		    IOUtils.copy(thumnailInputStream, response.getOutputStream());
    		} else {
                throw new ActivitiServiceException("Error getting thumbnail");
            }
    		
		} finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.warn("Error closing http client instance", e);
            }
        }
	}
	
	/**
	 * Deploy a process model from the modeler 
	 */
	@Timed
	@RequestMapping(value = "/rest/modeler-processes", method = RequestMethod.POST, produces="application/json")
	public JsonNode deployProcess(@RequestBody DeployModelerProcessRepresentation deployModelerProcessRepresentation,
			@RequestParam(required=true) long serverId) throws Exception {
	    
		if (deployModelerProcessRepresentation == null) {
			throw new BadRequestException("Invalid json");
		}
		if (deployModelerProcessRepresentation.getUsername() == null) {
			throw new BadRequestException("Invalid json : username is null");
		}
		if (deployModelerProcessRepresentation.getPassword() == null) {
			throw new BadRequestException("Invalid json : password is null");
		}
		if (deployModelerProcessRepresentation.getProcessModelId() == null) {
			throw new BadRequestException("Invalid json : processModelId is null");
		}
		
		CloseableHttpClient httpClient = processModelerService.createClient(deployModelerProcessRepresentation.getUsername(), deployModelerProcessRepresentation.getPassword());
		try {
    		InputStream inputStream = processModelerService.getBpmn20Xml(httpClient, deployModelerProcessRepresentation.getProcessModelId());
    		if (inputStream != null) {
    		    return deploymentService.uploadDeployment(retrieveServerConfig(serverId), deployModelerProcessRepresentation.getName() + ".bpmn", inputStream);
    		} else {
    		    throw new ActivitiServiceException("Error uploading deployment");
    		}
    		
		} finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.warn("Error closing http client instance", e);
            }
        }
	}
}
