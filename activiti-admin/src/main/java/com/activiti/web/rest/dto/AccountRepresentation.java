/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.dto;

import com.activiti.domain.User;

/**
 * @author Bassam Al-Sarori
 */
public class AccountRepresentation extends UserRepresentation {

    protected boolean multiTenant;
    
    public AccountRepresentation(User user, boolean multiTenant) {
        super(user);
        this.multiTenant = multiTenant;
    }

    public boolean isMultiTenant() {
        return multiTenant;
    }

    public void setMultiTenant(boolean multiTenant) {
        this.multiTenant = multiTenant;
    }
    
    
}
