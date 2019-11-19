/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class DecisionAuditService {

	private final Logger log = LoggerFactory.getLogger(DecisionAuditService.class);

	@Autowired
	protected ActivitiClientService clientUtil;

	public JsonNode listDecisionAudits(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("/enterprise/decisions/audits");

		for (String name : parameterMap.keySet()) {
			builder.addParameter(name, parameterMap.get(name)[0]);
		}
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder.toString()));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode getDecisionAudit(ServerConfig serverConfig, String decisionAuditId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "/enterprise/decisions/audits/" + decisionAuditId));
		return clientUtil.executeRequest(get, serverConfig);
	}
	
	public JsonNode getProcessInstanceDecisionAudit(ServerConfig serverConfig, String processInstanceId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "/enterprise/process-instances/" + processInstanceId + "/decision-tasks"));
        return clientUtil.executeRequest(get, serverConfig);
    }
}
