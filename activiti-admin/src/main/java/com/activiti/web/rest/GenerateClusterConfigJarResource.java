/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ClusterConfig;
import com.activiti.service.activiti.ClusterConfigService;
import com.activiti.web.rest.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;

/**
 * @author jbarrez
 */
@RestController
@RequestMapping("/rest/cluster-config-jars")
public class GenerateClusterConfigJarResource {

	private final Logger log = LoggerFactory.getLogger(GenerateClusterConfigJarResource.class);

	private static final String NEWLINE = "\r\n";

	@Autowired
	protected ClusterConfigService clusterConfigService;

	@Timed
	@RequestMapping(method = RequestMethod.GET, value = "/{clusterConfigId}", produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	public void generateClusterConfigJar(HttpServletResponse response, @PathVariable Long clusterConfigId,
                                         @RequestParam(value = "adminAppUrl", required = true) String adminAppUrl) throws Exception {

		ClusterConfig clusterConfig = clusterConfigService.findOne(clusterConfigId);
		if (clusterConfig != null) {
			response.setContentType("application/java-archive");
			response.setHeader("Content-Disposition", "attachment;filename=activiti-" + clusterConfig.getClusterName() + "-cluster-cfg.jar");
			ServletOutputStream out = response.getOutputStream();
			generateJar(out, clusterConfig, adminAppUrl);
		} else {
			throw new BadRequestException("No cluster config found with id: "+ clusterConfigId);
		}
	}

	protected void generateJar(OutputStream outputStream, ClusterConfig clusterConfig, String adminAppUrl) throws IOException {

		// Generate activiti-cluster.properties
		String activitiClusterProperties = generatePropertyFileString(clusterConfig, adminAppUrl);

		// Generate jar
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest);

		JarEntry entry = new JarEntry("activiti-cluster.properties");
		entry.setTime(new Date().getTime());
		jarOutputStream.putNextEntry(entry);

		BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(activitiClusterProperties.getBytes("UTF-8")));

		byte[] buffer = new byte[1024];
		while (true) {
			int count = in.read(buffer);
			if (count == -1)
				break;
			jarOutputStream.write(buffer, 0, count);
		}
		jarOutputStream.closeEntry();

		// Flush and close stream
		jarOutputStream.flush();
		jarOutputStream.close();
	}

	private String generatePropertyFileString(ClusterConfig clusterConfig, String adminAppUrl) {
		StringBuilder strb = new StringBuilder();
        strb.append("admin.app.url=" + adminAppUrl + NEWLINE);
		strb.append("cluster.name=" + clusterConfig.getClusterName() + NEWLINE);
        strb.append("cluster.username=" + clusterConfig.getUser().getLogin() + NEWLINE);
		strb.append("cluster.password=" + clusterConfigService.getDecryptedClusterPassword(clusterConfig) + NEWLINE);
		strb.append("master.cfg.required=" + clusterConfig.getRequiresMasterConfig() + NEWLINE);
		strb.append("metric.sending.interval=" + clusterConfig.getMetricSendingInterval() + NEWLINE);

		return strb.toString();
	}

	/**
	 * Parses the tcp interfaces which are of the form x:y, w:z, etc.
	 * and gets the host + port for the given hostinterface.
	 *
	 * Results:
	 * [0]: Host interface
	 * [1]: Host port
	 * [2]: Other tcp interfaces
	 *
	 */
	@SuppressWarnings("unchecked")
	protected Object[] parseHostAndPorts(String tcpInterfaces, String hostInterface) {
		try {

			String requiredHost = hostInterface.split(":")[0];
			String requiredPort = hostInterface.split(":")[1];

			Object[] result = new Object[3];
			String[] addresses = tcpInterfaces.split(",");
			for (String address : addresses) {
				String[] splittedAddress = address.split(":");
				if (requiredHost.equals(splittedAddress[0]) && requiredPort.equals(splittedAddress[1])) {
					result[0] = splittedAddress[0];
					result[1] = splittedAddress[1];
				} else {
					if (result[2] == null) {
						result[2] = new ArrayList<String>();
 					}
					((ArrayList<String>)result[2]).add(address);
				}
			}
			return result;
		} catch (Exception e) {
			log.warn("Something went wrong while parsing the tcp interfaces", e);
		}
		return new Object[] {};
	}

	protected String toFlatList(ArrayList<String> list) {
		StringBuilder strb = new StringBuilder();
		for (int i=0; i<list.size(); i++) {
			strb.append(list.get(i));
			if (i != list.size() - 1) {
				strb.append(",");
			}
		}
		return strb.toString();
	}

}
