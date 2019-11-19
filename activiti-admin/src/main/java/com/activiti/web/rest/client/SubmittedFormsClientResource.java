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
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.SubmittedFormService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.InternalServerErrorException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
@RestController
public class SubmittedFormsClientResource extends AbstractClientResource {

    @Autowired
    protected SubmittedFormService clientService;

    @RequestMapping(value = "/rest/activiti/submitted-forms", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode listSubmittedForms(HttpServletRequest request) {
        JsonNode resultNode = null;
        ServerConfig serverConfig = retrieveServerConfig(request);
        Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);

        try {
            resultNode = clientService.listSubmittedForms(serverConfig, parameterMap);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }

        return resultNode;
    }
    
    @RequestMapping(value = "/rest/activiti/form-submitted-forms/{formId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode listFomrSubmittedForms(HttpServletRequest request, @PathVariable String formId, @RequestParam(required=true) long serverId) {
        ServerConfig serverConfig = retrieveServerConfig(serverId);

        try {
            return clientService.listFormSubmittedForms(serverConfig, formId, getRequestParametersWithoutServerId(request));
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/rest/activiti/task-submitted-form/{taskId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getTaskSubmittedForm(@PathVariable String taskId, @RequestParam(required=true) long serverId) {
        ServerConfig serverConfig = retrieveServerConfig(serverId);

        try {
            return clientService.getTaskSubmittedForm(serverConfig, taskId);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }

    }
    
    @RequestMapping(value = "/rest/activiti/process-submitted-forms/{processId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getProcessSubmittedForms(@PathVariable String processId, @RequestParam(required=true) long serverId) {
        ServerConfig serverConfig = retrieveServerConfig(serverId);

        try {
            return clientService.getProcessSubmittedForms(serverConfig, processId);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/rest/activiti/submitted-forms/{submittedFormId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getSubmittedForm(@PathVariable String submittedFormId, @RequestParam(required=true) long serverId) {
        ServerConfig serverConfig = retrieveServerConfig(serverId);

        try {
            return clientService.getSubmittedForm(serverConfig, submittedFormId);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/rest/activiti/submitted-forms/content/{contentId}/raw", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode downloadUploadContent(HttpServletRequest request,@PathVariable String contentId, HttpServletResponse httpResponse) {
        ServerConfig serverConfig = retrieveServerConfig(request);

        try {
            return clientService.downloadUploadContent(serverConfig, contentId, httpResponse);

        } catch (IOException e) {
        	throw new InternalServerErrorException("Could not download content: " + e.getMessage());
        }
    }
}