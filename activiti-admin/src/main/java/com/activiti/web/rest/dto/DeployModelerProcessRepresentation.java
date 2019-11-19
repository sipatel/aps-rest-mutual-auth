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
public class DeployModelerProcessRepresentation {

	protected Long processModelId;
	
	protected String username;
	
	protected String password;
	
	protected String name;

	public Long getProcessModelId() {
		return processModelId;
	}

	public void setProcessModelId(Long processModelId) {
		this.processModelId = processModelId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
