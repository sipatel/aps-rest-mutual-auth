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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.license.LicenseStatus;
import com.activiti.license.LicenseValidity;
import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for license validation.
 */
@RestController
public class LicenseValidityResource {

    private final Logger log = LoggerFactory.getLogger(LicenseValidityResource.class);

    /**
     * GET  /rest/license-validity -> get the license validity info.
     */
    @RequestMapping(value = "/rest/license-validity",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public LicenseValidity getLicenseValidity(@RequestParam Long serverConfigId, HttpServletResponse response) {
        LicenseValidity licenseValidity = new LicenseValidity();
        String serverLicenseStatus = LicenseStatus.serverLicenseValidityMap.get(serverConfigId);
        licenseValidity.setServerLicenseStatus(serverLicenseStatus);
        return licenseValidity;
    }
}
