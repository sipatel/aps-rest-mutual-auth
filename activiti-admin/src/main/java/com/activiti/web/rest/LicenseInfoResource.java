/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.license.LicenseInfo;
import com.activiti.license.LicenseService;
import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for license info.
 */
@RestController
public class LicenseInfoResource {

    private final Logger log = LoggerFactory.getLogger(LicenseInfoResource.class);

    @Autowired
    private LicenseService licenseService;

    /**
     * GET  /rest/license-info -> get the license information.
     */
    @RequestMapping(value = "/rest/license-info",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public LicenseInfo getLicenseInfo(HttpServletResponse response) {
        log.debug("REST request to get license information");
        return licenseService.getLicenseInfo();
    }
}
