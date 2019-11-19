/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.domain.representation;

import java.io.Serializable;

/**
 * Representation of the config json stored for an Activiti BPM Suite master config.
 *
 * @author jbarrez
 */
public class BpmSuiteMasterConfigurationRepresentation extends MasterConfigurationRepresentation {

    private String propertiesText;

    public BpmSuiteMasterConfigurationRepresentation() {
        this.setType("bpmSuite");
    }

    public String getPropertiesText() {
        return propertiesText;
    }

    public void setPropertiesText(String propertiesText) {
        this.propertiesText = propertiesText;
    }
}
