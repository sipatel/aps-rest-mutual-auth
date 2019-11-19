/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.conf.metrics;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.codahale.metrics.health.HealthCheck;

/**
 * Metrics HealthCheck for the Database.
 */
public class DatabaseHealthCheck extends HealthCheck {

    private final Logger log = LoggerFactory.getLogger(HealthCheck.class);

    private JdbcTemplate jdbcTemplate;

    public DatabaseHealthCheck(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Result check() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Result.healthy();
        } catch (Exception e) {
            log.debug("Cannot connect to Database: {}", e);
            return Result.unhealthy("Cannot connect to Database : " + e.getMessage());
        }
    }
}
