/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.api;

import com.activiti.domain.ClusterConfig;
import com.activiti.domain.User;
import com.activiti.security.SecurityUtils;
import com.activiti.service.activiti.ClusterConfigService;
import com.activiti.service.activiti.cluster.ActivitiClusterService;
import com.activiti.web.rest.exception.NotFoundException;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoint for other services to post events to
 *
 * @author Joram Barrez
 */
@RestController
public class EventsResource {

    private final static Logger logger = LoggerFactory.getLogger(EventsResource.class);

    @Autowired
    private ClusterConfigService clusterConfigService;

    @Autowired
    private ActivitiClusterService activitiClusterService;

    @Timed
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/enterprise/{clusterName}/events", method = RequestMethod.POST, produces = "application/json")
    public void receiveEvent(@PathVariable String clusterName, @RequestBody List<Map<String, Object>> events) {

        if (StringUtils.isNotBlank(clusterName)) {

            ClusterConfig clusterConfig = clusterConfigService.findClusterConfigByName(clusterName);

            // First, verify if the current user is allowed to post events for this cluster
            String currentLogin = SecurityUtils.getCurrentLogin();
            User clusterConfigUser = clusterConfig.getUser();
            if (clusterConfigUser == null || !currentLogin.equals(clusterConfigUser.getLogin())) {
                throw new NotFoundException(); // Send a 404. No need to tell that the cluster config exists
            }

            // Post the events
            if (events != null && !events.isEmpty()) {
                for (Map<String, Object> event : events) {
                    activitiClusterService.eventReceived(clusterName, event);
                }
            } else {
                logger.warn("Invalid cluster name");
            }

        } else {
            logger.warn("Received an empty event");
        }


    }
}
