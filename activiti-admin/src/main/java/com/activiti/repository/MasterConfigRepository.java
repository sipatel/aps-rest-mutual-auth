/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.activiti.domain.MasterConfiguration;

/**
 * Spring Data JPA repository for the ServerConfig entity.
 */
public interface MasterConfigRepository extends JpaRepository<MasterConfiguration, Long> {

	MasterConfiguration findByClusterConfigId(Long clusterConfigId);

}
