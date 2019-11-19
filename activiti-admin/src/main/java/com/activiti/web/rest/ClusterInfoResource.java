/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.service.activiti.cluster.ActivitiClusterNode;
import com.activiti.service.activiti.cluster.ActivitiClusterService;
import com.codahale.metrics.annotation.Timed;

/**
 * @author jbarrez
 */
@RestController
public class ClusterInfoResource {

    private final Logger log = LoggerFactory.getLogger(ClusterInfoResource.class);

    @Autowired
    protected ActivitiClusterService activitiClusterService;

    @Timed
    @RequestMapping(value = "/rest/activiti/cluster-info/{clusterConfigId}",
            method = RequestMethod.GET,
            produces = "application/json")
    public @ResponseBody List<ActivitiClusterNode> getClusterInfo(@PathVariable Long clusterConfigId) {
        return activitiClusterService.getClusterNodes(clusterConfigId);
    }

}
