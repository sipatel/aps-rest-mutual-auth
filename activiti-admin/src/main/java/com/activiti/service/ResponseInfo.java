/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
public class ResponseInfo {

    protected int statusCode;
    protected JsonNode content;
    
    public ResponseInfo(int statusCode) {
        this(statusCode, null);
    }
    
    public ResponseInfo(int statusCode, JsonNode content) {
        this.statusCode = statusCode;
        this.content = content;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public JsonNode getContent() {
        return content;
    }
    
    public boolean isSuccess() {
        return statusCode == HttpStatus.SC_OK;
    }
    
}
