/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.User;
import com.activiti.security.SecurityUtils;
import com.activiti.service.UserService;
import com.activiti.web.rest.dto.UserRepresentation;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.ConflictException;
import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing users.
 */
@RestController
public class UsersResource {

    private final Logger log = LoggerFactory.getLogger(UsersResource.class);

    @Autowired
    protected UserService userService;
    
    @Autowired
    protected Environment env;

    /**
     * GET  /rest/users -> get a list of users.
     */
    @RequestMapping(value = "/rest/users",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public List<UserRepresentation> getUsers() {
        if (isMultiTenantEnabled() && !SecurityUtils.isCurrentUserAnAdmin()) {
            // return cluster users created by current user
            return userService.getClusterUsers(SecurityUtils.getCurrentLogin());
        }
        return userService.getAllUsers();
    }
    
    /**
     * POST  /rest/users -> create a new user.
     */
    @RequestMapping(value = "/rest/users", method = RequestMethod.POST)
    @Timed
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void createUser(@RequestBody UserRepresentation userRepresentation) {
        log.debug("REST request to create a User : {}");
        
        if (isMultiTenantEnabled()) {
            if (!SecurityUtils.isCurrentUserAnAdmin()) {
                throw new BadRequestException("Only users with admin rights can create new users.");
            }
        } else {
            // in non multi tenant mode all users are admins
            userRepresentation.setIsAdmin(true);
        }
        
        if (userRepresentation.getLogin() == null) {
            throw new BadRequestException("user login is required");
        }
        
        if (userRepresentation.getPassword() == null) {
            throw new BadRequestException("a password is required");
        }
        
        try {
            User result = userService.createUser(userRepresentation);
            if(result == null) {
                throw new ConflictException("User with login '" + userRepresentation.getLogin() + "' already exists.");
            }
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (ConstraintViolationException cv) {
            String message = "Invalid user details";
            if (cv.getConstraintViolations().size() > 0) {
                message = cv.getConstraintViolations().iterator().next().getMessage();
            }
            throw new BadRequestException(message);
        }
        
    }
    
    protected boolean isMultiTenantEnabled() {
        return env.getProperty("multi-tenant.enabled", Boolean.class, false);
    }
}
