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

/**
 * @author Yvo Swillens
 */
@RestController
public class DecisionAuditClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(DecisionAuditClientResource.class);

    @Autowired
    protected DecisionAuditService clientService;

    /**
     * GET  /rest/activiti/decision-audits/{decisionAuditId} -> get a list of deployed decision tables.
     */
    @Timed
    @RequestMapping(value = "/rest/activiti/decision-audits/{decisionAuditId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode listDecisionTableAudits(@PathVariable String decisionAuditId,
            @RequestParam(required=true) long serverId) {
        log.debug("REST request to get a decision table audit");

        ServerConfig serverConfig = retrieveServerConfig(serverId);
        try {
            return clientService.getDecisionAudit(serverConfig, decisionAuditId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}