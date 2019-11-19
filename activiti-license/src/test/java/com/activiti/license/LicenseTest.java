package com.activiti.license;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.verhas.licensor.License;

public class LicenseTest {
	
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

	@Test
	public void createLicense() throws Exception {
		License license = new License();
		InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream("license.txt");
		String licenseString = IOUtils.toString(fileStream);
		license.setLicense(licenseString);
		InputStream securityStream = new FileInputStream("secring.gpg");
		license.loadKey(securityStream, "Activiti License Server (primary key) <activiti@alfresco.com>");
		Properties licenseProperties = new Properties();
		licenseProperties.load(new FileInputStream("license.properties"));
		String encodedLicense = license.encodeLicense(licenseProperties.getProperty("passphrase"));
		assertNotNull(encodedLicense);
	}
	
	@Test
	public void printDigest() throws Exception {
		License license = new License();
		InputStream securityStream = this.getClass().getClassLoader().getResourceAsStream("pubring.gpg");
		license.loadKeyRing(securityStream, null);
		assertTrue(license.dumpPublicKeyRingDigest().contains("(byte)0xAA, (byte)0x4E, (byte)0x8E, (byte)0x1F, (byte)0x05, (byte)0x9D, (byte)0x99, (byte)0x4F,"));
	}
	
	@Test
	public void checkLicense() throws Exception {
		License license = new License();
		InputStream securityStream = this.getClass().getClassLoader().getResourceAsStream("pubring.gpg");
		license.loadKeyRing(securityStream, digest);
		InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream("encodedlicense.txt");
		license.setLicenseEncoded(fileStream);
		String edition = license.getFeature("edition");
		assertEquals("asp", edition);
	}
	
	@Test
	public void createLicenseWithLicenseManager() throws Exception {
		LicenseManager licenseManager = new LicenseManager();
		LicenseInfo licenseInfo = new LicenseInfo();
		licenseInfo.setHolder("John Doe Ltd");
		Calendar afterCal = new GregorianCalendar(2011, 2, 3);
		licenseInfo.setGoodAfterDate(afterCal.getTime());
		Calendar beforeCal = new GregorianCalendar();
		beforeCal.add(Calendar.MONTH, 1);
		licenseInfo.setGoodBeforeDate(beforeCal.getTime());
		licenseInfo.setProductKey("1.0ev");
		licenseInfo.getFeatureInfo().setNumberOfAdmins(2);
		licenseManager.createLicense(licenseInfo, "test.lic");
		
		License license = licenseManager.loadLicenseFromFilePath("test.lic");
		licenseManager.validateLicense(license);
		
		FileUtils.deleteQuietly(new File("test.lic"));
	}
	
	@Test
	public void createInvalidVersionLicenseWithLicenseManager() throws Exception {
		LicenseManager licenseManager = new LicenseManager();
		LicenseInfo licenseInfo = new LicenseInfo();
		licenseInfo.setHolder("John Doe Ltd");
		Calendar afterCal = new GregorianCalendar(2011, 2, 3);
		licenseInfo.setGoodAfterDate(afterCal.getTime());
		Calendar beforeCal = new GregorianCalendar();
		beforeCal.add(Calendar.MONTH, 1);
		licenseInfo.setGoodBeforeDate(beforeCal.getTime());
		licenseInfo.setProductKey("1.0ev2");
		licenseInfo.getFeatureInfo().setNumberOfAdmins(2);
		try {
			licenseManager.createLicense(licenseInfo, "test.lic");
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof LicenseException);
		}
		
		FileUtils.deleteQuietly(new File("test.lic"));
	}
	
	@Test
	public void createExpiredLicenseWithLicenseManager() throws Exception {
		LicenseManager licenseManager = new LicenseManager();
		LicenseInfo licenseInfo = new LicenseInfo();
		licenseInfo.setHolder("John Doe Ltd");
		Calendar afterCal = new GregorianCalendar(2011, 2, 3);
		licenseInfo.setGoodAfterDate(afterCal.getTime());
		Calendar beforeCal = new GregorianCalendar();
		beforeCal.add(Calendar.MONTH, -1);
		licenseInfo.setGoodBeforeDate(beforeCal.getTime());
		licenseInfo.setProductKey("1.0ev");
		licenseInfo.getFeatureInfo().setNumberOfAdmins(2);
		licenseManager.createLicense(licenseInfo, "test.lic");
		
		License license = licenseManager.loadLicenseFromFilePath("test.lic");
		try {
			licenseManager.validateLicense(license);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof LicenseException);
		}
		
		FileUtils.deleteQuietly(new File("test.lic"));
	}
	
	@Test
	public void createNotYetValidLicenseWithLicenseManager() throws Exception {
		LicenseManager licenseManager = new LicenseManager();
		LicenseInfo licenseInfo = new LicenseInfo();
		licenseInfo.setHolder("John Doe Ltd");
		Calendar afterCal = new GregorianCalendar();
		afterCal.add(Calendar.DAY_OF_MONTH, 3);
		licenseInfo.setGoodAfterDate(afterCal.getTime());
		Calendar beforeCal = new GregorianCalendar();
		beforeCal.add(Calendar.MONTH, 1);
		licenseInfo.setGoodBeforeDate(beforeCal.getTime());
		licenseInfo.setProductKey("1.0ev");
		licenseInfo.getFeatureInfo().setNumberOfAdmins(2);
		licenseManager.createLicense(licenseInfo, "test.lic");
		
		License license = licenseManager.loadLicenseFromFilePath("test.lic");
		try {
			licenseManager.validateLicense(license);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof LicenseException);
		}
		
		FileUtils.deleteQuietly(new File("test.lic"));
	}
	
	@Test
	public void validateFeatureInfo() throws Exception {
		LicenseManager licenseManager = new LicenseManager();
		LicenseInfo licenseInfo = new LicenseInfo();
		licenseInfo.setHolder("John Doe Ltd");
		Calendar afterCal = new GregorianCalendar(2011, 2, 3);
		licenseInfo.setGoodAfterDate(afterCal.getTime());
		Calendar beforeCal = new GregorianCalendar();
		beforeCal.add(Calendar.MONTH, 1);
		licenseInfo.setGoodBeforeDate(beforeCal.getTime());
		licenseInfo.setProductKey("1.0ev");
		licenseInfo.getFeatureInfo().setNumberOfAdmins(2);
		licenseInfo.getFeatureInfo().setNumberOfProcesses(200);
		licenseInfo.getFeatureInfo().setDefaultTenant("alfresco");
		licenseManager.createLicense(licenseInfo, "test.lic");
		
		License license = licenseManager.loadLicenseFromFilePath("test.lic");
		FeatureInfo featureInfo = licenseManager.getLicenseFeatureInfo(license);
		assertEquals(2, featureInfo.getNumberOfAdmins());
		assertEquals(200, featureInfo.getNumberOfProcesses());
		assertEquals(5, featureInfo.getNumberOfEditors());
		assertEquals(1, featureInfo.getNumberOfLicenses());
		assertTrue(!featureInfo.isMultiTenant());
		assertEquals("alfresco", featureInfo.getDefaultTenant());
		
		FileUtils.deleteQuietly(new File("test.lic"));
	}
}
