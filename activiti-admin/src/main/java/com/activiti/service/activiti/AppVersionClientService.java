/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppVersionClientService extends AbstractEncryptingService {

    public static final String TYPE_BPM_SUITE = "bpmSuite";

	@Autowired
    protected ActivitiClientService clientUtil;

    public String getEndpointTypeUsingEncryptedPassword(String contextRoot, String restRoot,
                                  String serverAddress, Integer port,
                                  String userName, String encryptedPassword) {
        String decryptedPassword = decrypt(encryptedPassword);
        return getEndpointType(contextRoot, restRoot, serverAddress, port, userName, decryptedPassword);
    }

    public String getEndpointType(String contextRoot, String restRoot,
                                  String serverAddress, Integer port,
                                  String userName, String password) {
        String result = null;
        HttpGet get = new HttpGet(clientUtil.getServerUrl(contextRoot, restRoot, serverAddress, port, null, "enterprise/app-version"));

        JsonNode jsonNode = clientUtil.executeRequest(get, userName, password);

        if (jsonNode != null && jsonNode.has("type")) {
            result = jsonNode.get("type").asText();
        }

        return result;
    }
}
