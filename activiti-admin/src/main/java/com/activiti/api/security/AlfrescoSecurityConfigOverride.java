/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.api.security;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public interface AlfrescoSecurityConfigOverride {

    void configureGlobal(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService);
}
