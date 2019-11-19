/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client.modelinfo.bpmn;

import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;

import com.activiti.web.rest.client.modelinfo.AbstractInfoMapper;

public class ServiceTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		ServiceTask serviceTask = (ServiceTask) element;
		if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Class", serviceTask.getImplementation());
		} else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Expression", serviceTask.getImplementation());
		} else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Delegate expression", serviceTask.getImplementation());
		}
		createPropertyNode("Result variable name", serviceTask.getResultVariableName());
	}
}
