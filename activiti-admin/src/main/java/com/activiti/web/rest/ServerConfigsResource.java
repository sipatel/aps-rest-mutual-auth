/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import java.util.List;

import com.activiti.service.activiti.exception.ActivitiServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ClusterConfig;
import com.activiti.domain.ServerConfig;
import com.activiti.security.SecurityUtils;
import com.activiti.service.ActivitiEndpointLicenseService;
import com.activiti.service.activiti.AppVersionClientService;
import com.activiti.service.activiti.ClusterConfigService;
import com.activiti.service.activiti.EndpointUserProfileService;
import com.activiti.service.activiti.ServerConfigService;
import com.activiti.web.rest.dto.ServerConfigRepresentation;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.NotFoundException;
import com.activiti.web.rest.exception.NotPermittedException;
import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing the server configs.
 *
 * @author trademak
 * @author jbarrez
 */
@RestController
public class ServerConfigsResource {

    private final static Logger log = LoggerFactory.getLogger(ServerConfigsResource.class);

    @Autowired
    private ActivitiEndpointLicenseService activitiEndpointLicenseService;

    @Autowired
    private ServerConfigService serverConfigService;

    @Autowired
    private ClusterConfigService clusterConfigService;

    @Autowired
    private AppVersionClientService appVersionClientService;

    @Autowired
    private EndpointUserProfileService endpointUserProfileService;

    @Autowired
    protected Environment env;

