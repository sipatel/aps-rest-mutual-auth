/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */

package org.activiti.test.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Ryan Dawson.
 */
@Component
public class EncryptedPropertiesBean {


    @Value("${test.notencrypted.property}")
    protected String notencryptedProperty;

    @Value("${test.encrypted.property}")
    protected String encryptedProperty;

    @Value("${test.referto.encrypted.property}")
    protected String referencedProperty;

    public String getNotencryptedProperty() {
        return notencryptedProperty;
    }

    public void setNotencryptedProperty(String notencryptedProperty) {
        this.notencryptedProperty = notencryptedProperty;
    }

    public String getEncryptedProperty() {
        return encryptedProperty;
    }

    public void setEncryptedProperty(String encryptedProperty) {
        this.encryptedProperty = encryptedProperty;
    }

    public String getReferencedProperty() {
        return referencedProperty;
    }

    public void setReferencedProperty(String referencedProperty) {
        this.referencedProperty = referencedProperty;
    }
}
