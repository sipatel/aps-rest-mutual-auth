package com.activiti.license;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.verhas.licensor.License;

public class LicenseManager {
	
	private static final String HOLDER = "holder";
	private static final String SUBJECT = "subject";
	private static final String PRODUCT_KEY = "productKey";
	private static final String GOOD_AFTER_DATE = "goodAfterDate";
	private static final String GOOD_BEFORE_DATE = "goodBeforeDate";
	private static final String NUMBER_LICENSES = "numberOfLicenses";
	private static final String NUMBER_PROCESSES = "numberOfProcesses";
	private static final String NUMBER_EDITORS = "numberOfEditors";
	private static final String NUMBER_ADMINS = "numberOfAdmins";
	private static final String NUMBER_APPS = "numberOfApps";
	private static final String MULTI_TENANT = "multiTenant";
	private static final String DEFAULT_TENANT = "defaultTenant";
	
	private static List<String> validVersionsList;
	
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

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
	
	public LicenseManager() {
		validVersionsList = new ArrayList<String>();
		validVersionsList.add("1.0ev");
		validVersionsList.add("1.0ent");
		validVersionsList.add("1.0dep");
		validVersionsList.add("1.1ev");
        validVersionsList.add("1.1ent");
        validVersionsList.add("1.1dep");
	}
	
	public void createLicense(LicenseInfo licenseInfo, String fileName) throws Exception {
		if (validVersionsList.contains(licenseInfo.getProductKey()) == false) {
			throw new LicenseException("Product key is not valid");
		}
		
		License license = new License();
		license.setLicense(getLicenseContent(licenseInfo));
		InputStream securityStream = new FileInputStream("secring.gpg");
		license.loadKey(securityStream, "Activiti License Server (primary key) <activiti@alfresco.com>");
		Properties licenseProperties = new Properties();
		licenseProperties.load(new FileInputStream("license.properties"));
		String encodedLicense = license.encodeLicense(licenseProperties.getProperty("passphrase"));
		FileWriter writer = new FileWriter(fileName);
		IOUtils.write(encodedLicense, writer);
		writer.flush();
		writer.close();
	}
	
	public void validateLicense(String fileName) {
		License license = loadLicenseFromClassPath(fileName);
		validateLicense(license);
	}
	
	public void validateLicense(License license) {
		String holder = license.getFeature(HOLDER);
		if (StringUtils.isEmpty(holder)) {
			throw new LicenseException("Expected holder info is not present");
		}
		
		String subject = license.getFeature(SUBJECT);
		if (LicenseInfo.DEFAULT_SUBJECT.equals(subject) == false) {
			throw new LicenseException("Subject value is not expected");
		}
		
		String productKey = license.getFeature(PRODUCT_KEY);
		if (validVersionsList.contains(productKey) == false) {
			throw new LicenseException("Product key is not valid");
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
			throw new LicenseException("License is not valid yet, license is valid starting from " + goodAfterDateString);
		}
		
		String goodBeforeDateString = license.getFeature(GOOD_BEFORE_DATE);
		Date goodBeforeDate = null;
		try {
			goodBeforeDate = dateFormat.parse(goodBeforeDateString);
		} catch (Exception e) {
			throw new LicenseException("Error parsing good before date", e);
		}
		
		if (todayDate.after(goodBeforeDate)) {
			throw new LicenseException("License is not valid anymore, license was valid until " + goodBeforeDateString);
		}
	}
	
	public FeatureInfo getLicenseFeatureInfo(String fileName) {
		License license = loadLicenseFromClassPath(fileName);
		return getLicenseFeatureInfo(license);
	}
	
	public FeatureInfo getLicenseFeatureInfo(License license) {
		FeatureInfo featureInfo = new FeatureInfo();
		featureInfo.setNumberOfLicenses(Integer.valueOf(license.getFeature(NUMBER_LICENSES)));
		featureInfo.setNumberOfProcesses(Integer.valueOf(license.getFeature(NUMBER_PROCESSES)));
		featureInfo.setNumberOfEditors(Integer.valueOf(license.getFeature(NUMBER_EDITORS)));
		featureInfo.setNumberOfAdmins(Integer.valueOf(license.getFeature(NUMBER_ADMINS)));
		featureInfo.setNumberOfApps(Integer.valueOf(license.getFeature(NUMBER_APPS)));
		featureInfo.setMultiTenant(Boolean.valueOf(license.getFeature(MULTI_TENANT)));
		featureInfo.setDefaultTenant(license.getFeature(DEFAULT_TENANT));
		return featureInfo;
	}
	
