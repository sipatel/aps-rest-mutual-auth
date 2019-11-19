/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public class DbScriptUtil {

    public static void main(String... args) throws Exception {
        dropSchema();
    }

    public static void dropSchema() throws Exception {
        System.out.println("Dropping schema");
        DatabaseConnection databaseConnection = createDbConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);

        Liquibase liquibase = new Liquibase("META-INF/liquibase/db-changelog.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.dropAll();

        closeDatabase(database, databaseConnection);
    }

    protected static DatabaseConnection createDbConnection() throws Exception {
        Properties properties = new Properties();
        properties.load(DbScriptUtil.class.getClassLoader().getResourceAsStream("META-INF/activiti-admin/TEST-db.properties"));
        Connection connection = DriverManager.getConnection(properties.getProperty("datasource.url"),
                properties.getProperty("datasource.username"), properties.getProperty("datasource.password"));
        DatabaseConnection databaseConnection = new JdbcConnection(connection);
        return databaseConnection;
    }

    protected static void closeDatabase(Database database, DatabaseConnection databaseConnection) {
        try {
            database.close();
            databaseConnection.close();
        } catch (Exception e) {
            System.out.println("Error closing db connection " + e.getMessage());
        }
    }
}
