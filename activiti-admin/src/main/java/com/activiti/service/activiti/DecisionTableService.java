/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class DecisionTableService {

	@Autowired
	protected ActivitiClientService clientUtil;

	public JsonNode listDecisionTables(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("enterprise/decisions/decision-tables");

		for (String name : parameterMap.keySet()) {
			builder.addParameter(name, parameterMap.get(name)[0]);
		}
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder.toString()));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode getDecisionTable(ServerConfig serverConfig, String decisionTableId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "enterprise/decisions/decision-tables/" + decisionTableId));
		return clientUtil.executeRequest(get, serverConfig);
	}

    public JsonNode getEditorJsonForDecisionTable(ServerConfig serverConfig, String decisionTableId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "enterprise/decisions/decision-tables/" + decisionTableId + "/editorJson"));
        return clientUtil.executeRequest(get, serverConfig);
    }
    
    public JsonNode getProcessDefinitionDecisionTables(ServerConfig serverConfig, String processDefinitionId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "enterprise/process-definitions/" + processDefinitionId + "/decision-tables"));
        return clientUtil.executeRequest(get, serverConfig);
    }
    
}
