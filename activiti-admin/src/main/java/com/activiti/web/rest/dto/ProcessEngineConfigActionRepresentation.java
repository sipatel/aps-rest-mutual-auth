/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.dto;

/**
 * @author jbarrez
 */
public class ProcessEngineConfigActionRepresentation {
	
	protected Long clusterConfigId;
	protected String action;
	
	public Long getClusterConfigId() {
		return clusterConfigId;
	}

	public void setClusterConfigId(Long clusterConfigId) {
		this.clusterConfigId = clusterConfigId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
}
