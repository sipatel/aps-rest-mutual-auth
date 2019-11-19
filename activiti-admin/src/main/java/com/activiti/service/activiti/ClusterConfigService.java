/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.Authority;
import com.activiti.domain.ClusterConfig;
import com.activiti.domain.ServerConfig;
import com.activiti.domain.User;
import com.activiti.repository.ClusterConfigRepository;
import com.activiti.repository.ServerConfigRepository;
import com.activiti.service.UserService;
import com.activiti.service.activiti.cluster.ActivitiClusterService;
import com.activiti.web.rest.dto.ClusterConfigRepresentation;

/**
 * @author jbarrez
 */
@Service
public class ClusterConfigService extends AbstractEncryptingService {
    @Autowired
    protected ClusterConfigRepository clusterConfigRepository;
    
    @Autowired
    protected UserService userService;
    
    @Autowired
    protected MasterConfigurationService masterConfigurationService;
    
    @Autowired
    protected ServerConfigRepository serverConfigRepository;
    
    @Autowired
    protected ServerConfigService serverConfigService;
    
    @Autowired
    protected ActivitiClusterService activitiClusterService;
    
    @Autowired
    protected Environment environment;
    
    @Transactional
    public ClusterConfig findOne(Long clusterConfigId) {
        return clusterConfigRepository.findOne(clusterConfigId);
    }
    
    @Transactional
    public ClusterConfigRepresentation createNewClusterConfig(String clusterName, String userName, String password, boolean requiresMasterCfg) {
        return createNewClusterConfig(clusterName, userName, password, requiresMasterCfg, null);
    }
    
    @Transactional
    public ClusterConfigRepresentation createNewClusterConfig(String clusterName, String userName, String password,
                                                              boolean requiresMasterCfg, Integer metricSendingInterval) {
        return createNewClusterConfig(clusterName, userName, password, requiresMasterCfg, metricSendingInterval, null);
    }
    
    @Transactional
    public ClusterConfigRepresentation createNewClusterConfig(String clusterName, String userName, String password,
                                                              boolean requiresMasterCfg, Integer metricSendingInterval, String createdByUserId) {
        
        // Create the user for accesing the cluster information
        User user = userService.createUser(userName, password, null, null, null, Boolean.FALSE, Authority.ROLE_CLUSTER_MANAGER);
        
        // Create cluster config
        ClusterConfig clusterConfig = new ClusterConfig();
        clusterConfig.setClusterName(clusterName);
        clusterConfig.setClusterPassword(encrypt(password));
        clusterConfig.setUser(user);
        clusterConfig.setRequiresMasterConfig(requiresMasterCfg);
        clusterConfig.setMetricSendingInterval(metricSendingInterval != null ? metricSendingInterval : 60);
        if (createdByUserId != null) {
            clusterConfig.setCreatedBy(userService.getUser(createdByUserId));
        }
        ClusterConfig savedClusterConfig = clusterConfigRepository.save(clusterConfig);
        
        if (!isMultiTenantEnabled()) {
            // Create default server config if in MT mode is disabled
            // otherwise user needs to create a server config manually
            serverConfigService.createDefaultServiceConfigForClusterConfig(savedClusterConfig.getId());
        }
        
        
        // Prod the cluster service that a new instance needs to be added
        activitiClusterService.addCluster(savedClusterConfig.getId());
        
        return createClusterConfigRepresentations(savedClusterConfig);
    }
    
    public String getDecryptedClusterPassword(ClusterConfig clusterConfig) {
        return decrypt(clusterConfig.getClusterPassword());
    }
    
    @Transactional
    public void deleteClusterConfig(Long clusterConfigId) {
        ClusterConfig clusterConfig = clusterConfigRepository.findOne(clusterConfigId);
        if (clusterConfig != null) {
            
            // Delete end point configs
            List<ServerConfig> serverConfigs = serverConfigRepository.findByClusterConfigId(clusterConfigId);
            for (ServerConfig serverConfig : serverConfigs) {
                serverConfigRepository.delete(serverConfig);
            }
            
            // Remove dependent master config
            masterConfigurationService.deleteMasterConfigurationForCluster(clusterConfigId);
            
            // Remove from Activiti cluster service
            activitiClusterService.removeCluster(clusterConfigId);
            
            // Remove data
            User user = clusterConfig.getUser();
            clusterConfigRepository.delete(clusterConfig);
            userService.deleteUser(user.getLogin());
        }
    }
    
    @Transactional
    public void updateClusterConfig(Long clusterConfigId, ClusterConfig clusterConfig) {
        if (clusterConfigRepository.findOne(clusterConfigId) != null) {
            
            // Store the change
            clusterConfigRepository.save(clusterConfig);
            
        } else {
            throw new RuntimeException("No cluster config found with id: " + clusterConfigId);
        }
    }
    
    @Transactional
    public List<ClusterConfigRepresentation> findAll() {
        return createClusterConfigRepresentations(clusterConfigRepository.findAll(new Sort("clusterName")));
    }
    
    @Transactional
    public List<ClusterConfigRepresentation> findUserClusters(String login) {
        return createClusterConfigRepresentations(clusterConfigRepository.findByCreatedByLoginOrderByClusterNameAsc(login));
    }
    
    @Transactional
    public ClusterConfig findClusterConfigByName(String clusterName) {
        return clusterConfigRepository.findByClusterName(clusterName);
    }
    
    protected List<ClusterConfigRepresentation> createClusterConfigRepresentations(List<ClusterConfig> clusterConfigs) {
        List<ClusterConfigRepresentation> configsRepresentations = new ArrayList<ClusterConfigRepresentation>();
        for (ClusterConfig clusterConfig : clusterConfigs) {
            configsRepresentations.add(createClusterConfigRepresentations(clusterConfig));
        }
        return configsRepresentations;
    }
    
    
    protected ClusterConfigRepresentation createClusterConfigRepresentations(ClusterConfig clusterConfig) {
        return new ClusterConfigRepresentation(clusterConfig, serverConfigService.findByClusterConfigId(clusterConfig.getId()));
    }
    
    protected boolean isMultiTenantEnabled() {
        return environment.getProperty("multi-tenant.enabled", Boolean.class, false);
    }
}
