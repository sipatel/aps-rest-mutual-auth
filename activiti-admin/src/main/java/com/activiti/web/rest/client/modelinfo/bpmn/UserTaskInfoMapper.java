/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.web.rest.client.modelinfo.bpmn;

import org.activiti.bpmn.model.UserTask;

import com.activiti.web.rest.client.modelinfo.AbstractInfoMapper;

public class UserTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		UserTask userTask = (UserTask) element;
		createPropertyNode("Assignee", userTask.getAssignee());
		createPropertyNode("Candidate users", userTask.getCandidateUsers());
		createPropertyNode("Candidate groups", userTask.getCandidateGroups());
		createPropertyNode("Due date", userTask.getDueDate());
		createPropertyNode("Form key", userTask.getFormKey());
		createPropertyNode("Priority", userTask.getPriority());
	}
}
