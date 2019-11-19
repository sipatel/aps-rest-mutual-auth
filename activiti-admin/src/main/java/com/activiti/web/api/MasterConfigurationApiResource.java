/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.api;

import com.activiti.domain.ClusterConfig;
import com.activiti.domain.User;
import com.activiti.domain.representation.MasterConfigurationRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.activiti.ClusterConfigService;
import com.activiti.service.activiti.MasterConfigurationService;
import com.activiti.web.rest.exception.NotFoundException;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Joram Barrez
 */
@RestController
public class MasterConfigurationApiResource {

    private final static Logger logger = LoggerFactory.getLogger(MasterConfigurationApiResource.class);

    @Autowired
    private ClusterConfigService clusterConfigService;

    @Autowired
    private MasterConfigurationService masterConfigurationService;

    @Timed
    @RequestMapping(value = "/enterprise/{clusterName}/master-config", method = RequestMethod.GET, produces = "application/json")
    public MasterConfigurationRepresentation getClusterConfig(@PathVariable String clusterName) {

        if (StringUtils.isNotBlank(clusterName)) {

            ClusterConfig clusterConfig = clusterConfigService.findClusterConfigByName(clusterName);

            // First, verify if the current user is allowed to get that config
            String currentLogin = SecurityUtils.getCurrentLogin();
            User clusterConfigUser = clusterConfig.getUser();
            if (clusterConfigUser == null || !currentLogin.equals(clusterConfigUser.getLogin())) {
                throw new NotFoundException(); // Send a 404. No need to tell that the cluster config exists
            }

            if (clusterConfig != null) {
                MasterConfigurationRepresentation masterConfiguration = masterConfigurationService.findByClusterConfigId(clusterConfig.getId());
                if (masterConfiguration != null) {
                    return masterConfiguration;
                }
            }
        }

        // Simply throw a 404
        throw new NotFoundException();
    }

}
