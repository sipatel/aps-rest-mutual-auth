/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.license;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LicenseStatus {

    // Admin app license status codes
    public static final String VALID = "license-valid";
    public static final String INVALID = "license-invalid";
    public static final String ENDPOINT_ERROR = "endpoint-error";
    public static final String NOT_FOUND = "license-not-found";
    public static final String INVALID_DATE = "license-invalid-date";

    public static Map<Long, String> serverLicenseValidityMap =
            Collections.synchronizedMap(new HashMap<Long, String>());
}
