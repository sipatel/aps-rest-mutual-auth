/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * @author jbarrez
 */
@Entity
@Table(name = "CLUSTER_CONFIG")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ClusterConfig {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "clusterConfigIdGenerator")
    @TableGenerator(name = "clusterConfigIdGenerator", table = "HIBERNATE_SEQUENCES")
    private Long id;

	@Column(name="cluster_name", unique=true)
    private String clusterName;

    @Column(name="cluster_password")
    private String clusterPassword;

    @Column(name="requires_master_cfg")
    private Boolean requiresMasterConfig;

    @Column(name="metric_sending_interval")
    private Integer metricSendingInterval;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="user_id")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User user;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="created_by")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private User createdBy;

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

    public String getClusterPassword() {
        return clusterPassword;
    }

    public void setClusterPassword(String clusterPassword) {
        this.clusterPassword = clusterPassword;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getRequiresMasterConfig() {
        return requiresMasterConfig;
    }

    public void setRequiresMasterConfig(Boolean requiresMasterConfig) {
        this.requiresMasterConfig = requiresMasterConfig;
    }

    public Integer getMetricSendingInterval() {
        return metricSendingInterval;
    }

    public void setMetricSendingInterval(Integer metricSendingInterval) {
        this.metricSendingInterval = metricSendingInterval;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
}
