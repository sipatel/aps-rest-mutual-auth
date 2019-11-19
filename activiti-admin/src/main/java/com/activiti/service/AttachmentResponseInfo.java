/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
public class AttachmentResponseInfo extends ResponseInfo {

    protected String fileName;
    protected byte[] bytes;
    
    public AttachmentResponseInfo(String fileName, byte[] bytes) {
        super(200);
        this.fileName = fileName;
        this.bytes = bytes;
    }
    
    public AttachmentResponseInfo(int statusCode, JsonNode content) {
        super(statusCode, content);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }
    
}
