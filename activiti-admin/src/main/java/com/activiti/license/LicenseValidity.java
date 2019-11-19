/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.license;


public class LicenseValidity {
    
	protected String adminLicenseStatus;
	protected String serverLicenseStatus;
	protected String description;
	
    public String getAdminLicenseStatus() {
        return adminLicenseStatus;
    }
    public void setAdminLicenseStatus(String adminLicenseStatus) {
        this.adminLicenseStatus = adminLicenseStatus;
    }
    public String getServerLicenseStatus() {
        return serverLicenseStatus;
    }
    public void setServerLicenseStatus(String serverLicenseStatus) {
        this.serverLicenseStatus = serverLicenseStatus;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
