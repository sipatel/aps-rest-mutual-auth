/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.dto;

import java.util.List;

import com.activiti.domain.ClusterConfig;

/**
 * @author Joram Barrez
 */
public class ClusterConfigRepresentation {

    protected Long id;
    protected String clusterName;
    protected String clusterUserName;
    protected String clusterPassword;
    protected Boolean requiresMasterConfiguration;
    protected Integer metricSendingInterval;
    protected UserRepresentation user;
    protected List<ServerConfigRepresentation> serverConfigs;
    
    public ClusterConfigRepresentation() {
    }
    
    public ClusterConfigRepresentation(ClusterConfig clusterConfig, List<ServerConfigRepresentation> serverConfigs) {
        this.id = clusterConfig.getId();
        this.clusterName = clusterConfig.getClusterName();
        this.clusterPassword = clusterConfig.getClusterPassword();
        this.requiresMasterConfiguration = clusterConfig.getRequiresMasterConfig();
        this.metricSendingInterval = clusterConfig.getMetricSendingInterval();
        this.user = new UserRepresentation(clusterConfig.getUser());
        this.serverConfigs = serverConfigs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterUserName() {
        return clusterUserName;
    }

    public void setClusterUserName(String clusterUserName) {
        this.clusterUserName = clusterUserName;
    }

    public String getClusterPassword() {
        return clusterPassword;
    }

    public void setClusterPassword(String clusterPassword) {
        this.clusterPassword = clusterPassword;
    }

    public Boolean getRequiresMasterConfiguration() {
        return requiresMasterConfiguration;
    }

    public void setRequiresMasterConfiguration(Boolean requiresMasterConfiguration) {
        this.requiresMasterConfiguration = requiresMasterConfiguration;
    }

    public Integer getMetricSendingInterval() {
        return metricSendingInterval;
    }

    public void setMetricSendingInterval(Integer metricSendingInterval) {
        this.metricSendingInterval = metricSendingInterval;
    }

    public UserRepresentation getUser() {
        return user;
    }

    public void setUser(UserRepresentation user) {
        this.user = user;
    }

    public List<ServerConfigRepresentation> getServerConfigs() {
        return serverConfigs;
    }

    public void setServerConfigs(List<ServerConfigRepresentation> serverConfigs) {
        this.serverConfigs = serverConfigs;
    }

}
