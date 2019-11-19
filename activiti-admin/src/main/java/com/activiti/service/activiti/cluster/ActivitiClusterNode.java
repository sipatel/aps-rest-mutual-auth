/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author jbarrez
 */
public class ActivitiClusterNode {

	public static final int MAX_EVENTS = 1000;

	public enum STATE {
		UNKNOWN, BOOTING, RUNNING, CLOSED
	}

    protected String nodeId;
    protected String ipAddress;

    protected STATE state;
	protected Date lastSeenAlive;

	protected boolean usingMasterConfiguration;
	protected boolean correctMasterConfiguration;
	protected String masterConfigurationId;

	protected boolean asyncExecutorRunning;

	protected Map<String, Object> jvmMetrics;
	protected Map<String, Object> runtimeMetrics;
	protected Map<String, Object> processDefinitionCacheMetrics;
    protected Map<String, Object> jobFailures;

    // Activiti BPM Suite
    protected String bpmSuiteMajorVersion;
    protected String bpmSuiteMinorVersion;
    protected String bpmSuiteRevisionVersion;
    protected String bpmSuiteEdition;
    protected Map<String, Object> bpmSuiteRestMeters;
    protected Map<String, Object> bpmSuiteRestTimers;
    protected String bpmSuiteElasticSearchStatsJson; // The json is passed as-is for performance

	public ActivitiClusterNode(String nodeId, String ipAddress) {
		this.nodeId = nodeId;
        this.ipAddress = ipAddress;
		this.state = STATE.UNKNOWN;
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

	public STATE getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}

	public boolean isUsingMasterConfiguration() {
		return usingMasterConfiguration;
	}

	public void setUsingMasterConfiguration(boolean usingMasterConfiguration) {
		this.usingMasterConfiguration = usingMasterConfiguration;
	}

	public boolean isCorrectMasterConfiguration() {
		return correctMasterConfiguration;
	}

	public void setCorrectMasterConfiguration(boolean correctMasterConfiguration) {
		this.correctMasterConfiguration = correctMasterConfiguration;
	}

	public String getMasterConfigurationId() {
		return masterConfigurationId;
	}

	public void setMasterConfigurationId(String masterConfigurationId) {
		this.masterConfigurationId = masterConfigurationId;
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

    public Map<String, Object> getJobFailures() {
        return jobFailures;
    }

    public void setJobFailures(Map<String, Object> jobFailures) {
        this.jobFailures = jobFailures;
    }

    public String getBpmSuiteMajorVersion() {
        return bpmSuiteMajorVersion;
    }

    public void setBpmSuiteMajorVersion(String bpmSuiteMajorVersion) {
        this.bpmSuiteMajorVersion = bpmSuiteMajorVersion;
    }

    public String getBpmSuiteMinorVersion() {
        return bpmSuiteMinorVersion;
    }

    public void setBpmSuiteMinorVersion(String bpmSuiteMinorVersion) {
        this.bpmSuiteMinorVersion = bpmSuiteMinorVersion;
    }

    public String getBpmSuiteRevisionVersion() {
        return bpmSuiteRevisionVersion;
    }

    public void setBpmSuiteRevisionVersion(String bpmSuiteRevisionVersion) {
        this.bpmSuiteRevisionVersion = bpmSuiteRevisionVersion;
    }

    public String getBpmSuiteEdition() {
        return bpmSuiteEdition;
    }

    public void setBpmSuiteEdition(String bpmSuiteEdition) {
        this.bpmSuiteEdition = bpmSuiteEdition;
    }

    public Map<String, Object> getBpmSuiteRestMeters() {
        return bpmSuiteRestMeters;
    }

    public void setBpmSuiteRestMeters(Map<String, Object> bpmSuiteRestMeters) {
        this.bpmSuiteRestMeters = bpmSuiteRestMeters;
    }

    public Map<String, Object> getBpmSuiteRestTimers() {
        return bpmSuiteRestTimers;
    }

    public void setBpmSuiteRestTimers(Map<String, Object> bpmSuiteRestTimers) {
        this.bpmSuiteRestTimers = bpmSuiteRestTimers;
    }

    public String getBpmSuiteElasticSearchStatsJson() {
        return bpmSuiteElasticSearchStatsJson;
    }

    public void setBpmSuiteElasticSearchStatsJson(String bpmSuiteElasticSearchStatsJson) {
        this.bpmSuiteElasticSearchStatsJson = bpmSuiteElasticSearchStatsJson;
    }
}
