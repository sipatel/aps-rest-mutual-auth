/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.activiti.license.LicenseStatus;
import com.activiti.repository.ServerConfigRepository;
import com.activiti.service.activiti.ActivitiClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ActivitiEndpointLicenseService {

    // Remote activiti server's license status codes
    public static final String SERVER_STATUS_LICENSE_VALID = "valid";
    public static final String SERVER_STATUS_LICENSE_NOT_FOUND = "not-found";
    public static final String SERVER_STATUS_LICENSE_INVALID_DATE = "invalid-date";

    private static final Logger log = LoggerFactory.getLogger(ActivitiEndpointLicenseService.class);

	private static final String LICENSE_URL = "management/engine/license";
	private static final String KEY = "vN9kuqLLj5SDZ4UpBP6ekonWSVFJwZgg";
	private static final String ALGORITHM = "DESede";

	// Max absolute deviation of license check is one day
	private static final Long LICENSE_CHECK_DEVIATION_MILLIS = 1440000L;


    @Autowired
	protected ServerConfigRepository serverConfigRepository;

	@Autowired
	protected ActivitiClientService clientUtil;

	@Autowired
	protected ObjectMapper objectMapper;

	public void checkServerConfigLicenses() {
		List<ServerConfig> all = serverConfigRepository.findAll();
		if (all != null && all.size() > 0) {
			for (ServerConfig serverConfig : all) {
				LicenseStatus.serverLicenseValidityMap.put(serverConfig.getId(), getServerConfigLicenseStatus(serverConfig));
			}
		}
	}

	public void checkServerConfigLicense(ServerConfig serverConfig) {
		if (serverConfig == null) {
			return;
		}
		LicenseStatus.serverLicenseValidityMap.put(serverConfig.getId(), getServerConfigLicenseStatus(serverConfig));
	}

	protected String getServerConfigLicenseStatus(ServerConfig config) {
		String status = LicenseStatus.INVALID;

		HttpGet get = new HttpGet(clientUtil.getServerUrl(config, LICENSE_URL));

		CloseableHttpClient client = clientUtil.getHttpClient(config);
        HttpClientContext context = clientUtil.createHttpClientContext(get);
        try {
			CloseableHttpResponse response = client.execute(get, context);

			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				if (response.getEntity() != null && response.getEntity().getContentLength() != 0) {
					JsonNode licenseNode = objectMapper.readTree(response.getEntity().getContent());
					if (licenseNode.isObject() && licenseNode.has("holder") && licenseNode.has("licenseCheck")) {

						ObjectNode license = (ObjectNode) licenseNode;
						String holder = license.get("holder").asText();
						String licenseCheck = license.get("licenseCheck").asText();

                        String serverLicenseStatus = license.get("status").asText();
                        if (serverLicenseStatus != null) {
                            if (serverLicenseStatus.equals(SERVER_STATUS_LICENSE_INVALID_DATE)) {
                                status = LicenseStatus.INVALID_DATE;
                            } else if (serverLicenseStatus.equals(SERVER_STATUS_LICENSE_NOT_FOUND)) {
                                status = LicenseStatus.NOT_FOUND;
                            } else if (serverLicenseStatus.equals(SERVER_STATUS_LICENSE_VALID)) {
                                status = LicenseStatus.VALID;
                            } else {
                                log.error("Invalid license response status from Activiti endpoint: " + config.getName());
                                status = LicenseStatus.INVALID;
                            }
                        }

                        // Finally, validate the response license using the shared key
                        if (status.equals(LicenseStatus.VALID) && !validateLicenseCheck(holder, licenseCheck, config)) {
                            log.error("License was considered valid by the Activiti endpoint but when checking it locally it was not. Activity endpoint: " + config.getName());
                            status = LicenseStatus.INVALID;
                        }
					} else {
						log.error("Invalid license response body from Activiti endpoint: " + config.getName());
                        status = LicenseStatus.INVALID;
					}
				} else {
					log.error("Empty license response body from Activiti endpoint: " + config.getName());
                    status = LicenseStatus.INVALID;
				}
			} else if(HttpStatus.SC_CONFLICT == response.getStatusLine().getStatusCode()) {
				log.error("License corrupt in Activiti endpoint: " + config.getName());
                status = LicenseStatus.INVALID;
			} else if(HttpStatus.SC_NOT_FOUND == response.getStatusLine().getStatusCode() || HttpStatus.SC_INTERNAL_SERVER_ERROR == response.getStatusLine().getStatusCode()) {
				log.error("Non-enterprise Activiti endpoint: " + config.getName());
                status = LicenseStatus.ENDPOINT_ERROR;
			} else {
				log.error("Unexpected license response (" + response.getStatusLine().getStatusCode() +") from Activiti endpoint: " + config.getName());
                status = LicenseStatus.ENDPOINT_ERROR;
			}
		} catch (ClientProtocolException e) {
			log.warn("Error while calling Activiti endpoint: " + config.getName() + " - assuming valid license: " + e.getMessage());
            status = LicenseStatus.ENDPOINT_ERROR;
        } catch (IOException e) {
        	log.warn("Error while calling Activiti endpoint: " + config.getName() + " - assuming valid license: " + e.getMessage());
            status = LicenseStatus.ENDPOINT_ERROR;
        } finally {
			try {
			    client.close();
			} catch (IOException ignore) {
              	// Ignore silently
			}
		}

		return status;
	}

	protected boolean validateLicenseCheck(String holder, String licenseCheck, ServerConfig config) {
		boolean validLicense = false;
		String check = null;
		try {
		    check = decryptLicenseCheck(licenseCheck);
        } catch (Exception e) {
        	log.error("Licence check from Activiti endpoint cannot be decrypted: " + config.getName());
        }

		if (check != null) {
			String[] parts = check.split("\\|");
			if (parts.length == 2 && holder.equals(parts[0])) {
				try {
					Long time = Long.parseLong(parts[1]);
					Long diff = Math.abs(Calendar.getInstance().getTimeInMillis() - time);

					if (diff > LICENSE_CHECK_DEVIATION_MILLIS) {
						log.error("Timestamp returned from Activiti endpoint license check deviates too much");
					} else {
						log.debug("Valid license and timestamp in Activiti endpoint: " + config.getName());
						validLicense = true;
					}

				} catch(NumberFormatException ignore) {
					// Will fall trough to returning false
				}
			}

			if (!validLicense) {
				log.error("Licence check in responese from Activiti endpoint invalid: " + config.getName());
			}

		}
	  return validLicense;
  }

	protected static String decryptLicenseCheck(String check) throws Exception {
		SecretKeySpec spec = new SecretKeySpec(Base64.decodeBase64(KEY), ALGORITHM);
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, spec);
		byte[] data = c.doFinal(Base64.decodeBase64(check));
		return new String(data);
	}
}
