/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti.cluster;

import com.activiti.domain.ClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a cluster of Activiti instances.
 *
 * @author jbarrez
 */
public class ActivitiCluster {

	private final Logger logger = LoggerFactory.getLogger(ActivitiCluster.class);

    protected ClusterConfig clusterConfig;

    protected String currentMasterConfigurationId;

	/**
     * A mapping of all members of this cluster.
     * Each member of the cluster has a unique id.
     */
	protected Map<String, ActivitiClusterNode> clusterNodes =
			Collections.synchronizedMap(new HashMap<String, ActivitiClusterNode>());

	/* Instance members */

	protected boolean isShutdown;


	public ActivitiCluster(ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
	}

    public ActivitiClusterNode addNode(String nodeId, String ipAddress) {
        ActivitiClusterNode node = new ActivitiClusterNode(nodeId, ipAddress);
        node.setLastSeenAlive(new Date());
        clusterNodes.put(nodeId, node);
        return node;
    }

    public void nodeRemoved(String nodeId) {
        if (clusterNodes.containsKey(nodeId)) {
            clusterNodes.remove(nodeId);
        }
    }

	/* GETTERS */

	public Map<String, ActivitiClusterNode> getClusterNodes() {
		return clusterNodes;
	}

	public boolean isShutDown() {
		return isShutdown;
	}

    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    public String getCurrentMasterConfigurationId() {
        return currentMasterConfigurationId;
    }

    public void setCurrentMasterConfigurationId(String currentMasterConfigurationId) {
        this.currentMasterConfigurationId = currentMasterConfigurationId;
    }
}
