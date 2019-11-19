/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.DecisionTableService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Yvo Swillens
 */
@RestController
public class DecisionTableClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(DecisionTableClientResource.class);

    @Autowired
    protected DecisionTableService clientService;

    @RequestMapping(value = "/rest/activiti/decision-tables/{decisionTableId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getDecisionTable(@PathVariable String decisionTableId,
            @RequestParam(required=true) long serverId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getDecisionTable(serverConfig, decisionTableId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @RequestMapping(value = "/rest/activiti/decision-tables/{decisionTableId}/editorJson", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public JsonNode getEditorJsonForDecisionTable(@PathVariable String decisionTableId,
            @RequestParam(required=true) long serverId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getEditorJsonForDecisionTable(serverConfig, decisionTableId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}