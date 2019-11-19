/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.license;

public class LicenseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LicenseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public LicenseException(String message) {
		super(message);
	}
}
