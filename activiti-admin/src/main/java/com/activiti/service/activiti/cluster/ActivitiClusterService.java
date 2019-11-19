/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.ClusterConfig;
import com.activiti.repository.ClusterConfigRepository;
import com.activiti.service.ActivitiEndpointLicenseService;
import com.activiti.service.activiti.MasterConfigurationService;

/**
 * @author jbarrez
 */
@Service
@DependsOn(value = {"liquibase", "minimalDataGenerator"}) // The database needs to exist. Needed for development/testing environment.
public class ActivitiClusterService implements EnvironmentAware {

	private final Logger logger = LoggerFactory.getLogger(ActivitiClusterService.class);

	/* Dependencies */

	@Autowired
	protected ClusterConfigRepository clusterConfigRepository;

    @Autowired
    protected MasterConfigurationService masterConfigurationService;

    @Autowired
    protected ActivitiEndpointLicenseService activitiEndpointLicenseService;

	/* Instance members */

    protected long maxInactiveTimeInMs;

	protected Map<String, ActivitiCluster> clusters = Collections.synchronizedMap(new HashMap<String, ActivitiCluster>());


	public ActivitiClusterService() { }

	@PostConstruct
	public void loadClusters() {
		for (ClusterConfig clusterConfig : clusterConfigRepository.findAll()) {
            ActivitiCluster activitiCluster = new ActivitiCluster(clusterConfig);
            activitiCluster.setCurrentMasterConfigurationId(
                masterConfigurationService.findMasterConfigurationUuidByClusterConfigId(clusterConfig.getId()));
			clusters.put(clusterConfig.getClusterName(), activitiCluster);
		}
	}

    @Override
    public void setEnvironment(Environment environment) {
        maxInactiveTimeInMs = environment.getProperty("cluster.monitoring.max.inactive.time", Long.class, 600000L);
    }

	public void addCluster(Long clusterConfigId) {
		ClusterConfig clusterConfig = clusterConfigRepository.findOne(clusterConfigId);
		if (clusterConfig != null) {
			if (!clusters.containsKey(clusterConfig.getClusterName())) {
				clusters.put(clusterConfig.getClusterName(), new ActivitiCluster(clusterConfig));
				logger.info("Cluster " + clusterConfig.getClusterName() + " has been added to cluster service.");
			} else {
				logger.warn("Already a cluster found with name " + clusterConfig.getClusterName()
						+ ". This is most likely a programmatic error. Original one will be overwritten");
			}
		} else {
			logger.warn("No cluster found with id " + clusterConfigId + ". This is most likely a programmatic error.");
		}
	}

	public void removeCluster(Long clusterConfigId) {
        ClusterConfig clusterConfig = clusterConfigRepository.findOne(clusterConfigId);
		ActivitiCluster cluster = clusters.get(clusterConfig.getClusterName());
		if (cluster != null) {
			clusters.remove(clusterConfigId);
			logger.info("Cluster " + cluster.getClusterConfig().getClusterName() + " is removed");
		} else {
			logger.warn("No cluster registered with id " + clusterConfigId + ". This is most likely a programmatic error.");
		}
	}

	/** Called when a cluster master config has been updated by the user */
	@Transactional
	public void updateMasterConfiguration(Long clusterConfigId) {
        ClusterConfig clusterConfig = clusterConfigRepository.findOne(clusterConfigId);
		ActivitiCluster cluster = clusters.get(clusterConfig.getClusterName());
		if (cluster != null) {
			cluster.setCurrentMasterConfigurationId(masterConfigurationService.findMasterConfigurationUuidByClusterConfigId(clusterConfigId));
		} else {
			logger.warn("Could not find cluster with id " + clusterConfigId + ". Master cfg NOT updated");
		}
	}

	/** Called when a cluster master config has been deleted by the user */
	@Transactional
	public void deleteMasterConfiguration(Long clusterConfigId) {
        ClusterConfig clusterConfig = clusterConfigRepository.findOne(clusterConfigId);
		ActivitiCluster cluster = clusters.get(clusterConfig.getClusterName());
		if (cluster != null) {
            cluster.setCurrentMasterConfigurationId(null);
		} else {
			logger.warn("Could not find cluster with id " + clusterConfigId + ". Master cfg NOT deleted");
		}
	}

