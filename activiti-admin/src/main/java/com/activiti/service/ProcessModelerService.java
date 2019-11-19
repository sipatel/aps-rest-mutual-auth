/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service;

import java.io.InputStream;

import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author jbarrez
 */
@Service
public class ProcessModelerService {
    
    private final static Logger log = LoggerFactory.getLogger(ProcessModelerService.class);
	
	@Autowired
	protected ConfigurationService configurationService;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	public CloseableHttpClient createClient(String username, String password) {
	    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            log.warn("Could not configure HTTP client to use SSL" , e);
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        if (sslsf != null) {
            httpClientBuilder.setSSLSocketFactory(sslsf);
        }

        return httpClientBuilder.build();
	}
	
	public JsonNode getProcesses(CloseableHttpClient httpClient) throws Exception {
	    String url = configurationService.getModelerUrl() + "enterprise/models";
		HttpGet httpGet = new HttpGet(url);
		try {
    		InputStream responseContent = executeRequest(httpGet, httpClient);
    		if (responseContent != null) {
    		    return objectMapper.readTree(responseContent);
    		} else {
    		    throw new ActivitiServiceException("Could not retrieve processes");
    		}
    		
		} finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.warn("Error closing http client instance", e);
            }
        }
    }
	
	public InputStream getThumbnail(CloseableHttpClient httpClient, Long processModelId) throws Exception {
	    String url = configurationService.getModelerUrl() + "enterprise/models/" + processModelId + "/thumbnail";
		HttpGet httpGet = new HttpGet(url);
		return executeRequest(httpGet, httpClient);
    }
	
	public InputStream getBpmn20Xml(CloseableHttpClient httpClient, Long processModelId) throws Exception {
		String url = configurationService.getModelerUrl() + "enterprise/models/" + processModelId + "/bpmn20";
		HttpGet httpGet = new HttpGet(url);
		return executeRequest(httpGet, httpClient);
	}
	
	protected InputStream executeRequest(HttpUriRequest request, CloseableHttpClient httpClient) {
	    ActivitiServiceException exception = null;
	    try {
            CloseableHttpResponse response = httpClient.execute(request);

            try {
                InputStream responseContent = response.getEntity().getContent();
                boolean success = response.getStatusLine() != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
                if (success) {
                    return responseContent;

                } else {
                    log.error("Expected response " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                }
                
            } catch (Exception e) {
                log.warn("Error consuming response from uri " + request.getURI(), e);
                exception = wrapException(e, request);
            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("Error executing request to uri " + request.getURI(), e);
            exception = wrapException(e, request);
        }
	    
	    if (exception != null) {
            throw exception;
        }

        return null;
	}
	
	public ActivitiServiceException wrapException(Exception e, HttpUriRequest request) {
        if (e instanceof HttpHostConnectException) {
            return new ActivitiServiceException("Unable to connect to the Activiti server.");
        } else if (e instanceof ConnectTimeoutException) {
            return new ActivitiServiceException("Connection to the Activiti server timed out.");
        } else {
            // Use the raw exception message
            return new ActivitiServiceException(e.getClass().getName() + ": " + e.getMessage());
        }
    }

}
