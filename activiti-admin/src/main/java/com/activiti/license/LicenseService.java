/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.license;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.verhas.licensor.License;

@Service
public class LicenseService {
	
	private final Logger log = LoggerFactory.getLogger(LicenseService.class);
	
	private static final String LICENSE_FILE = "activiti.lic";
	private static final String PUBLIC_KEY_FILE = "pubring.gpg";

	private static final String HOLDER = "holder";
	private static final String SUBJECT = "subject";
	private static final String PRODUCT_KEY = "productKey";
	private static final String GOOD_AFTER_DATE = "goodAfterDate";
	private static final String GOOD_BEFORE_DATE = "goodBeforeDate";
	private static final String NUMBER_LICENSES = "numberOfLicenses";
	private static final String NUMBER_PROCESSES = "numberOfProcesses";
	private static final String NUMBER_EDITORS = "numberOfEditors";
	private static final String NUMBER_ADMINS = "numberOfAdmins";
	private static final String MULTI_TENANT = "multiTenant";
    private static final String DEFAULT_TENANT = "defaultTenant";
	
	private static final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyyMMdd");

	byte [] digest = new byte[] {
		(byte)0x6D, 
		(byte)0x15, (byte)0xE2, (byte)0x0B, (byte)0xA1, (byte)0x68, (byte)0x5D, (byte)0x91, (byte)0x55, 
		(byte)0x0C, (byte)0x8E, (byte)0xCA, (byte)0x3A, (byte)0x55, (byte)0x2C, (byte)0x44, (byte)0x15, 
		(byte)0x72, (byte)0x75, (byte)0xED, (byte)0xB6, (byte)0x6D, (byte)0xFF, (byte)0xE7, (byte)0x8F, 
		(byte)0xC0, (byte)0xCF, (byte)0xA0, (byte)0x53, (byte)0x37, (byte)0xB5, (byte)0xBF, (byte)0x05, 
		(byte)0x3E, (byte)0x16, (byte)0x6E, (byte)0xD0, (byte)0x68, (byte)0xA1, (byte)0x81, (byte)0x6D, 
		(byte)0xAA, (byte)0x4E, (byte)0x8E, (byte)0x1F, (byte)0x05, (byte)0x9D, (byte)0x99, (byte)0x4F, 
		(byte)0x98, (byte)0x21, (byte)0x8B, (byte)0xAD, (byte)0xDB, (byte)0x15, (byte)0x07, (byte)0x21, 
		(byte)0xF1, (byte)0x23, (byte)0x8F, (byte)0x0E, (byte)0x2B, (byte)0xF4, (byte)0x2E, 
	};
	
	public String validateLicense(License license) {
    	
    	String holder = license.getFeature(HOLDER);
		if (StringUtils.isEmpty(holder)) {
			throw new LicenseException("Expected holder info is not present");
		}
		
		String subject = license.getFeature(SUBJECT);
		if (LicenseInfo.DEFAULT_SUBJECT.equals(subject) == false) {
			throw new LicenseException("Subject value is not expected");
		}
		
		String goodAfterDateString = license.getFeature(GOOD_AFTER_DATE);
		Date goodAfterDate = null;
		try {
			goodAfterDate = dateFormat.parse(goodAfterDateString);
		} catch (Exception e) {
			throw new LicenseException("Error parsing good after date", e);
		}
		
		Date todayDate = new Date();
		if (todayDate.before(goodAfterDate)) {
			log.error("License is not valid yet, license is valid starting from " + goodAfterDateString);
            return LicenseStatus.INVALID_DATE;
		}
		
		String goodBeforeDateString = license.getFeature(GOOD_BEFORE_DATE);
		Date goodBeforeDate = null;
		try {
			goodBeforeDate = dateFormat.parse(goodBeforeDateString);
		} catch (Exception e) {
			throw new LicenseException("Error parsing good before date", e);
		}
		
		if (todayDate.after(goodBeforeDate)) {
            log.error("License is not valid anymore, license was valid until " + goodBeforeDateString);
            return LicenseStatus.INVALID_DATE;
		}

        return LicenseStatus.VALID;
	}
	
