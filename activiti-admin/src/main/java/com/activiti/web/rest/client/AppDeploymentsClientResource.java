/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.AppService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.InternalServerErrorException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * @author Yvo Swillens
 */
@RestController
@RequestMapping("/rest/activiti/apps")
public class AppDeploymentsClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(AppDeploymentsClientResource.class);

    @Autowired
    protected AppService clientService;

    /**
     * GET  /rest/activiti/apps -> get a list of apps.
     */
    @Timed
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public JsonNode listDeployments(HttpServletRequest request) {
        log.debug("REST request to get a list of apps");
        
        JsonNode resultNode = null;
        ServerConfig serverConfig = retrieveServerConfig(request);
    	Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);
    	
    	try {
    		resultNode = clientService.listAppDefinitions(serverConfig, parameterMap);
	        
        } catch (ActivitiServiceException e) {
        	throw new BadRequestException(e.getMessage());
        }
    	
        return resultNode;
    }
    
    /**
     * POST /rest/activiti/apps: upload a app
     */
    @Timed
    @RequestMapping(method=RequestMethod.POST)
    public void handleFileUpload(HttpServletRequest request, HttpServletResponse httpResponse, @RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
        	try {
        		ServerConfig serverConfig = retrieveServerConfig(request);
        		String fileName = file.getOriginalFilename();
        		if (fileName != null && fileName.endsWith(".zip")) {
        			clientService.uploadAppDefinition(httpResponse, serverConfig, fileName, file.getInputStream());
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
    
    /**
     * POST /rest/activiti/apps/publish: publish an app
     * @throws IOException 
     */
    @Timed
    @RequestMapping(value="{modelId}/publish",method=RequestMethod.POST, produces = "application/json")
    public void publishAppModel(HttpServletRequest request, HttpServletResponse httpResponse, @PathVariable String modelId, @RequestBody JsonNode comment) throws IOException{
        ServerConfig targetServerConfig = retrieveOptionalServerConfig(request, "targetServerId");
        if (targetServerConfig == null) {
            clientService.publishAppDefinition(httpResponse, retrieveServerConfig(request), modelId, comment);
        } else {
            clientService.publishAppDefinitionToServer(httpResponse, retrieveServerConfig(request), targetServerConfig, modelId, request.getParameter("replaceAppId"));
        }
    }
    
}
