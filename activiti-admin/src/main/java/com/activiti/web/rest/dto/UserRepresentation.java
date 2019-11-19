/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.dto;

import com.activiti.domain.Authority;
import com.activiti.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class UserRepresentation {
    protected String login;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected Boolean adminUser;
    protected Boolean clusterUser;
    protected Boolean readOnlyUser;
    
    public UserRepresentation() {
    }
    
    public UserRepresentation(User user) {
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.readOnlyUser = user.getReadOnlyUser();
        
        if (user.getAuthorities() != null) {
            for (Authority authority : user.getAuthorities()) {
                if (Authority.ROLE_ADMIN.equals(authority.getName())) {
                    this.adminUser = true;
                } else if (Authority.ROLE_CLUSTER_MANAGER.equals(authority.getName())) {
                    this.clusterUser = true;
                }
            }
        }
    }
    
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    @JsonInclude(Include.NON_NULL)
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @JsonInclude(Include.NON_NULL)
    public Boolean getAdminUser() {
        return adminUser;
    }
    
    public void setIsAdmin(Boolean adminUser) {
        this.adminUser = adminUser;
    }
    
    @JsonInclude(Include.NON_NULL)
    public Boolean getClusterUser() {
        return clusterUser;
    }
    
    public void setClusterUser(Boolean clusterUser) {
        this.clusterUser = clusterUser;
    }
    
    @JsonInclude(Include.NON_NULL)
    public Boolean getReadOnlyUser() {
        return readOnlyUser;
    }
    
    public void setReadOnlyUser(Boolean readOnlyUser) {
        this.readOnlyUser = readOnlyUser;
    }
}
