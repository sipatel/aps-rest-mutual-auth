/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.domain;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "SERVER_CONFIG")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ServerConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "serverConfigIdGenerator")
    @TableGenerator(name = "serverConfigIdGenerator", table = "HIBERNATE_SEQUENCES")
	protected Long id;

	@Column
	@NotNull
	protected String name;

	@Column
	protected String description;

	@Column(name="server_address")
	@NotNull
	protected String serverAddress;

	@Column
	@NotNull
	protected Integer port;

	@Column(name="context_root")
	protected String contextRoot;

	@Column(name="rest_root")
	protected String restRoot;

	@Column(name="user_name")
	@NotNull
	protected String userName;

    @Column(name = "server_password")
	protected String password;

	@Column(name="cluster_config_id")
	protected Long clusterConfigId;

	// only used repository query
	// to easily fetch server configs
	// of cluster configs
	// created by a specific user
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="cluster_config_id", insertable = false, updatable = false)
    private ClusterConfig clusterConfig;

	@Column(name="tenant_id")
	protected String tenantId;

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public String getRestRoot() {
		return restRoot;
	}

	public void setRestRoot(String restRoot) {
		this.restRoot = restRoot;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getClusterConfigId() {
		return clusterConfigId;
	}

	public void setClusterConfigId(Long clusterConfigId) {
		this.clusterConfigId = clusterConfigId;
	}

    public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerConfig config = (ServerConfig) o;

        if (!id.equals(config.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

	@Override
	public String toString() {
		return "ServerConfig [id=" + id + ", name=" + name + ", description="
				+ description + ", serverAddress=" + serverAddress + ", port="
				+ port + ", contextRoot=" + contextRoot + ", restRoot="
				+ restRoot + ", userName=" + userName + ", password="
				+ password + "tenantId = "+ tenantId + "]";
	}
}
