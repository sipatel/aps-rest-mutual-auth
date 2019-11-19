/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import com.activiti.domain.ServerConfig;
import com.activiti.repository.ServerConfigRepository;
import com.activiti.web.rest.dto.ServerConfigRepresentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jbarrez
 */
@Service
public class ServerConfigService extends AbstractEncryptingService {

    private static final String REST_APP_NAME = "rest.app.name";
    private static final String REST_APP_DESCRIPTION = "rest.app.description";
    private static final String REST_APP_HOST = "rest.app.host";
    private static final String REST_APP_PORT = "rest.app.port";
    private static final String REST_APP_CONTEXT_ROOT = "rest.app.contextroot";
    private static final String REST_APP_REST_ROOT = "rest.app.restroot";
    private static final String REST_APP_USER = "rest.app.user";
    private static final String REST_APP_PASSWORD = "rest.app.password";

    @Autowired
    protected Environment environment;

	@Autowired
	protected ServerConfigRepository serverConfigRepository;

	@Transactional
	public void createDefaultServiceConfigForClusterConfig(Long clusterConfigId) {

        ServerConfig serverConfig = getDefaultServiceConfigForClusterConfig();
        serverConfig.setUserName(environment.getRequiredProperty(REST_APP_USER));
        serverConfig.setPassword(environment.getRequiredProperty(REST_APP_PASSWORD));
        serverConfig.setClusterConfigId(clusterConfigId);

		save(serverConfig, true);
	}

    @Transactional
    public ServerConfig findOne(Long id) {
        return serverConfigRepository.findOne(id);
    }

    @Transactional
    public List<ServerConfigRepresentation> findAll() {
        return createServerConfigRepresentation(serverConfigRepository.findAll());
    }
    
    @Transactional
    public List<ServerConfigRepresentation> findUserServerConfigs(String login) {
        return createServerConfigRepresentation(serverConfigRepository.findByClusterConfigCreatedByLogin(login));
    }
    
    @Transactional
    public List<ServerConfigRepresentation> findUserServerConfigsOfCluster(String login, Long clusterConfigId) {
        return createServerConfigRepresentation(serverConfigRepository.findByClusterConfigIdAndClusterConfigCreatedByLogin(clusterConfigId, login));
    }

    @Transactional
    public List<ServerConfigRepresentation> findByClusterConfigId(Long id) {
        return createServerConfigRepresentation(serverConfigRepository.findByClusterConfigId(id));
    }

    @Transactional
    public void save(ServerConfig serverConfig, boolean encryptPassword) {
        if (encryptPassword) {
            serverConfig.setPassword(encrypt(serverConfig.getPassword()));
        }
        serverConfigRepository.save(serverConfig);
    }
    
    public String getServerConfigDecryptedPassword(ServerConfig serverConfig) {
        return decrypt(serverConfig.getPassword());
    }

    protected List<ServerConfigRepresentation> createServerConfigRepresentation(List<ServerConfig> serverConfigs) {
        List<ServerConfigRepresentation> serversRepresentations = new ArrayList<ServerConfigRepresentation>();
        for (ServerConfig serverConfig: serverConfigs) {
            serversRepresentations.add(createServerConfigRepresentation(serverConfig));
        }
        return serversRepresentations;
    }
    
    protected ServerConfigRepresentation createServerConfigRepresentation(ServerConfig serverConfig) {
        ServerConfigRepresentation serverRepresentation = new ServerConfigRepresentation();
        serverRepresentation.setId(serverConfig.getId());
        serverRepresentation.setName(serverConfig.getName());
        serverRepresentation.setDescription(serverConfig.getDescription());
        serverRepresentation.setServerAddress(serverConfig.getServerAddress());
        serverRepresentation.setServerPort(serverConfig.getPort());
        serverRepresentation.setContextRoot(serverConfig.getContextRoot());
        serverRepresentation.setRestRoot(serverConfig.getRestRoot());
        serverRepresentation.setUserName(serverConfig.getUserName());
        serverRepresentation.setTenantId(serverConfig.getTenantId());
        serverRepresentation.setClusterConfigId(serverConfig.getClusterConfigId());
        return serverRepresentation;
    }
    
    public ServerConfig getDefaultServiceConfigForClusterConfig() {

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setName(environment.getRequiredProperty(REST_APP_NAME));
        serverConfig.setDescription(environment.getRequiredProperty(REST_APP_DESCRIPTION));
        serverConfig.setServerAddress(environment.getRequiredProperty(REST_APP_HOST));
        serverConfig.setPort(environment.getRequiredProperty(REST_APP_PORT, Integer.class));
        serverConfig.setContextRoot(environment.getRequiredProperty(REST_APP_CONTEXT_ROOT));
        serverConfig.setRestRoot(environment.getRequiredProperty(REST_APP_REST_ROOT));
        
        return serverConfig;

    }
    
    protected boolean isMultiTenantEnabled() {
        return environment.getProperty("multi-tenant.enabled", Boolean.class, false);
    }
}
