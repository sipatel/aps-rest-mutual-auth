/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.DeploymentService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.InternalServerErrorException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author jbarrez
 */
@RestController
@RequestMapping("/rest/activiti/deployments")
public class DeploymentsClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(DeploymentsClientResource.class);

    @Autowired
    protected DeploymentService clientService;

    /**
     * GET  /rest/activiti/deployments -> get a list of deployments.
     */
    @Timed
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public JsonNode listDeployments(HttpServletRequest request) {
        log.debug("REST request to get a list of deployments");
        
        JsonNode resultNode = null;
        ServerConfig serverConfig = retrieveServerConfig(request);
    	Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);
    	
    	try {
    		resultNode = clientService.listDeployments(serverConfig, parameterMap);
	        
        } catch (ActivitiServiceException e) {
        	throw new BadRequestException(e.getMessage());
        }
    	
        return resultNode;
    }

    /**
     * POST /rest/activiti/deployments: upload a deployment
     */
    @Timed
    @RequestMapping(method=RequestMethod.POST, produces = "application/json")
    public JsonNode handleFileUpload(HttpServletRequest request, @RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
        	try {
        		ServerConfig serverConfig = retrieveServerConfig(request);
        		String fileName = file.getOriginalFilename();
        		if (fileName != null &&
        				(fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml")
        						|| fileName.endsWith(".zip") || fileName.endsWith(".bar"))) {
        			return clientService.uploadDeployment(serverConfig, fileName, file.getInputStream());
        		} else {
        			throw new BadRequestException("Invalid file name");
        		}
            } catch (IOException e) {
            	throw new InternalServerErrorException("Could not deploy file: " + e.getMessage());
            }
        } else {
            throw new BadRequestException("No file found in POST body");
        }
    }
    
}