	public FeatureInfo getLicenseFeatureInfo() {
		License license = loadLicense();
		return getLicenseFeatureInfo(license);
	}
	
	public FeatureInfo getLicenseFeatureInfo(License license) {
		FeatureInfo featureInfo = new FeatureInfo();
		featureInfo.setNumberOfLicenses(Integer.valueOf(license.getFeature(NUMBER_LICENSES)));
		featureInfo.setNumberOfProcesses(Integer.valueOf(license.getFeature(NUMBER_PROCESSES)));
		featureInfo.setNumberOfEditors(Integer.valueOf(license.getFeature(NUMBER_EDITORS)));
		featureInfo.setNumberOfAdmins(Integer.valueOf(license.getFeature(NUMBER_ADMINS)));
		featureInfo.setMultiTenant(Boolean.valueOf(license.getFeature(MULTI_TENANT)));
		featureInfo.setDefaultTenant(license.getFeature(DEFAULT_TENANT));
		return featureInfo;
	}
	
	public LicenseInfo getLicenseInfo() {
		LicenseInfo info = new LicenseInfo();
		License license = loadLicense();
		if (license != null) {
            info.setHolder(license.getFeature(HOLDER));
            info.setSubject(license.getFeature(SUBJECT));
            info.setProductKey(license.getFeature(PRODUCT_KEY));
            try {
            	info.setGoodAfterDate(dateFormat.parse(license.getFeature(GOOD_AFTER_DATE)));
            	info.setGoodBeforeDate(dateFormat.parse(license.getFeature(GOOD_BEFORE_DATE)));
            } catch (Exception e) {
            	log.error("Error formatting license date", e);
            }
            info.setFeatureInfo(getLicenseFeatureInfo(license));
		}
        
        return info;
	}
	
	public License loadLicense() {
	    License license = null;
	    try {
	        license = loadLicenseFromClassPath();
	    } catch (Throwable t) {
            log.info("Error loading license from classpath, try loading from user home", t);
        }
	    
	    if (license == null) {
	        try {
                license = loadLicenseFromUserHome();
                
            } catch (Throwable t) {
                log.info("Error loading license from user home", t);
            }
	    }
	    return license;
	}
	
	protected License loadLicenseFromClassPath() {
		License license = null;
		try {
			InputStream securityStream = this.getClass().getClassLoader().getResourceAsStream(PUBLIC_KEY_FILE);
			if (securityStream != null) {
			    InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(LICENSE_FILE);
			    if (fileStream != null) {
    			    license = new License();
        			license.loadKeyRing(securityStream, digest);
        			license.setLicenseEncoded(fileStream);
                    IOUtils.closeQuietly(fileStream);
			    }
    			
			    IOUtils.closeQuietly(securityStream);
			}
			
		} catch (Exception e) {
			throw new LicenseException("Error loading license file " + LICENSE_FILE, e);
		}
		return license;
	}
	
	protected License loadLicenseFromUserHome() {
		String fileSeparator = System.getProperty("file.separator");
	    return loadLicenseFromFileLocation(System.getProperty("user.home") +  fileSeparator + 
	    		".activiti" + fileSeparator + "enterprise-license" + fileSeparator);
	}
	
	protected License loadLicenseFromFileLocation(String location) {
	    License license = null;
	    try {
	        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(PUBLIC_KEY_FILE);
	        if (inputStream != null) {
	            File licenseFile = new File(location + LICENSE_FILE);
                if (licenseFile.exists()) {
    	            license = new License();
    	            license.loadKeyRing(inputStream, digest);
    	            
    	            FileInputStream licenseFileInputStream = new FileInputStream(licenseFile);
                    license.setLicenseEncoded(licenseFileInputStream);
                    IOUtils.closeQuietly(licenseFileInputStream);
                }
	            
                IOUtils.closeQuietly(inputStream);
	        }
	
	    } catch (Exception e) {
	    	throw new LicenseException("Error loading license file " + LICENSE_FILE, e);
	    }
	    return license;
	  }
}
