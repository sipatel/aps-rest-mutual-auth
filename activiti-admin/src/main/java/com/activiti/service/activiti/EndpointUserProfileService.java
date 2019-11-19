/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class EndpointUserProfileService extends AbstractEncryptingService {

	@Autowired
    protected ActivitiClientService clientUtil;

    public String getEndpointUserTenantIdUsingEncryptedPassword(String contextRoot, String restRoot,
                                          String serverAddress, Integer port,
                                          String userName, String encryptedPassword) {
        String decryptedPassword = decrypt(encryptedPassword);
        return getEndpointUserTenantId(contextRoot, restRoot, serverAddress, port, userName, decryptedPassword);
    }

    public String getEndpointUserTenantId(String contextRoot, String restRoot,
                                          String serverAddress, Integer port,
                                          String userName, String password) {
        JsonNode jsonNode = getEndpointUserProfile(contextRoot, restRoot, serverAddress, port, userName, password);
        if (jsonNode.has("tenantId") && jsonNode.get("tenantId").isNull() == false) {
            JsonNode tenantIdNode = jsonNode.get("tenantId");
            return tenantIdNode.asText();
        }
        return null;
    }

	public JsonNode getEndpointUserProfile(String contextRoot, String restRoot,
                                           String serverAddress, Integer port,
                                           String userName, String password) {

		HttpGet get = new HttpGet(clientUtil.getServerUrl(contextRoot, restRoot, serverAddress, port, null, "enterprise/profile"));
		return clientUtil.executeRequest(get, userName, password);
	}

}