    public void eventReceived(String clusterName, Map<String, Object> eventData) {
        if (eventData != null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Got event " + eventData.get("type") + " from " + eventData.get("nodeId"));
            }

            // Get cluster
            ActivitiCluster cluster = clusters.get(clusterName);
            if (cluster == null) {
                logger.warn("No cluster node found for '" + clusterName + "'. Ignoring event.");
                return;
            }

            // Get cluster node
            String nodeId = (String) eventData.get("nodeId");
            String ipAddress = (String) eventData.get("ipAddress");
            if (StringUtils.isEmpty(nodeId)) {
                logger.warn("Event received without nodeId property. Ignoring it .");
                return;
            }

            // Find cluster node
            ActivitiClusterNode clusterNode = cluster.getClusterNodes().get(nodeId);
            if (clusterNode == null) { // First event from this node
                clusterNode = cluster.addNode(nodeId, ipAddress);

                // Give the endpoint checker a node. Could be a new node.
                activitiEndpointLicenseService.checkServerConfigLicenses();

            }

            // Update the last seen alive timestamp
            clusterNode.setLastSeenAlive(new Date());

            String type = (String) eventData.get("type");
            if (StringUtils.isEmpty(type)) {
                logger.warn("Event received without 'type' property. Ignoring it.");
                return;
            }

            if ("process-engine-lifecycle".equals(type)) {
                handleProcessEngineLifecycleData(eventData, cluster, clusterNode);
            } else if ("jvm-metrics".equals(type)) {
                handleJvmMetricsData(eventData, clusterNode);
            } else if ("process-definition-cache-metrics".equals(type)) {
                handleProcessDefinitionCacheMetricsData(eventData, clusterNode);
            } else if ("runtime-metrics".equals(type)) {
                handleRuntimeMetricsData(eventData, clusterNode);
            } else if ("async-executor".equals(type)) {
                handleAsyncExecutorData(eventData, clusterNode);
            } else if ("job-failures".equals(type)) {
                handleJobFailuresData(eventData, clusterNode);
            } else if ("suite-version".equals(type)) {
                handleSuiteVersionData(eventData, clusterNode);
            } else if ("suite-rest-metrics".equals(type)) {
                handleSuiteRestMetricsData(eventData, clusterNode);
            } else if ("suite-elastic-search-stats".equals(type)) {
                handleElasticSearchData(eventData, clusterNode);
            } else if ("master-configuration".equals(type)) {
                handleMasterConfigurationData(eventData, cluster, clusterNode);
            } else {
                logger.warn("Unknown event type received on event queue : " + type);
            }

        }
    }

    protected void handleProcessEngineLifecycleData(Map<String, Object> eventData,
                                                    ActivitiCluster cluster, ActivitiClusterNode clusterNode) {

        String eventType = (String) eventData.get("state");

        // Change state accordingly
        if ("process-engine-booting".equals(eventType)) {
            clusterNode.setState(ActivitiClusterNode.STATE.BOOTING);
        } else if ("process-engine-ready".equals(eventType)) {
            clusterNode.setState(ActivitiClusterNode.STATE.RUNNING);
        } else if ("process-engine-closed".equals(eventType)) {
            clusterNode.setState(ActivitiClusterNode.STATE.CLOSED);
        }
    }

    protected void handleJvmMetricsData(Map<String, Object> eventData, ActivitiClusterNode clusterNode) {
        clusterNode.setJvmMetrics(eventData);
    }

    protected void handleProcessDefinitionCacheMetricsData(Map<String, Object> eventData, ActivitiClusterNode clusterNode) {
        clusterNode.setProcessDefinitionCacheMetrics(eventData);
    }

    protected void handleRuntimeMetricsData(Map<String, Object> eventData, ActivitiClusterNode clusterNode) {
        clusterNode.setRuntimeMetrics(eventData);
    }

    protected void handleAsyncExecutorData(Map<String, Object> eventData, ActivitiClusterNode clusterNode) {
        String state = (String) eventData.get("state");
        clusterNode.setAsyncExecutorRunning(state != null && "started".equals(state));
    }

    protected void handleJobFailuresData(Map<String, Object> eventData, ActivitiClusterNode clusterNode) {
        clusterNode.setJobFailures(eventData);
    }

    protected void handleSuiteVersionData(Map<String, Object> eventData, ActivitiClusterNode clusterNode) {
        Map<String, Object> versionInfo = (Map<String, Object>) eventData.get("versionInfo");
        if (versionInfo != null) {

            if (versionInfo.containsKey("majorVersion")) {
                clusterNode.setBpmSuiteMajorVersion((String) versionInfo.get("majorVersion"));
            }
            if (versionInfo.containsKey("minorVersion")) {
                clusterNode.setBpmSuiteMinorVersion((String) versionInfo.get("minorVersion"));
            }
            if (versionInfo.containsKey("revisionVersion")) {
                clusterNode.setBpmSuiteRevisionVersion((String) versionInfo.get("revisionVersion"));
            }
            if (versionInfo.containsKey("edition")) {
                clusterNode.setBpmSuiteEdition((String) versionInfo.get("edition"));
            }

        }
    }

    protected void handleSuiteRestMetricsData(Map<String, Object> eventData, ActivitiClusterNode clusterNode) {
        if (eventData.containsKey("metrics")) {
            Map<String, Object> metrics = (Map<String, Object>) eventData.get("metrics");
            if (metrics.containsKey("meters")) {
                clusterNode.setBpmSuiteRestMeters((Map<String, Object>) metrics.get("meters"));
            }
            if (metrics.containsKey("timers")) {
                clusterNode.setBpmSuiteRestTimers((Map<String, Object>) metrics.get("timers"));
            }
        }
    }

    protected void handleElasticSearchData(Map<String, Object> eventData, ActivitiClusterNode clusterNode) {
        clusterNode.setBpmSuiteElasticSearchStatsJson((String) eventData.get("stats"));
    }

    protected void handleMasterConfigurationData(Map<String, Object> eventData, ActivitiCluster activitiCluster, ActivitiClusterNode clusterNode) {
        if (eventData.containsKey("enabled")) {
            clusterNode.setUsingMasterConfiguration((Boolean) eventData.get("enabled"));
        } else {
            clusterNode.setUsingMasterConfiguration(true);
        }
        if (activitiCluster.getCurrentMasterConfigurationId() != null) {
            String masterConfigId = (String) eventData.get("id");
            clusterNode.setMasterConfigurationId((masterConfigId));
            clusterNode.setCorrectMasterConfiguration(activitiCluster.getCurrentMasterConfigurationId().equals(masterConfigId));
        } else {
            clusterNode.setCorrectMasterConfiguration(false);
        }
    }

	public Map<String, ActivitiCluster> getClusters() {
		return clusters;
	}

	public ActivitiCluster getCluster(String clusterName) {
		return clusters.get(clusterName);
	}

	public List<ActivitiClusterNode> getClusterNodes(Long clusterId) {
        ClusterConfig clusterConfig = clusterConfigRepository.findOne(clusterId);
        if (clusterConfig != null) {
            ActivitiCluster cluster = getCluster(clusterConfig.getClusterName());
            if (cluster != null) {
                return new ArrayList<ActivitiClusterNode>(cluster.getClusterNodes().values());
            }
            return null;
        }
        return new ArrayList<ActivitiClusterNode>();
	}

    /**
     * Periodically checks for cluster nodes that are down and removes those from the list.
     */
    @Async
    @Scheduled(cron="${cluster.monitoring.inactive.check.cronexpression:0 0/5 * * * ?}")
    protected void verifyClusterNodes() {
        for (ActivitiCluster cluster : clusters.values()) {
            Iterator<Map.Entry<String, ActivitiClusterNode>> nodeIterator = cluster.getClusterNodes().entrySet().iterator();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, ActivitiClusterNode> entry = nodeIterator.next();
                Date lastSeenAlive = entry.getValue().getLastSeenAlive();
                Date now = new Date();
                if (lastSeenAlive != null && (now.getTime() - lastSeenAlive.getTime() > maxInactiveTimeInMs)) {
                    logger.info("Node " + entry.getValue().getNodeId() + " was not active for at least "
                        + maxInactiveTimeInMs + " ms. Removing it from the list");
                    nodeIterator.remove();
                }
            }
        }
    }

}
