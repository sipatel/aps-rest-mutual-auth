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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.DecisionTableService;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Yvo Swillens
 * @author Bassam Al-Sarori
 */
@RestController
public class DecisionTablesClientResource extends AbstractClientResource {

    @Autowired
    protected DecisionTableService clientService;

    /**
     * GET list of deployed decision tables.
     */
    @Timed
    @RequestMapping(value="/rest/activiti/decision-tables", method = RequestMethod.GET, produces = "application/json")
    public JsonNode listDecisionTables(HttpServletRequest request) {
        ServerConfig serverConfig = retrieveServerConfig(request);
        Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);
        return clientService.listDecisionTables(serverConfig, parameterMap);
    }
    
    /**
     * GET process definition's list of deployed decision tables.
     */
    @Timed
    @RequestMapping(value = "/rest/activiti/process-definition-decision-tables/{processDefinitionId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getProcessDefinitionDecisionTables(@PathVariable String processDefinitionId, HttpServletRequest request) {
        return clientService.getProcessDefinitionDecisionTables(retrieveServerConfig(request), processDefinitionId);
    }
}