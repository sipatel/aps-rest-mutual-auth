/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.representation.MasterConfigurationRepresentation;
import com.activiti.service.activiti.MasterConfigurationService;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.NotFoundException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author jbarrez
 */
@RestController
@RequestMapping("/rest/activiti/master-configs")
public class MasterConfigurationsResource {

    private final Logger log = LoggerFactory.getLogger(MasterConfigurationsResource.class);

    @Autowired
    private MasterConfigurationService masterConfigurationService;

    @Timed
    @RequestMapping(method=RequestMethod.GET, produces="application/json")
    public MasterConfigurationRepresentation getMasterConfig(@RequestParam(value = "clusterConfigId", required = true) Long clusterConfigId) {
        MasterConfigurationRepresentation masterConfigurationRepresentation = masterConfigurationService.findByClusterConfigId(clusterConfigId);
        if (masterConfigurationRepresentation != null) {
            return masterConfigurationRepresentation;
        } else {
            throw new NotFoundException();
        }
    }

    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method=RequestMethod.POST, produces="application/json")
  	public void createMasterConfiguration(@RequestBody ObjectNode json)  {
        storeMasterConfig(json);
    }

    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method=RequestMethod.PUT, produces="application/json")
    public void updateMasterConfiguration(@RequestBody ObjectNode json)  {
        storeMasterConfig(json);
    }

    @Timed
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method=RequestMethod.DELETE, value="/{clusterConfigId}", produces="application/json")
  	public void deleteMasterConfig(@PathVariable Long clusterConfigId)  {
        masterConfigurationService.deleteMasterConfigurationForCluster(clusterConfigId);
    }

    protected void storeMasterConfig(ObjectNode json) {
        JsonNode clusterConfigIdNode = json.get("clusterConfigId");
        Long clusterConfigId = null;
        if (clusterConfigIdNode != null) {
            clusterConfigId = clusterConfigIdNode.asLong();
        } else {
            throw new BadRequestException("Missing 'clusterConfigId' property");
        }

        JsonNode typeNode = json.get("type");
        JsonNode configNode = json.get("config");
        if (typeNode != null && configNode != null) {
            String type = typeNode.asText();

            if (type.equals("bpmSuite")) {

                masterConfigurationService.storeBpmSuiteMasterConfiguration(clusterConfigId, configNode.asText());

            } else if (type.equals("engine")) {

                masterConfigurationService.storeProcessEngineMasterConfiguration(clusterConfigId, configNode);

            }

        } else {
            throw new BadRequestException("Missing 'type' or 'config' property");
        }
    }

}
