/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.dto;

import com.activiti.domain.ServerConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * @author Tijs Rademakers
 */
public class ServerConfigRepresentation {
	
	protected Long id;
	protected String name;
	protected String description;
	protected String serverAddress;
	protected Integer serverPort;
	protected String contextRoot;
	protected String restRoot;
	protected String userName;
	protected String password;
	protected String tenantId;
	protected Long clusterConfigId;
	protected String endpointType;
	
	public ServerConfigRepresentation() {
	}
	
	public ServerConfigRepresentation(ServerConfig serverConfig) {
	    this.id = serverConfig.getId();
	    this.name = serverConfig.getName();
	    this.description = serverConfig.getDescription();
	    this.serverAddress = serverConfig.getServerAddress();
	    this.serverPort = serverConfig.getPort();
	    this.contextRoot = serverConfig.getContextRoot();
	    this.restRoot = serverConfig.getRestRoot();
	    this.userName = serverConfig.getUserName();
	    this.tenantId = serverConfig.getTenantId();
	    this.clusterConfigId = serverConfig.getClusterConfigId();
    }
	
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
	public Integer getServerPort() {
		return serverPort;
	}
	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
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
	
	@JsonInclude(Include.NON_NULL)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public Long getClusterConfigId() {
		return clusterConfigId;
	}
	public void setClusterConfigId(Long clusterConfigId) {
		this.clusterConfigId = clusterConfigId;
	}
	public String getEndpointType() {
        return endpointType;
    }
	public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }
	
}
