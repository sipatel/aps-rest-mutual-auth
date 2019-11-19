/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author jbarrez
 */
public class ClusterNodeRepresentation {

	protected String nodeId;
    protected String ipAddress;

	protected String state;
	protected Date lastSeenAlive;

	protected boolean usingMasterConfiguration;
	protected String masterConfigurationId;
	protected boolean correctMasterConfiguration;

	protected boolean asyncExecutorRunning;

	protected Map<String, Object> jvmMetrics;
	protected Map<String, Object> runtimeMetrics;
	protected Map<String, Object> processDefinitionCacheMetrics;

	public ClusterNodeRepresentation() {

	}

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ClusterNodeRepresentation(String nodeId, String ipAddress, String state) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
		this.state = state;
	}

	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public boolean isUsingMasterConfiguration() {
		return usingMasterConfiguration;
	}
	public void setUsingMasterConfiguration(boolean usingMasterConfiguration) {
		this.usingMasterConfiguration = usingMasterConfiguration;
	}
	public String getMasterConfigurationId() {
		return masterConfigurationId;
	}
	public void setMasterConfigurationId(String masterConfigurationId) {
		this.masterConfigurationId = masterConfigurationId;
	}
	public boolean isCorrectMasterConfiguration() {
		return correctMasterConfiguration;
	}
	public void setCorrectMasterConfiguration(boolean correctMasterConfiguration) {
		this.correctMasterConfiguration = correctMasterConfiguration;
	}
	public Map<String, Object> getJvmMetrics() {
		return jvmMetrics;
	}
	public void setJvmMetrics(Map<String, Object> jvmMetrics) {
		this.jvmMetrics = jvmMetrics;
	}
	public Map<String, Object> getProcessDefinitionCacheMetrics() {
		return processDefinitionCacheMetrics;
	}
	public void setProcessDefinitionCacheMetrics(Map<String, Object> processDefinitionCacheMetrics) {
		this.processDefinitionCacheMetrics = processDefinitionCacheMetrics;
	}
	public Map<String, Object> getRuntimeMetrics() {
		return runtimeMetrics;
	}
	public void setRuntimeMetrics(Map<String, Object> runtimeMetrics) {
		this.runtimeMetrics = runtimeMetrics;
	}

	public Date getLastSeenAlive() {
		return lastSeenAlive;
	}

	public void setLastSeenAlive(Date lastSeenAlive) {
		this.lastSeenAlive = lastSeenAlive;
	}

	public boolean isAsyncExecutorRunning() {
		return asyncExecutorRunning;
	}

	public void setAsyncExecutorRunning(boolean asyncExecutorRunning) {
		this.asyncExecutorRunning = asyncExecutorRunning;
	}

	public final class Event {

		protected Date timestamp;
		protected String event;
		public Event(Date timestamp, String event) {
			this.timestamp = timestamp;
			this.event = event;
		}
		public Date getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		public String getEvent() {
			return event;
		}
		public void setEvent(String event) {
			this.event = event;
		}

	}

}
