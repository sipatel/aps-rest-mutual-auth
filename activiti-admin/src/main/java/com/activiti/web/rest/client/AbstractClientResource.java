/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client;

import com.activiti.domain.ServerConfig;
import com.activiti.repository.ServerConfigRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.ActivitiEndpointLicenseService;
import com.activiti.service.activiti.AppVersionClientService;
import com.activiti.service.activiti.EndpointUserProfileService;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractClientResource {

	private static final String SERVER_ID = "serverId";

	@Autowired
	protected ServerConfigRepository configRepository;

    @Autowired
    protected AppVersionClientService appVersionClientService;

    @Autowired
    protected EndpointUserProfileService endpointUserProfileService;

    @Autowired
    protected ActivitiEndpointLicenseService activitiEndpointLicenseService;
    
    @Autowired
    protected Environment env;

    /**
     * We keep a set of all server configs that have been 'validated' since boot.
     *
     * See issue #721: it can be the server config is created, but missing vital info such as the tenant id.
     * So, on first use we do a check for it.
     */
    private Set<Long> validatedServerConfigs = new HashSet<Long>();

	protected ServerConfig retrieveServerConfig(HttpServletRequest request) {
	    return retrieveServerConfig(request, SERVER_ID);
	}
	
	protected ServerConfig retrieveServerConfig(HttpServletRequest request, String paramName) {
        ServerConfig serverConfig = null;
        String serverIdParam = request.getParameter(paramName);
        if (StringUtils.isNotEmpty(serverIdParam) && NumberUtils.isNumber(serverIdParam)) {
            Long serverId = Long.valueOf(serverIdParam);
            serverConfig = retrieveServerConfig(serverId);

        } else {
            throw new BadRequestException("valid " + paramName + " parameter is required");
        }
        return serverConfig;
    }
	
	protected ServerConfig retrieveOptionalServerConfig(HttpServletRequest request, String paramName) {
        ServerConfig serverConfig = null;
        String serverIdParam = request.getParameter(paramName);
        if (StringUtils.isNotEmpty(serverIdParam) && NumberUtils.isNumber(serverIdParam)) {
            Long serverId = Long.valueOf(serverIdParam);
            serverConfig = retrieveServerConfig(serverId);
        }
        return serverConfig;
    }

	protected ServerConfig retrieveServerConfig(ObjectNode bodyNode) {
		long serverId = bodyNode.get("serverId").asLong();
		bodyNode.remove("serverId");
		return retrieveServerConfig(serverId);
	}

	protected ServerConfig retrieveServerConfig(long serverId) {
		ServerConfig serverConfig = null;
		
		if (isMultiTenantEnabled() && !SecurityUtils.isCurrentUserAnAdmin()) {
		    serverConfig = configRepository.findByIdAndClusterConfigCreatedByLogin(serverId, SecurityUtils.getCurrentLogin());
		} else {
		    serverConfig = configRepository.findOne(serverId);
		}
		
		if (serverConfig == null) {
		    throw new BadRequestException("Invalid server id");
		}
		
        if (!validatedServerConfigs.contains(serverConfig.getId())) {
            
            // First, determine the type of the server
            String endpointType = appVersionClientService.getEndpointTypeUsingEncryptedPassword(serverConfig.getContextRoot(),
                serverConfig.getRestRoot(),
                serverConfig.getServerAddress(),
                serverConfig.getPort(),
                serverConfig.getUserName(),
                serverConfig.getPassword());

            String tenantId = null; // Bpm suite needs a tenant for the authorization service. Core engine doesn't.
            if (AppVersionClientService.TYPE_BPM_SUITE.equals(endpointType) && StringUtils.isEmpty(serverConfig.getTenantId())) {

                // Fetch tenant id for user of server config. This is a sync http call done before any db transaction is started
                tenantId = endpointUserProfileService.getEndpointUserTenantIdUsingEncryptedPassword(serverConfig.getContextRoot(),
                    serverConfig.getRestRoot(),
                    serverConfig.getServerAddress(),
                    serverConfig.getPort(),
                    serverConfig.getUserName(),
                    serverConfig.getPassword());

                if (StringUtils.isNotEmpty(tenantId)) {
                    // A tenant is stored as 'tenant_x' in Activiti Bpm Suite
                    tenantId = "tenant_" + tenantId;
                }

                serverConfig.setTenantId(tenantId);
                serverConfig = configRepository.save(serverConfig);

                // Validate the new server config
                activitiEndpointLicenseService.checkServerConfigLicenses();

            }

            if (endpointType != null) { // endpointType will be null if the server did not respond correctly. In that case, we don't set the serverconfig as validated
                validatedServerConfigs.add(serverConfig.getId());
            }

        }

        return serverConfig;
	}

	protected Map<String, String[]> getRequestParametersWithoutServerId(HttpServletRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		Map<String, String[]> resultMap = new HashMap<String, String[]>();
		resultMap.putAll(parameterMap);
		resultMap.remove(SERVER_ID);
		return resultMap;
	}
	
	protected boolean isMultiTenantEnabled() {
        return env.getProperty("multi-tenant.enabled", Boolean.class, false);
    }
 }
