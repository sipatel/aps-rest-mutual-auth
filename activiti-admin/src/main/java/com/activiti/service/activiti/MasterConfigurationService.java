/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import com.activiti.domain.MasterConfiguration;
import com.activiti.domain.representation.BpmSuiteMasterConfigurationRepresentation;
import com.activiti.domain.representation.MasterConfigurationRepresentation;
import com.activiti.domain.representation.ProcessEngineMasterConfigurationRepresentation;
import com.activiti.repository.MasterConfigRepository;
import com.activiti.service.activiti.cluster.ActivitiClusterService;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;

/**
 * @author jbarrez
 */
@Service
@Transactional
public class MasterConfigurationService extends AbstractEncryptingService {

    private static final Logger logger = LoggerFactory.getLogger(MasterConfigurationService.class);

	@Autowired
	protected MasterConfigRepository masterConfigRepository;

    @Autowired
    protected ActivitiClusterService activitiClusterService;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String cachedBpmSuiteMasterConfigTemplate;

    public String findMasterConfigurationUuidByClusterConfigId(Long cluserConfigId) {
        MasterConfiguration masterConfiguration = masterConfigRepository.findByClusterConfigId(cluserConfigId);
        if (masterConfiguration != null) {
            return masterConfiguration.getConfigId();
        }
        return null;
    }

    public MasterConfigurationRepresentation findByClusterConfigId(Long cluserConfigId) {

        MasterConfiguration masterConfiguration = decryptJson(masterConfigRepository.findByClusterConfigId(cluserConfigId));

        if (masterConfiguration != null && StringUtils.isNotEmpty(masterConfiguration.getConfigJson())) {
            try {

                MasterConfigurationRepresentation result = null;
                if (masterConfiguration.getType().equals(MasterConfiguration.TYPE_BPM_SUITE)) {
                    result = objectMapper.readValue(masterConfiguration.getDecryptedConfigJson(), BpmSuiteMasterConfigurationRepresentation.class);
                } else if (masterConfiguration.getType().equals(MasterConfiguration.TYPE_ENGINE)) {
                    result = objectMapper.readValue(masterConfiguration.getDecryptedConfigJson(), ProcessEngineMasterConfigurationRepresentation.class);
                } else {
                    logger.warn("Could not deserialize master configuration: unknown type '" + masterConfiguration.getType() + "'");
                }

                if (result != null) {
                    result.setConfigId(masterConfiguration.getConfigId());
                }

                return result;

            } catch (IOException e) {
                logger.error("Could not deserialize master configuration", e);
            }
        }

        return null;
    }

    public MasterConfiguration find(Long id, boolean decrypt) {
        MasterConfiguration masterConfiguration = masterConfigRepository.findOne(id);
        if (decrypt) {
            return decryptJson(masterConfiguration);
        } else {
            return masterConfiguration;
        }
    }

    protected MasterConfiguration decryptJson(MasterConfiguration masterConfiguration) {
        if (masterConfiguration == null) {
            return null;
        }

        if (StringUtils.isNotEmpty(masterConfiguration.getConfigJson())) {
            masterConfiguration.setDecryptedConfigJson(decrypt(masterConfiguration.getConfigJson()));
        }
        return masterConfiguration;
    }

    public void storeBpmSuiteMasterConfiguration(Long clusterConfigId, String config) {

        MasterConfiguration masterConfiguration = null;

        // There can be only one master config / cluster
        MasterConfiguration currentMasterConfiguration = masterConfigRepository.findByClusterConfigId(clusterConfigId);
        if (currentMasterConfiguration != null) {
            masterConfiguration = currentMasterConfiguration;
        } else {
            masterConfiguration = new MasterConfiguration();
        }

        masterConfiguration.setType(MasterConfiguration.TYPE_BPM_SUITE);
        masterConfiguration.setClusterConfigId(clusterConfigId);
        masterConfiguration.setConfigId(UUID.randomUUID().toString());

        // Verify if the config are valid properties
        try {
            Properties properties = new Properties();
            properties.load(new StringReader(config));
        } catch (Exception e) {
            throw new BadRequestException("Invalid config: " + e.getMessage());
        }

        // Reaching this, we know it's a valid properties file
        BpmSuiteMasterConfigurationRepresentation configJson = new BpmSuiteMasterConfigurationRepresentation();
        configJson.setPropertiesText(config);
        try {
            masterConfiguration.setDecryptedConfigJson(objectMapper.writeValueAsString(configJson));
        } catch (JsonProcessingException e) {
            logger.error("Could not serialize BPM Suite master config", e);
        }

        save(masterConfiguration, true);

        // Let the cluster service know
        activitiClusterService.updateMasterConfiguration(clusterConfigId);
    }

    public void storeProcessEngineMasterConfiguration(Long clusterConfigId, JsonNode config) {

        MasterConfiguration masterConfiguration = null;

        // There can be only one master config / cluster
        MasterConfiguration currentMasterConfiguration = masterConfigRepository.findByClusterConfigId(clusterConfigId);
        if (currentMasterConfiguration != null) {
            masterConfiguration = currentMasterConfiguration;
        } else {
            masterConfiguration = new MasterConfiguration();
        }

        masterConfiguration.setType(MasterConfiguration.TYPE_ENGINE);
        masterConfiguration.setClusterConfigId(clusterConfigId);
        masterConfiguration.setConfigId(UUID.randomUUID().toString());

        // Verify if the config are valid properties
        try {
            ProcessEngineMasterConfigurationRepresentation configJsonRepresentation = objectMapper.treeToValue(config, ProcessEngineMasterConfigurationRepresentation.class);
            masterConfiguration.setDecryptedConfigJson(objectMapper.writeValueAsString(configJsonRepresentation));
        } catch (IOException e) {
            logger.error("Could not serialize BPM Suite master config", e);
        }


        save(masterConfiguration, true);

        // Let the cluster service know
        activitiClusterService.updateMasterConfiguration(clusterConfigId);
    }

    public void save(MasterConfiguration masterConfiguration, boolean encrypt) {
        if (encrypt && StringUtils.isNotEmpty(masterConfiguration.getDecryptedConfigJson())) {
            masterConfiguration.setConfigJson(encrypt(masterConfiguration.getDecryptedConfigJson()));
        }
        masterConfigRepository.save(masterConfiguration);
    }

    public void deleteMasterConfigurationForCluster(Long clusterConfigId) {
        MasterConfiguration masterConfiguration = masterConfigRepository.findByClusterConfigId(clusterConfigId);
        if (masterConfiguration != null) {
            masterConfigRepository.delete(masterConfiguration);
        }

        // Let the cluster service know
        activitiClusterService.deleteMasterConfiguration(clusterConfigId);
    }

    public String getBpmSuiteTemplate() {
        if (cachedBpmSuiteMasterConfigTemplate == null) {
            try {
                cachedBpmSuiteMasterConfigTemplate = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("master-config-templates/bpm-suite-master-config-template.txt"));
            } catch (IOException e) {
                logger.error("Could not read bpm suite master config template", e);
            }
        }
        return cachedBpmSuiteMasterConfigTemplate;
    }

}
