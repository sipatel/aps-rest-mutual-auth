/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.AppService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.InternalServerErrorException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * REST controller for getting APP details
 */
@RestController
public class AppDeploymentClientResource extends AbstractClientResource {

	@Autowired
	protected AppService clientService;

	@RequestMapping(value = "/rest/activiti/apps/{appId}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public JsonNode getAppDefinition(@PathVariable String appId,
			@RequestParam(required=true) long serverId) throws BadRequestException {
		
		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getAppDefinition(serverConfig, appId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/apps/{appId}", method = RequestMethod.DELETE)
    @Timed
    public void deleteAppDeployment(HttpServletResponse respone, @PathVariable String appId,
            @RequestParam(required=true) long serverId) {
        clientService.deleteAppDeployment(retrieveServerConfig(serverId), respone, appId);
    }
	
	@RequestMapping(value = "/rest/activiti/app", method = RequestMethod.GET)
    @Timed
    public void getAppDefinitionByDeployment(HttpServletRequest request, HttpServletResponse respone) {
        clientService.getAppDefinitionByDeployment(retrieveServerConfig(request), respone, getRequestParametersWithoutServerId(request));
    }

    //todo: should not be path variable but request
    @RequestMapping(value = "/rest/activiti/apps/process-definitions/{deploymentId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getProcessDefinitionsForDeploymentId(@PathVariable String deploymentId,
            @RequestParam(required=true) long serverId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getProcessDefinitionsForDeploymentId(serverConfig, deploymentId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    //todo: should not be path variable but request
    @RequestMapping(value = "/rest/activiti/apps/decision-tables/{dmnDeploymentId}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public JsonNode getDecisionTablesForDeploymentId(@PathVariable String dmnDeploymentId,
			@RequestParam(required=true) long serverId) throws BadRequestException {

		ServerConfig serverConfig = retrieveServerConfig(serverId);
		try {
			return clientService.getDecisionDefinitionsForDeploymentId(serverConfig, dmnDeploymentId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

    //todo: should not be path variable but request
    @RequestMapping(value = "/rest/activiti/apps/forms/{appDeploymentId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getFormsForAppDeploymentId(@PathVariable String appDeploymentId,
            @RequestParam(required=true) long serverId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getFormsForAppDeploymentId(serverConfig, appDeploymentId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @Timed
    @RequestMapping(value="/rest/activiti/apps/export/{deploymentId}",method=RequestMethod.GET, produces = "application/json")
    public JsonNode exportApp(HttpServletRequest request,@PathVariable String deploymentId, HttpServletResponse httpResponse){
        ServerConfig serverConfig = retrieveServerConfig(request);
        try {
            return clientService.exportApp(serverConfig, deploymentId, httpResponse);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not download app: " + e.getMessage());
        }
    }
    
    @Timed
    @RequestMapping(value="/rest/activiti/apps/redeploy/{deploymentId}",method=RequestMethod.GET, produces = "application/json")
    public JsonNode redeployApp(HttpServletRequest request, HttpServletResponse httpResponse, @PathVariable String deploymentId){
        ServerConfig serverConfig = retrieveServerConfig(request);
        ServerConfig targetServerConfig = retrieveServerConfig(request, "targetServerId");
        try {
            return clientService.redeployApp(httpResponse, serverConfig, targetServerConfig, deploymentId);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not redeploy app: " + e.getMessage());
        }
    }
    
    @Timed
    @RequestMapping(value="/rest/activiti/apps/redeploy/{deploymentId}/{replaceAppId}",method=RequestMethod.GET, produces = "application/json")
    public JsonNode redeployApp(HttpServletRequest request, HttpServletResponse httpResponse, @PathVariable String deploymentId, @PathVariable String replaceAppId){
        ServerConfig serverConfig = retrieveServerConfig(request);
        ServerConfig targetServerConfig = retrieveServerConfig(request, "targetServerId");
        try {
            return clientService.redeployReplaceApp(httpResponse, serverConfig, targetServerConfig, deploymentId, replaceAppId);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not redeploy app: " + e.getMessage());
        }
    }
}
