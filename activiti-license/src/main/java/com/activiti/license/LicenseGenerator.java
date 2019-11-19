package com.activiti.license;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.verhas.licensor.License;

public class LicenseGenerator {
	
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	
	/**
	 * 
	 * Expects license file information in the following format:
	 * 
	 * -file FILE_NAME -holder CUSTOMER_NAME -version APP_VERSION -start VALID_FROM (yyyyMMdd) -end VALID_UNTIL (yyyyMMdd) -numberOfLicenses 1 -numberOfProcesses 100 -numberOfEditors 5 -numberOfAdmins 1 -multiTenant false -defaultTenant alfresco.com
	 * -f FILE_NAME -h CUSTOMER_NAME -v APP_VERSION -s VALID_FROM (yyyyMMdd) -e VALID_UNTIL (yyyyMMdd) -numberOfLicenses 1 -numberOfProcesses 100 -numberOfEditors 5 -numberOfAdmins 1 -numberOfApps 10 -multiTenant false -defaultTenant alfresco.com
	 * 
	 * example:
	 * 
	 * -f johndoe.lic -h John Doe Ltd -v 1.0ent -s 20140401 -e 20150401 -numberOfLicenses 2
	 * 
	 * The file name (-file or -f), customer name (-holder or -h), application version (-version or -v) and end date (-end or -e) are required.
	 * The default values of the other parameters are:
	 * 
	 * - valid from --> today
	 * - numberOfLicenses --> 1
	 * - numberOfProcesses --> 150
	 * - numberOfEditors --> 5 
	 * - numberOfAdmins --> 1
	 * - numberOfApps --> 0 (unlimited)
	 * - multiTenant --> false
	 * - defaultTenant --> test
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 0)
        {
            displayHelp();
            return;
        }
		
		LicenseManager licenseManager = new LicenseManager();
		if (args.length == 2 && "dump".equals(args[0])) {
			License license = licenseManager.loadLicenseFromFilePath(args[1]);
			System.out.println(licenseManager.getLicenseContent(license));
			return;
		}
		
		if (args.length == 2 && "publickey".equals(args[0])) {
            License license = licenseManager.loadLicenseFromFilePath(args[1]);
            System.out.println(license.dumpPublicKeyRingDigest());
            return;
        }
		
		LicenseInfo licenseInfo = null;
		try {
			licenseInfo = processArgs(args);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			return;
		}
		
		try {
			licenseManager.createLicense(licenseInfo, licenseInfo.getFileName());
		} catch(Exception e) {
			System.out.println("Error creating license: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		System.out.println("License " + licenseInfo.getFileName() + " created");
	}
	
	private static void displayHelp() {
        System.out.println("USAGE: java -jar license.jar [-f <file name>] [-h <holder name>] [-v <version>] [-s <start date>] [-e <end date>] [-numberOfLicenses <number of licenses>] " + 
                "[-numberOfProcesses <number of processes>] [-numberOfEditors <number of editors>] [-numberOfAdmins <number of admins>] [-multiTenant <true or false>] [-defaultTenant <tenant name>]");
    }
	
	private static LicenseInfo processArgs(String args[]) {
		LicenseInfo licenseInfo = new LicenseInfo();
		for (int i = 0; i < args.length; i++) {
			
			if ("-f".equals(args[i]) || "-file".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <file name> for the option -file or -f must be specified");
                }
                licenseInfo.setFileName(args[i]);
			
			} else if ("-h".equals(args[i]) || "-holder".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <holder name> for the option -holder or -h must be specified");
                }
                licenseInfo.setHolder(args[i]);
			
			} else if ("-v".equals(args[i]) || "-version".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <version> for the option -version or -v must be specified");
                }
                licenseInfo.setProductKey(args[i]);
			
			} else if ("-s".equals(args[i]) || "-start".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <start date> for the option -start or -s must be specified");
                }
                licenseInfo.setGoodAfterDate(convertStringToDate(args[i]));
			
			} else if ("-e".equals(args[i]) || "-end".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <end date> for the option -end or -e must be specified");
                }
                licenseInfo.setGoodBeforeDate(convertStringToDate(args[i]));
			
			} else if ("-numberOfLicenses".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <number of licenses> for the option -numberOfLicenses must be specified");
                }
                licenseInfo.getFeatureInfo().setNumberOfLicenses(convertStringToInteger(args[i]));
			
			} else if ("-numberOfProcesses".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <number of processes> for the option -numberOfProcesses must be specified");
                }
                licenseInfo.getFeatureInfo().setNumberOfProcesses(convertStringToInteger(args[i]));
			
			} else if ("-numberOfEditors".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <number of editors> for the option -numberOfEditors must be specified");
                }
                licenseInfo.getFeatureInfo().setNumberOfEditors(convertStringToInteger(args[i]));
			
			} else if ("-numberOfAdmins".equals(args[i])) {
				
				if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <number of admins> for the option -numberOfAdmins must be specified");
                }
                licenseInfo.getFeatureInfo().setNumberOfAdmins(convertStringToInteger(args[i]));
                
			} else if ("-numberOfApps".equals(args[i])) {
                
                if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <number of apps> for the option -numberOfApps must be specified");
                }
                licenseInfo.getFeatureInfo().setNumberOfApps(convertStringToInteger(args[i]));
			
			} else if ("-multiTenant".equals(args[i])) {
                
                if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <multi tenant> for the option -multiTenant must be specified");
                }
                licenseInfo.getFeatureInfo().setMultiTenant(convertStringToBoolean(args[i]));
            
			} else if ("-defaultTenant".equals(args[i])) {
                
                if (++i == args.length || args[i].length() == 0) {
                    throw new LicenseException("The value <default tenant> for the option -defaultTenant must be specified");
                }
                licenseInfo.getFeatureInfo().setDefaultTenant(args[i]);
            }
		}
		
		if (StringUtils.isEmpty(licenseInfo.getFileName())) {
			throw new LicenseException("A value <file name> for the option -file or -f must be specified");
		}
		
		if (StringUtils.isEmpty(licenseInfo.getHolder())) {
			throw new LicenseException("A value <holder name> for the option -holder or -h must be specified");
		}
		
		if (StringUtils.isEmpty(licenseInfo.getProductKey())) {
			throw new LicenseException("A value <version> for the option -version or -v must be specified");
		}
		
		if (licenseInfo.getGoodBeforeDate() == null) {
			throw new LicenseException("A value <end date> for the option -end or -e must be specified");
		}
		
		if (licenseInfo.getGoodAfterDate() == null) {
		    GregorianCalendar todayCal = new GregorianCalendar();
		    todayCal.add(Calendar.DAY_OF_YEAR, -1);
			licenseInfo.setGoodAfterDate(todayCal.getTime());
		}
		
		return licenseInfo;
	}

	private static Date convertStringToDate(String dateString) {
		try {
			return dateFormat.parse(dateString);
		} catch (Exception e) {
			throw new LicenseException("Expected date in format yyyyMMdd, but encountered " + dateString, e); 
		}
	}
	
	private static Integer convertStringToInteger(String integerString) {
		if (NumberUtils.isNumber(integerString) == false) {
			throw new LicenseException("Expected number, but encountered " + integerString); 
		}
		
		int number = Integer.valueOf(integerString);
		if (number < 1) {
			throw new LicenseException("Expected a number equal to 1 or higher, but encountered " + integerString); 
		}
		
		return number;
	}
	
	private static boolean convertStringToBoolean(String booleanString) {
        boolean value = Boolean.valueOf(booleanString);
        return value;
    }
}
