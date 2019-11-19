/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import com.activiti.security.SecurityUtils;
import com.activiti.service.UserService;
import com.activiti.web.rest.dto.AccountRepresentation;
import com.activiti.web.rest.dto.UserRepresentation;
import com.activiti.web.rest.exception.NotPermittedException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Autowired
    protected UserService userService;

    @Autowired
    protected ObjectMapper objectMapper;
    
    @Timed
    @RequestMapping(value = "/rest/authenticate", method = RequestMethod.GET, produces = {"application/json"})
    public ObjectNode isAuthenticated(HttpServletRequest request) {
        String user = request.getRemoteUser();

        if(user == null) {
            throw new NotPermittedException("Request did not contain valid authorization");
        }

        ObjectNode result = objectMapper.createObjectNode();
        result.put("login", user);
        return result;
    }

    /**
     * GET  /rest/account -> get the current user.
     */
    @RequestMapping(value = "/rest/account",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public UserRepresentation getAccount(HttpServletResponse response) {
        AccountRepresentation account = userService.getAccountRepresentation(SecurityUtils.getCurrentLogin());
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return account;
    }
    
}
