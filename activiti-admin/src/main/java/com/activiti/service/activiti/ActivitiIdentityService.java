/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
@Service
public class ActivitiIdentityService {

	public static final String USER_LIST_URL = "enterprise/admin/users";
	public static final String USER_URL = "enterprise/admin/users/{0}";
	public static final String GROUP_LIST_URL = "enterprise/admin/groups";
	public static final String GROUP_URL = "enterprise/admin/groups/{0}";
	
	@Autowired
    protected ActivitiClientService clientUtil;
	
	public JsonNode getUsers(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    
	    URIBuilder builder =  clientUtil.createUriBuilder(USER_LIST_URL);
	    addParametersToBuilder(builder, parameterMap);
	    
	    // add tenantId
	    addTenantIdParameterToBuilder(builder, serverConfig);
        
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder));
		return clientUtil.executeRequest(get, serverConfig);
	}
	
	public JsonNode getUser(ServerConfig serverConfig, String userId, Map<String, String[]> parameterMap) {
		if(userId == null) {
			throw new IllegalArgumentException("User id is required");
		}
		
		URIBuilder builder =  clientUtil.createUriBuilder(MessageFormat.format(USER_URL, userId));
        addParametersToBuilder(builder, parameterMap);

		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder));
		return clientUtil.executeRequest(get, serverConfig);
	}
	
	public JsonNode getGroups(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    
	    URIBuilder builder =  clientUtil.createUriBuilder(GROUP_LIST_URL);
        addParametersToBuilder(builder, parameterMap);
        
        // add tenantId
        addTenantIdParameterToBuilder(builder, serverConfig);
        
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder));
		return clientUtil.executeRequest(get, serverConfig);
	}
	
	public JsonNode getGroup(ServerConfig serverConfig, String groupId, Map<String, String[]> parameterMap) {
		if(groupId == null) {
			throw new IllegalArgumentException("Group id is required");
		}

		URIBuilder builder =  clientUtil.createUriBuilder(MessageFormat.format(GROUP_URL, groupId));
		addParametersToBuilder(builder, parameterMap);
		
		HttpGet get = new HttpGet(clientUtil.getServerUrlWithoutTenantId(serverConfig, builder));
		return clientUtil.executeRequest(get, serverConfig);
	}
	
	protected void addParametersToBuilder(URIBuilder builder, Map<String, String[]> parameterMap) {
	    for (String name : parameterMap.keySet()) {
            builder.addParameter(name, parameterMap.get(name)[0]);
        }
	}
	
	protected void addTenantIdParameterToBuilder(URIBuilder builder, ServerConfig serverConfig) {
	    if (serverConfig.getTenantId() != null) {
	        // A tenant is stored as 'tenant_x' in ServerConfig where x is the id
	        String tenantSplit [] = serverConfig.getTenantId().split("_");
	        builder.addParameter("tenantId", tenantSplit[tenantSplit.length - 1]);
	    }
	}
}
