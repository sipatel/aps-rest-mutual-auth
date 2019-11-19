/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.activiti.domain.ServerConfig;

/**
 * Spring Data JPA repository for the ServerConfig entity.
 */
public interface ServerConfigRepository extends JpaRepository<ServerConfig, Long> {
	
	List<ServerConfig> findByClusterConfigId(Long clusterConfigId);
	
	List<ServerConfig> findByClusterConfigCreatedByLogin(String login);
	
	List<ServerConfig> findByClusterConfigIdAndClusterConfigCreatedByLogin(Long clusterConfigId, String login);
	
	ServerConfig findByIdAndClusterConfigCreatedByLogin(Long serverConfigId, String login);

}