    @Timed
    @RequestMapping(value = "/rest/server-configs", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<ServerConfigRepresentation> getServers(@RequestParam(value = "clusterConfigId", required = false) Long clusterConfigId) {
        if (clusterConfigId == null) {
            if (isMultiTenantEnabled()) {
                return serverConfigService.findUserServerConfigs(SecurityUtils.getCurrentLogin());
            }
            return serverConfigService.findAll();
        } else {
            if (isMultiTenantEnabled()) {
                return serverConfigService.findUserServerConfigsOfCluster(SecurityUtils.getCurrentLogin(), clusterConfigId);
            }
            return serverConfigService.findByClusterConfigId(clusterConfigId);
        }
    }

    @Timed
    @RequestMapping(value = "/rest/server-configs/default", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public ServerConfigRepresentation getDefaultServerConfig() {
        return new ServerConfigRepresentation(serverConfigService.getDefaultServiceConfigForClusterConfig());
    }

    @Timed
    @RequestMapping(value = "/rest/server-configs/{serverId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public ServerConfigRepresentation getServer(@PathVariable Long serverId) {
        ServerConfig serverConfig = serverConfigService.findOne(serverId);

        if (serverConfig == null) {
            throw new NotFoundException("No server config with id " + serverId + " exist");
        }

        String endpointType = null;
        try {
            endpointType = appVersionClientService.getEndpointType(serverConfig.getContextRoot(),
                    serverConfig.getRestRoot(),
                    serverConfig.getServerAddress(),
                    serverConfig.getPort(),
                    serverConfig.getUserName(),
                    serverConfigService.getServerConfigDecryptedPassword(serverConfig));
        } catch (Exception ex) {
            // could not reach server
        }

        ServerConfigRepresentation serverRepresentation = createServerConfigRepresentation(serverConfig);
        serverRepresentation.setEndpointType(endpointType);

        return serverRepresentation;
    }

    @Timed
    @RequestMapping(value = "/rest/server-configs", method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public void createServer(@RequestBody ServerConfigRepresentation configRepresentation) {

        if (StringUtils.isEmpty(configRepresentation.getUserName())) {
            throw new BadRequestException("A username is required.");
        }

        if (StringUtils.isEmpty(configRepresentation.getPassword())) {
            throw new BadRequestException("A password is required.");
        }

        if (configRepresentation.getClusterConfigId() == null) {
            throw new BadRequestException("A cluster config is required.");
        }

        ClusterConfig clusterConfig = clusterConfigService.findOne(configRepresentation.getClusterConfigId());

        if (clusterConfig == null) {
            throw new BadRequestException("No cluster with id '" + configRepresentation.getClusterConfigId() + "' exists.");
        }

        if (isMultiTenantEnabled()) {
            // check if user is allowed to use the cluster
            if (!SecurityUtils.getCurrentLogin().equals(clusterConfig.getCreatedBy().getLogin())) {
                throw new NotPermittedException("You are not allowed to add servers to this cluster");
            }

        }

        // First, determine the type of the server
        String endpointType = null;
        boolean checkLicense = true;

        try {
            endpointType = appVersionClientService.getEndpointType(configRepresentation.getContextRoot(),
                    configRepresentation.getRestRoot(),
                    configRepresentation.getServerAddress(),
                    configRepresentation.getServerPort(),
                    configRepresentation.getUserName(),
                    configRepresentation.getPassword());
        } catch (ActivitiServiceException ase) {
            checkLicense = false;
        } catch (Exception ex) {
            log.error("Could not determine endpoint type", ex);
        }

        String tenantId = null; // Bpm suite needs a tenant for the authorization service. Core engine doesn't.
        if (AppVersionClientService.TYPE_BPM_SUITE.equals(endpointType)) {

            // Fetch tenant id for user of server config. This is a sync http call done before any db transaction is started
            tenantId = endpointUserProfileService.getEndpointUserTenantId(
                    configRepresentation.getContextRoot(),
                    configRepresentation.getRestRoot(),
                    configRepresentation.getServerAddress(),
                    configRepresentation.getServerPort(),
                    configRepresentation.getUserName(),
                    configRepresentation.getPassword());

            if (StringUtils.isNotEmpty(tenantId)) {
                // A tenant is stored as 'tenant_x' in Activiti Bpm Suite
                tenantId = "tenant_" + tenantId;
            }

        }

        ServerConfig config = new ServerConfig();
        config.setContextRoot(configRepresentation.getContextRoot());
        config.setDescription(configRepresentation.getDescription());
        config.setName(SecurityUtils.getCleanedClrfSequencesString(configRepresentation.getName()));
        config.setPort(configRepresentation.getServerPort());
        config.setRestRoot(configRepresentation.getRestRoot());
        config.setServerAddress(configRepresentation.getServerAddress());
        config.setUserName(configRepresentation.getUserName());
        config.setClusterConfigId(configRepresentation.getClusterConfigId());
        config.setPassword(configRepresentation.getPassword());

        config.setTenantId(tenantId);

        serverConfigService.save(config, true);

        // Validate the new server config
        if (checkLicense) {
            activitiEndpointLicenseService.checkServerConfigLicenses();
        }

    }

    @Timed
    @RequestMapping(value = "/rest/server-configs/{serverId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateServer(@PathVariable Long serverId, @RequestBody ServerConfigRepresentation configRepresentation) {

        ServerConfig config = serverConfigService.findOne(serverId);

        if (config == null) {
            throw new BadRequestException("Server with id '" + serverId
                    + "' does not exist");
        }

        boolean updatePassword = false;
        if (StringUtils.isNotEmpty(configRepresentation.getPassword())) {
            config.setPassword(configRepresentation.getPassword());
            updatePassword = true;
        } else {
            configRepresentation.setPassword(serverConfigService.getServerConfigDecryptedPassword(config));
        }

        String endpointType = null;
        boolean checkLicense = true;

        // First, determine the type of the server
        try {
            endpointType = appVersionClientService.getEndpointType(configRepresentation.getContextRoot(),
                    configRepresentation.getRestRoot(),
                    configRepresentation.getServerAddress(),
                    configRepresentation.getServerPort(),
                    configRepresentation.getUserName(),
                    configRepresentation.getPassword());
        } catch (ActivitiServiceException ase) {
            checkLicense = false;
        } catch (Exception ex) {
            log.error("Could not determine endpoint type", ex);
        }

        String tenantId = null; // Bpm suite needs a tenant for the authorization service. Core engine doesn't.
        if (AppVersionClientService.TYPE_BPM_SUITE.equals(endpointType)) {

            // Fetch tenant id for user of server config. This is a sync http call done before any db transaction is started
            tenantId = endpointUserProfileService.getEndpointUserTenantId(
                    configRepresentation.getContextRoot(),
                    configRepresentation.getRestRoot(),
                    configRepresentation.getServerAddress(),
                    configRepresentation.getServerPort(),
                    configRepresentation.getUserName(),
                    configRepresentation.getPassword());

            if (StringUtils.isNotEmpty(tenantId)) {
                // A tenant is stored as 'tenant_x' in Activiti Bpm Suite
                tenantId = "tenant_" + tenantId;
            }

        }

        config.setContextRoot(configRepresentation.getContextRoot());
        config.setDescription(configRepresentation.getDescription());
        config.setName(SecurityUtils.getCleanedClrfSequencesString(configRepresentation.getName()));
        config.setPort(configRepresentation.getServerPort());
        config.setRestRoot(configRepresentation.getRestRoot());
        config.setServerAddress(configRepresentation.getServerAddress());
        config.setUserName(configRepresentation.getUserName());

        config.setTenantId(tenantId);

        serverConfigService.save(config, updatePassword);

        // Validate the new server config
        if (checkLicense) {
            activitiEndpointLicenseService.checkServerConfigLicense(config);
        }

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

    protected boolean isMultiTenantEnabled() {
        return env.getProperty("multi-tenant.enabled", Boolean.class, false);
    }
}
