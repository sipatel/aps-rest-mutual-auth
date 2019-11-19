/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.activiti.domain.ClusterConfig;

/**
 * Spring Data JPA repository for the {@link ClusterConfig} entity.
 */
public interface ClusterConfigRepository extends JpaRepository<ClusterConfig, Long> {

    ClusterConfig findByClusterName(String clusterName);
    
    List<ClusterConfig> findByCreatedByLoginOrderByClusterNameAsc(String createdBy);
    
    List<ClusterConfig> findByClusterNameAndCreatedByLogin(String clusterName, String createdBy);
    
    List<ClusterConfig> findByCreatedByLoginAndUserLogin(String createdBy, String userLogin);
    
    Long countByClusterNameAndCreatedByLogin(String clusterName, String createdBy);
    
    Long countByCreatedByLoginAndUserLogin(String createdBy, String userLogin);
    
    Long countByClusterName(String clusterName);

}
