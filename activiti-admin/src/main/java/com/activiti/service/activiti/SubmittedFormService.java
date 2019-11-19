/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
public class SubmittedFormService {


	@Autowired
	protected ActivitiClientService clientUtil;

	public JsonNode listSubmittedForms(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("/enterprise/submitted-forms");

		for (String name : parameterMap.keySet()) {
			builder.addParameter(name, parameterMap.get(name)[0]);
		}
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder.toString()));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode listFormSubmittedForms(ServerConfig serverConfig, String formId, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("/enterprise/form-submitted-forms/" + formId);
        
	    for (String name : parameterMap.keySet()) {
            builder.addParameter(name, parameterMap.get(name)[0]);
        }
	    
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder.toString()));
        return clientUtil.executeRequest(get, serverConfig);
    }
	
	public JsonNode getSubmittedForm(ServerConfig serverConfig, String submittedFormId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "/enterprise/submitted-forms/" + submittedFormId));
		return clientUtil.executeRequest(get, serverConfig);
	}
	
	public JsonNode getTaskSubmittedForm(ServerConfig serverConfig, String taskId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "/enterprise/task-submitted-form/" + taskId));
        return clientUtil.executeRequest(get, serverConfig);
    }
	
	public JsonNode getProcessSubmittedForms(ServerConfig serverConfig, String processId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "/enterprise/process-submitted-forms/" + processId));
        return clientUtil.executeRequest(get, serverConfig);
    }
	
	public JsonNode downloadUploadContent(ServerConfig serverConfig, String contentId, HttpServletResponse httpResponse) throws IOException {
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, "/enterprise/content/"+contentId+"/raw"));
		return clientUtil.executeDownloadRequest(get, httpResponse, serverConfig);
	}

}
