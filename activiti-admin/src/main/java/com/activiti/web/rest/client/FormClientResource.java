/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.FormService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
@RestController
public class FormClientResource extends AbstractClientResource {

    @Autowired
    protected FormService clientService;

    @RequestMapping(value = "/rest/activiti/forms/{formId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getForm(@PathVariable String formId,
            @RequestParam(required=true) long serverId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getForm(serverConfig, formId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @RequestMapping(value = "/rest/activiti/forms/{formId}/editorJson", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getEditorJsonForForm(@PathVariable String formId,
            @RequestParam(required=true) long serverId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getEditorJsonForForm(serverConfig, formId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/rest/activiti/process-definition-start-form/{processDefinitionId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getProcessDefinitionStartForm(@PathVariable String processDefinitionId,
            @RequestParam(required=true) long serverId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getProcessDefinitionStartForm(serverConfig, processDefinitionId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}