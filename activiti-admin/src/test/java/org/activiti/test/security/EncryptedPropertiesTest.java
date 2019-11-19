/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.test.security;

import org.activiti.test.ApplicationSecurityTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;

/**
 * @author Ryan Dawson
 *
 * Using this approach we can set any property to an encyrpted value by enclosing in ENC( ) and it will be decrypted so long as the encryption secret is available as a parameter.
 * See more detailed instructions in the below.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationSecurityTestConfiguration.class)
public class EncryptedPropertiesTest  {


    @Autowired
    private EncryptedPropertiesBean encryptedPropertiesBean;

    private final static Logger log = LoggerFactory.getLogger(EncryptedPropertiesTest.class);

    static {

       /*
        Setting the password using System.setProperty is a substitute for setting -Djasypt.encryptor.password=supersecretz in mvn spring-boot:run command (e.g. in start.sh)
        or setting this property in the pom.xml <jsypt.encryptor.password>supersecretz</jsypt.encryptor.password>.
        Any of the jasypt.encryptor properties can be set through System.setProperty, properties file, command line arguments or environment variable.
        In a client situation it will be set in application server's configuration - the client will choose whatever password they wish.
        They will put encrypted values in their property file using ENC(*) format.
        This implementation is based this example - https://www.ricston.com/blog/encrypting-properties-in-spring-boot-with-jasypt-spring-boot/
        The example online how to run the jasypt jar to generate the encrypted password - command is java -cp ~/.m2/repository/org/jasypt/jasypt/1.9.2/jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=contactspassword password=supersecretz algorithm=PBEWITHSHA1ANDDESEDE  - be careful with quotes (best not to use them)
        For decryption the client's secret password has to be available so they will want to set that in their application server.
        For example with tomcat if the war is in $tomcat_home/webapps then it would work to create tomcat_home/bin/setenv.sh to set jasypt.encryptor.password=supersecretz and run using $tomcat_home/bin/catalina.sh run (https://github.com/ulisesbocchio/jasypt-spring-boot/issues/11, https://stackoverflow.com/questions/17019233/pass-user-defined-environment-variable-to-tomcat)
        An example setenv.sh would be (this one also sets algo) - export JAVA_OPTS="$JAVA_OPTS -Djasypt.encryptor.password=supersecretz -Djasypt.encryptor.algorithm=PBEWITHSHA1ANDDESEDE"
        You can disable the VersionsLoggerListener by removing <Listener className="org.apache.catalina.startup.VersionLoggerListener" /> from your Tomcats server.xml (https://stackoverflow.com/questions/35485826/turn-off-tomcat-logging-via-spring-boot-application).
        With WebSphere I would expect to set a jvm parameter - http://www-01.ibm.com/support/docview.wss?uid=swg21417365 or an environment variable - https://www.ibm.com/support/knowledgecenter/en/SSAW57_8.5.5/com.ibm.websphere.nd.doc/ae/welcvariables.html

         */

        System.setProperty("jasypt.encryptor.password", "supersecretz");

            /*
             * In order to use greater than 128-bit encryption algorithms the oracle policy extensions need to be installed.
             * The below provides a way to check for them.
             */
        try{
            int length = Cipher.getMaxAllowedKeyLength("AES");
            boolean unlimited = (length == Integer.MAX_VALUE);
            log.info("Unlimited cryptopgraphy supported by JRE (policy installed) "+unlimited);
        } catch(NoSuchAlgorithmException e){

        }
    }


    @Test
    public void testNonEncrypted() throws Exception {
        Assert.assertTrue(encryptedPropertiesBean.getNotencryptedProperty().equalsIgnoreCase("iamnotencrypted"));
    }

    @Test
    public void testEncrypted() throws Exception{
        Assert.assertTrue(encryptedPropertiesBean.getEncryptedProperty().equalsIgnoreCase("contactspassword"));
    }

    @Test
    public void testRefertoEncrypted() throws Exception {
        Assert.assertTrue(encryptedPropertiesBean.getReferencedProperty().equalsIgnoreCase("contactspassword"));
    }

}
