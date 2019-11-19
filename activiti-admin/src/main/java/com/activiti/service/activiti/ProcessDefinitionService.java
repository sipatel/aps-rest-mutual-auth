/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class ProcessDefinitionService {

	private final Logger log = LoggerFactory.getLogger(ProcessDefinitionService.class);

	@Autowired
    protected ActivitiClientService clientUtil;

	@Autowired
	protected ObjectMapper objectMapper;

	public JsonNode listProcesDefinitions(ServerConfig serverConfig,
			Map<String, String[]> parameterMap, boolean latest) {

		URIBuilder builder = null;
		try {
			builder = new URIBuilder("repository/process-definitions");
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

	public JsonNode getProcessDefinition(ServerConfig serverConfig, String definitionId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "repository/process-definitions/" + definitionId));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode updateProcessDefinitionCategory(ServerConfig serverConfig, String definitionId, String category) {
		ObjectNode updateCall = objectMapper.createObjectNode();
		updateCall.put("category", category);

		URIBuilder builder = clientUtil.createUriBuilder("repository/process-definitions/" + definitionId);

		HttpPut put = clientUtil.createPut(builder, serverConfig);
		put.setEntity(clientUtil.createStringEntity(updateCall));

		return clientUtil.executeRequest(put, serverConfig);
	}

	public BpmnModel getProcessDefinitionModel(ServerConfig serverConfig, String definitionId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "repository/process-definitions/" + definitionId + "/resourcedata"));
		return executeRequestForXML(get, serverConfig, HttpStatus.SC_OK);
	}

	protected BpmnModel executeRequestForXML(HttpUriRequest request, ServerConfig serverConfig, int expectedStatusCode) {

		ActivitiServiceException exception = null;
		CloseableHttpClient client = clientUtil.getHttpClient(serverConfig);
		try {
			CloseableHttpResponse response = client.execute(request, clientUtil.createHttpClientContext(request));
			boolean success = response.getStatusLine() != null && response.getStatusLine().getStatusCode() == expectedStatusCode;
			if (success) {
			    try {
			        InputStream responseContent = response.getEntity().getContent();
	                XMLInputFactory xif = XMLInputFactory.newInstance();
	                InputStreamReader in = new InputStreamReader(responseContent, "UTF-8");
	                XMLStreamReader xtr = xif.createXMLStreamReader(in);
	                BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
	                return bpmnModel;
	            } catch (Exception e) {
	                log.warn("Error consuming response from uri " + request.getURI(), e);
	                exception = clientUtil.wrapException(e, request);
	            } finally {
	                response.close();
	            }
                
            } else {
                exception = new ActivitiServiceException("An error occured while calling Activiti: " + response.getStatusLine());
            }

		} catch (Exception e) {
			log.error("Error executing request to uri " + request.getURI(), e);
			exception = clientUtil.wrapException(e, request);

		} finally {
			try {
				client.close();
			} catch (Exception e) {
				log.warn("Error closing http client instance", e);
			}
		}

		if (exception != null) {
			throw exception;
		}

		return null;
	}

}
