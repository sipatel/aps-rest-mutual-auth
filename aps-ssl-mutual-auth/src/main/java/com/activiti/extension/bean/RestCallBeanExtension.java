package com.activiti.extension.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateHelper;
import org.activiti.engine.delegate.Expression;
import org.apache.http.HttpResponse;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.activiti.domain.idm.EndpointConfiguration;
import com.activiti.model.editor.kickstart.TypedValueObject;
import com.activiti.runtime.activiti.bean.RestCallBean;
import com.fasterxml.jackson.databind.JsonNode;


@Component("custom_activiti_restCallDelegate")
public class RestCallBeanExtension extends RestCallBean{	

	File jarPath = new File(RestCallBeanExtension.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	String propertiesFilePath = jarPath.getParentFile().getAbsolutePath() + File.separator +"keystore.properties";
    String whitelistedDomainsConfFilePath = jarPath.getParentFile().getAbsolutePath()+File.separator+"whitelisted-domains.conf";
	private Properties prop = null;

	private static final Logger logger = LoggerFactory.getLogger(RestCallBeanExtension.class);
	
	/*public RestCallBeanExtension() throws FileNotFoundException, IOException {
		prop = new Properties();
		prop.load(new FileInputStream(propertiesFilePath));
	}*/

	private List<TypedValueObject> createRequestValues(Map<String, Object> variables)
	{
		List result = new ArrayList();

		for (String nextKey : variables.keySet()) {
			Object variable = variables.get(nextKey);

			String stringVar = null;
			String varType = null;

			if (variable != null)
			{
				stringVar = variable.toString();

				Class varClass = variable.getClass();

				if (Boolean.class.isAssignableFrom(varClass)) { varType = "boolean";
				} else if (String.class.isAssignableFrom(varClass)) { varType = "string";
				} else if (Number.class.isAssignableFrom(varClass)) { varType = "long";
				} else if (Date.class.isAssignableFrom(varClass)) {
					varType = "date";
					stringVar = (String)convertValue(variable, "String");
				}
			}

			TypedValueObject typedValueObject = new TypedValueObject(nextKey, nextKey, varType, stringVar);
			result.add(typedValueObject);
		}

		return result;
	}

	public void execute(DelegateExecution execution)
			throws Exception {
		
		prop = new Properties();
		prop.load(new FileInputStream(propertiesFilePath));

		logger.debug("started Custom REST call delegate");

		Expression baseEndpointExpression = DelegateHelper.getFieldExpression(execution, "baseEndpoint");
		Expression baseEndpointNameExpression = DelegateHelper.getFieldExpression(execution, "baseEndpointName");
		Expression restUrlExpression = DelegateHelper.getFieldExpression(execution, "restUrl");
		Expression httpMethodExpression = DelegateHelper.getFieldExpression(execution, "httpMethod");
		Expression requestMappingJSONTemplate = DelegateHelper.getFieldExpression(execution, "requestMappingJSONTemplate");

		if ((baseEndpointExpression == null) && (restUrlExpression == null)) {
			throw new IllegalArgumentException("A base endpoint or REST URL is required");
		}

		String baseEndpointIdValue = null;
		if (baseEndpointExpression != null) {
			baseEndpointIdValue = getExpressionAsString(baseEndpointExpression, execution);
		}

		String baseEndpointNameValue = null;
		if (baseEndpointNameExpression != null) {
			baseEndpointNameValue = getExpressionAsString(baseEndpointNameExpression, execution);
		}

		String restUrlValue = null;
		if (restUrlExpression != null) {
			restUrlValue = getExpressionAsString(restUrlExpression, execution);
		}

		String httpMethodValue = null;
		if (httpMethodExpression != null) {
			httpMethodValue = getExpressionAsString(httpMethodExpression, execution);
		}

		String requestMappingJSONTemplateValue = null;
		if (requestMappingJSONTemplate != null)
		{
			List requestValues = createRequestValues(execution.getVariables());
			if (requestMappingJSONTemplate != null) {
				JsonNode jsonNode = createJSONRequestObject(requestValues, requestMappingJSONTemplate.getExpressionText());
				requestMappingJSONTemplateValue = jsonNode.toString();
			}

		}

		EndpointConfiguration endpointConfiguration = getEndpointConfiguration(baseEndpointIdValue, baseEndpointNameValue, execution);

		Map extensionElementsMap = getExtensionElements(execution, getRepositoryService(execution));

		List requestMappings = composeRequestMappings(extensionElementsMap, execution);
		List responseMappings = composeResponseMappings(extensionElementsMap);
		List requestHeaders = composeRequestHeaders(extensionElementsMap, execution);

		URI restEndpointURI = composeURI(endpointConfiguration, restUrlValue, execution);
		logger.debug("Using REST URI " + restEndpointURI);
		HttpRequestBase httpRequest = createHttpRequest(restEndpointURI, httpMethodValue, requestMappingJSONTemplateValue, requestMappings, requestHeaders);

		CloseableHttpClient httpClient = null;
		try
		{
			CredentialsProvider credentialsProvider = createCredentialsProvider(endpointConfiguration, execution);
			boolean isHttpsEnabledDomain = false;
			File file = new File(whitelistedDomainsConfFilePath);
			BufferedReader br = new BufferedReader(new FileReader(file)); 

			String domainName; 
			while ((domainName = br.readLine()) != null) {
				System.out.println(domainName); 
				if(restEndpointURI.toString().startsWith(domainName.trim())) {
					isHttpsEnabledDomain = true;
				}
			} 

			// If ssl enabled domain found then add ssl certificate
			if(isHttpsEnabledDomain) {
				httpClient = addSSLCertificate();
			}
			else {
				httpClient = createHttpClient(httpRequest.getURI().getScheme(), credentialsProvider);
			}

			HttpResponse response = executeHttpRequest(httpClient, httpRequest, credentialsProvider);

			mapResponse(response, responseMappings, execution);
		}
		finally
		{
			if (httpClient != null)
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.debug("Could not close httpclient.", e);
				}
		}


	}

	public CloseableHttpClient addSSLCertificate() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyManagementException, UnrecoverableKeyException {

		String jksFilePath = prop.getProperty("keystore_path");
		String jksFilePassword = prop.getProperty("keystore_password");

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(jksFilePath),jksFilePassword.toCharArray());

		SSLContext sslContext = SSLContexts.custom()
				.loadKeyMaterial(ks, jksFilePassword.toCharArray())
				.loadTrustMaterial(null, new TrustSelfSignedStrategy())
				.build();

		SSLConnectionSocketFactory sslConnectionFactory =
				new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.getDefaultHostnameVerifier());

		CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLHostnameVerifier(SSLConnectionSocketFactory.getDefaultHostnameVerifier())
				.setSSLSocketFactory(sslConnectionFactory)
				.build();

		return httpclient;
	}

}