	private License loadLicenseFromClassPath(String fileName) {
		License license = new License();
		try {
			InputStream securityStream = this.getClass().getClassLoader().getResourceAsStream("pubring.gpg");
			license.loadKeyRing(securityStream, digest);
			InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
			license.setLicenseEncoded(fileStream);
		} catch (Exception e) {
			throw new LicenseException("Error loading license file", e);
		}
		return license;
	}
	
	public License loadLicenseFromFilePath(String fileName) {
		License license = new License();
		try {
			InputStream securityStream = this.getClass().getClassLoader().getResourceAsStream("pubring.gpg");
			license.loadKeyRing(securityStream, digest);
			InputStream fileStream = new FileInputStream(fileName);
			license.setLicenseEncoded(fileStream);
		} catch (Exception e) {
			throw new LicenseException("Error loading license file", e);
		}
		return license;
	}
	
	public String getLicenseContent(License license) {
		LicenseInfo licenseInfo = new LicenseInfo();
		licenseInfo.setHolder(license.getFeature(HOLDER));
		licenseInfo.setSubject(license.getFeature(SUBJECT));
		licenseInfo.setProductKey(license.getFeature(PRODUCT_KEY));
		try {
			licenseInfo.setGoodAfterDate(dateFormat.parse(license.getFeature(GOOD_AFTER_DATE)));
		} catch (Exception e) {
			throw new LicenseException("Error parsing good after date", e);
		}
		try {
			licenseInfo.setGoodBeforeDate(dateFormat.parse(license.getFeature(GOOD_BEFORE_DATE)));
		} catch (Exception e) {
			throw new LicenseException("Error parsing good before date", e);
		}
		licenseInfo.getFeatureInfo().setNumberOfLicenses(Integer.valueOf(license.getFeature(NUMBER_LICENSES)));
		licenseInfo.getFeatureInfo().setNumberOfProcesses(Integer.valueOf(license.getFeature(NUMBER_PROCESSES)));
		licenseInfo.getFeatureInfo().setNumberOfEditors(Integer.valueOf(license.getFeature(NUMBER_EDITORS)));
		licenseInfo.getFeatureInfo().setNumberOfAdmins(Integer.valueOf(license.getFeature(NUMBER_ADMINS)));
		if (license.getFeature(NUMBER_APPS) != null) {
		    licenseInfo.getFeatureInfo().setNumberOfApps(Integer.valueOf(license.getFeature(NUMBER_APPS)));
		}
		licenseInfo.getFeatureInfo().setMultiTenant(Boolean.valueOf(license.getFeature(MULTI_TENANT)));
		licenseInfo.getFeatureInfo().setDefaultTenant(license.getFeature(DEFAULT_TENANT));
		return getLicenseContent(licenseInfo);
	}
	
	private String getLicenseContent(LicenseInfo licenseInfo) {
		StringBuilder licenseBuilder = new StringBuilder();
		writeLine(HOLDER, licenseInfo.getHolder(), licenseBuilder);
		writeLine(SUBJECT, licenseInfo.getSubject(), licenseBuilder);
		writeLine(PRODUCT_KEY, licenseInfo.getProductKey(), licenseBuilder);
		writeLine(GOOD_AFTER_DATE, dateFormat.format(licenseInfo.getGoodAfterDate()), licenseBuilder);
		writeLine(GOOD_BEFORE_DATE, dateFormat.format(licenseInfo.getGoodBeforeDate()), licenseBuilder);
		writeLine(NUMBER_LICENSES, String.valueOf(licenseInfo.getFeatureInfo().getNumberOfLicenses()), licenseBuilder);
		writeLine(NUMBER_PROCESSES, String.valueOf(licenseInfo.getFeatureInfo().getNumberOfProcesses()), licenseBuilder);
		writeLine(NUMBER_EDITORS, String.valueOf(licenseInfo.getFeatureInfo().getNumberOfEditors()), licenseBuilder);
		writeLine(NUMBER_ADMINS, String.valueOf(licenseInfo.getFeatureInfo().getNumberOfAdmins()), licenseBuilder);
		writeLine(NUMBER_APPS, String.valueOf(licenseInfo.getFeatureInfo().getNumberOfApps()), licenseBuilder);
		writeLine(MULTI_TENANT, String.valueOf(licenseInfo.getFeatureInfo().isMultiTenant()), licenseBuilder);
		writeLine(DEFAULT_TENANT, String.valueOf(licenseInfo.getFeatureInfo().getDefaultTenant()), licenseBuilder);
		return licenseBuilder.toString();
	}
	
	private void writeLine(String propertyName, String propertyValue, StringBuilder licenseBuilder) {
		licenseBuilder.append(propertyName);
		licenseBuilder.append("=");
		licenseBuilder.append(propertyValue);
		licenseBuilder.append("\n");
	}
}
