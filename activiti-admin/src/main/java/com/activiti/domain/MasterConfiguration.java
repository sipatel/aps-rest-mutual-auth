/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * @author jbarrez
 */
@Entity
@Table(name = "MASTER_CONFIG")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MasterConfiguration {

    public static final String TYPE_BPM_SUITE = "bpmSuite";
    public static final String TYPE_ENGINE = "engine";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "masterConfigIdGenerator")
    @TableGenerator(name = "masterConfigIdGenerator", table = "HIBERNATE_SEQUENCES")
    private Long id;

    @Column(name="config_type")
    private String type;

    @Column(name="config_id")
    private String configId;

    @Column(name="cluster_config_id")
    private Long clusterConfigId;

    @Column(name="config_json")
    private String configJson;

    @Transient
    private String decryptedConfigJson; // NOT stored in db obviously

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public Long getClusterConfigId() {
        return clusterConfigId;
    }

    public void setClusterConfigId(Long clusterConfigId) {
        this.clusterConfigId = clusterConfigId;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public String getDecryptedConfigJson() {
        return decryptedConfigJson;
    }

    public void setDecryptedConfigJson(String decryptedConfigJson) {
        this.decryptedConfigJson = decryptedConfigJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MasterConfiguration config = (MasterConfiguration) o;

        if (!id.equals(config.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


}
