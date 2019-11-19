/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get the login of the current user.
     */
    public static String getCurrentLogin() {
        return getCurrentUserDetails().getUsername();
    }
    
    public static boolean isCurrentUserAnAdmin() {
        for (GrantedAuthority authority: getCurrentUserDetails().getAuthorities()) {
            if (AuthoritiesConstants.ADMIN.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
    
    private static UserDetails getCurrentUserDetails() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return (UserDetails) securityContext.getAuthentication().getPrincipal();
    }
    
    public static String getCleanedClrfSequencesString(String crlfSequencesString) {
        if (crlfSequencesString == null || crlfSequencesString.equals("")) {
            return "";
        }
        return crlfSequencesString.replace('\n', '_').replace('\r', '_');
        
    }
}
