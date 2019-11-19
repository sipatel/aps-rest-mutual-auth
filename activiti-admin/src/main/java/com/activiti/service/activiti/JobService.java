/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class JobService {

	private final Logger log = LoggerFactory.getLogger(JobService.class);

	@Autowired
    protected ActivitiClientService clientUtil;

	public JsonNode listJobs(ServerConfig serverConfig, Map<String, String[]> parameterMap) {

		URIBuilder builder = null;
		try {
			builder = new URIBuilder("management/jobs");
		} catch (Exception e) {
			log.error("Error building uri", e);
			throw new ActivitiServiceException("Error building uri", e);
		}

		for (String name : parameterMap.keySet()) {
			builder.addParameter(name, parameterMap.get(name)[0]);
		}
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder.toString()));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode getJob(ServerConfig serverConfig, String jobId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "management/jobs/" + jobId));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public String getJobStacktrace(ServerConfig serverConfig, String jobId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "management/jobs/" + jobId + "/exception-stacktrace"));
		return clientUtil.executeRequestAsString(get, serverConfig, HttpStatus.SC_OK);
  }

	public void executeJob(ServerConfig serverConfig, String jobId) {
		HttpPost post = clientUtil.createPost("management/jobs/" + jobId, serverConfig);
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("action", "execute");
		post.setEntity(clientUtil.createStringEntity(node));

		clientUtil.executeRequestNoResponseBody(post, serverConfig, HttpStatus.SC_NO_CONTENT);
	}

	public void deleteJob(ServerConfig serverConfig, String jobId) {
		HttpDelete post = new HttpDelete(clientUtil.getServerUrl(serverConfig, "management/jobs/" + jobId));
		clientUtil.executeRequestNoResponseBody(post, serverConfig, HttpStatus.SC_NO_CONTENT);
	}
}
