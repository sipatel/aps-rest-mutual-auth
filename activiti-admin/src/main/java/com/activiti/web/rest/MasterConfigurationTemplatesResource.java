/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import com.activiti.service.activiti.MasterConfigurationService;
import com.activiti.web.rest.exception.NotFoundException;
import com.codahale.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author jbarrez
 */
@RestController
@RequestMapping("/rest/activiti/master-config-templates")
public class MasterConfigurationTemplatesResource {

    @Autowired
    private MasterConfigurationService masterConfigurationService;

    @Timed
    @RequestMapping(method = RequestMethod.GET, produces = "plain/text")
    public String getMasterConfigurationTemplate(@RequestParam(value = "type", required = true) String type) {
        // Only supporting BPM Suite at the moment
        if ("suite".equals(type)) {
            return masterConfigurationService.getBpmSuiteTemplate();
        } else {
            throw new NotFoundException();
        }
    }

}
