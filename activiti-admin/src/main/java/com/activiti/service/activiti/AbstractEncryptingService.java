/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.activiti;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.codec.Base64;

/**
 * Superclass for services that want to encrypt certain properties
 *
 * @author jbarrez
 */
public abstract class AbstractEncryptingService implements EnvironmentAware {

    public static final String UTF8_ENCODING = "UTF-8";
    public static final String AES_KEY = "AES";
    public static final String AES_CYPHER = "AES/CBC/PKCS5PADDING";

    protected IvParameterSpec initializationVectorSpec;

    protected SecretKeySpec secretKeySpec;

    @Override
    public void setEnvironment(Environment environment) {
        String ivString = environment.getRequiredProperty("security.encryption.credentialsIVSpec");
        String secretString = environment.getRequiredProperty("security.encryption.credentialsSecretSpec");

        try {
            initializationVectorSpec = new IvParameterSpec(ivString.getBytes(UTF8_ENCODING));
            secretKeySpec = new SecretKeySpec(secretString.getBytes(UTF8_ENCODING), AES_KEY);
        } catch (UnsupportedEncodingException e) {
            // Should never happen, UTF-8 is supported on all java platforms
            throw new RuntimeException(e);
        }
    }

    protected String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CYPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initializationVectorSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return new String(Base64.encode(encrypted), UTF8_ENCODING);
        } catch (GeneralSecurityException nsae) {
            throw new RuntimeException(nsae);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    protected String decrypt(String encrypted) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(AES_CYPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVectorSpec);
            byte[] original = cipher.doFinal(Base64.decode(encrypted.getBytes(UTF8_ENCODING)));
            return new String(original, UTF8_ENCODING);
        } catch (GeneralSecurityException nsae) {
            throw new RuntimeException(nsae);
        } catch (UnsupportedEncodingException usee) {
            throw new RuntimeException(usee);
        }
    }

}
