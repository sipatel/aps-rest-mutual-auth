/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.DecisionAuditService;
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
public class DecisionAuditsClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(DecisionAuditsClientResource.class);

    @Autowired
    protected DecisionAuditService clientService;

    /**
     * GET  /rest/activiti/decision-audits -> get a list of deployed decision tables.
     */
    @Timed
    @RequestMapping(value = "/rest/activiti/decision-audits", method = RequestMethod.GET, produces = "application/json")
    public JsonNode listDecisionTableAudits(HttpServletRequest request) {
        log.debug("REST request to get a list decision table audits");

        JsonNode resultNode = null;
        ServerConfig serverConfig = retrieveServerConfig(request);
        Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);

        try {
            resultNode = clientService.listDecisionAudits(serverConfig, parameterMap);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }

        return resultNode;
    }
    
    @Timed
    @RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/decision-tasks", method = RequestMethod.GET, produces = "application/json")
    public JsonNode listProcessInstanceDecisionTableAudits(@PathVariable String processInstanceId,
            @RequestParam(required=true) long serverId) {
        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getProcessInstanceDecisionAudit(serverConfig, processInstanceId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}