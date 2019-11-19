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
public class FormService {

	@Autowired
	protected ActivitiClientService clientUtil;

	public JsonNode listForms(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("enterprise/forms");

		for (String name : parameterMap.keySet()) {
			builder.addParameter(name, parameterMap.get(name)[0]);
		}
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder.toString()));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode getForm(ServerConfig serverConfig, String formId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "enterprise/forms/" + formId));
		return clientUtil.executeRequest(get, serverConfig);
	}

    public JsonNode getEditorJsonForForm(ServerConfig serverConfig, String formId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "enterprise/forms/" + formId + "/editorJson"));
        return clientUtil.executeRequest(get, serverConfig);
    }
    
    public JsonNode getProcessDefinitionStartForm(ServerConfig serverConfig, String processDefinitionId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "enterprise/process-definitions/" + processDefinitionId + "/start-form"));
        return clientUtil.executeRequest(get, serverConfig);
    }
    
    public JsonNode getProcessDefinitionForms(ServerConfig serverConfig, String processDefinitionId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "enterprise/process-definitions/" + processDefinitionId + "/forms"));
        return clientUtil.executeRequest(get, serverConfig);
    }
}
