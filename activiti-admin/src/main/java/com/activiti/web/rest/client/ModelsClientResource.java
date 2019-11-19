/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.ModelService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
@RestController
@RequestMapping("/rest/activiti/models")
public class ModelsClientResource extends AbstractClientResource {


    @Autowired
    protected ModelService clientService;

    /**
     * GET  /rest/activiti/models -> get a list of apps.
     */
    @Timed
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public JsonNode listModels(HttpServletRequest request) {
        ServerConfig serverConfig = retrieveServerConfig(request);
    	Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);
    	
    	try {
    		return clientService.listModels(serverConfig, parameterMap);
	        
        } catch (ActivitiServiceException e) {
        	throw new BadRequestException(e.getMessage());
        }
    }
}
