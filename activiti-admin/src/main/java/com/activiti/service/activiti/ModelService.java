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
public class ModelService {

    public static final String MODEL_LIST_URL = "enterprise/models";
    
    @Autowired
    protected ActivitiClientService clientUtil;

    public JsonNode listModels(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
        URIBuilder builder =  clientUtil.createUriBuilder(MODEL_LIST_URL);
        addParametersToBuilder(builder, parameterMap);
        
        // add tenantId
        addTenantIdParameterToBuilder(builder, serverConfig);
        
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
