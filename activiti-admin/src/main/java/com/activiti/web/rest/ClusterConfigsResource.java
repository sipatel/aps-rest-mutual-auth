/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ClusterConfig;
import com.activiti.repository.ClusterConfigRepository;
import com.activiti.repository.UserRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.activiti.ClusterConfigService;
import com.activiti.web.rest.dto.ClusterConfigRepresentation;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.ConflictException;
import com.codahale.metrics.annotation.Timed;

/**
 * @author jbarrez
 */
@RestController
@RequestMapping("/rest/cluster-configs")
public class ClusterConfigsResource {

    @Autowired
    protected ClusterConfigRepository clusterConfigRepository;

    @Autowired
    protected ClusterConfigService clusterConfigService;
    
    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected Environment env;

    @Timed
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<ClusterConfigRepresentation> listClusterConfigs() {
        if (isMultiTenantEnabled()) {
            return clusterConfigService.findUserClusters(SecurityUtils.getCurrentLogin());
        } else {
            return clusterConfigService.findAll();
        }
    }

    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method=RequestMethod.POST, produces="application/json")
  	public ClusterConfigRepresentation createClusterConfig(@RequestBody ClusterConfigRepresentation clusterConfigRepresentation)  {

        // Check uniqueness of cluster name
        // Check uniquess of user name
        
        if (userRepository.findOne(clusterConfigRepresentation.getClusterUserName()) != null) {
            throw new ConflictException("clusterUser");
        }
        
        if (clusterConfigRepository.countByClusterName(clusterConfigRepresentation.getClusterName()) > 0) {
            throw new ConflictException("clusterName");
        }
    
        return clusterConfigService.createNewClusterConfig(
            SecurityUtils.getCleanedClrfSequencesString(clusterConfigRepresentation.getClusterName()),
            clusterConfigRepresentation.getClusterUserName(),
            clusterConfigRepresentation.getClusterPassword(),
            clusterConfigRepresentation.getRequiresMasterConfiguration(),
            clusterConfigRepresentation.getMetricSendingInterval(),
            SecurityUtils.getCurrentLogin()
        );
    }

//    Disabled updating cluster config for now
//
//    @Timed
//    @RequestMapping(method=RequestMethod.PUT, value="/{clusterConfigId}", produces="application/json")
//  	public void updateConfig(@PathVariable Long clusterConfigId, @RequestBody ClusterConfig clusterConfig)  {
//    	if(clusterConfigRepository.findOne(clusterConfigId) != null) {
//    		clusterConfigService.updateClusterConfig(clusterConfigId, clusterConfig);
//    	} else {
//    		throw new BadRequestException("No cluster config found with id: " + clusterConfigId);
//    	}
//    }

    @Timed
    @RequestMapping(method=RequestMethod.DELETE, value="/{clusterConfigId}", produces="application/json")
    @ResponseStatus(value = HttpStatus.OK)
  	public void deleteConfig(@PathVariable Long clusterConfigId)  {
    	ClusterConfig clusterConfig = clusterConfigRepository.findOne(clusterConfigId);
    	if(clusterConfig != null) {
    		clusterConfigService.deleteClusterConfig(clusterConfigId);
    	} else {
    		throw new BadRequestException("No cluster config found with id: " + clusterConfigId);
    	}
    }

    protected boolean isMultiTenantEnabled() {
        return env.getProperty("multi-tenant.enabled", Boolean.class, false);
    }

}
