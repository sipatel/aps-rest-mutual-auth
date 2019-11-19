/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class VariableInstanceService {
	
	private final Logger log = LoggerFactory.getLogger(VariableInstanceService.class);
	
	public static final String HISTORIC_VARIABLE_INSTANCE_URL = "history/historic-variable-instances/{0}/data";
	
	@Autowired
    protected ActivitiClientService clientUtil;
	
	public JsonNode getVariableData(ServerConfig serverConfig, String variableId, HttpServletResponse httpResponse) {
		URIBuilder builder = clientUtil.createUriBuilder(MessageFormat.format(HISTORIC_VARIABLE_INSTANCE_URL, variableId));
       
        builder.addParameter("variableId", variableId);
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
		
		return clientUtil.executeDownloadVariableRequest(get, httpResponse, serverConfig);
	}
	
	
	

}
