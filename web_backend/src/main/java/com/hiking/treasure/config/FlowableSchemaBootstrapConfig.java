package com.hiking.treasure.config;

import lombok.extern.slf4j.Slf4j;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Configuration
public class FlowableSchemaBootstrapConfig {

    private static final String FLOWABLE_COMMON_TABLE = "ACT_GE_PROPERTY";
    private static final String FLOWABLE_ENGINE_TABLE = "ACT_RE_DEPLOYMENT";
    private static final String FLOWABLE_HISTORY_TABLE = "ACT_HI_PROCINST";

    private static final String FLOWABLE_COMMON_SQL = "org/flowable/common/db/create/flowable.mysql.create.common.sql";
    private static final String FLOWABLE_ENGINE_SQL = "org/flowable/db/create/flowable.mysql.create.engine.sql";
    private static final String FLOWABLE_HISTORY_SQL = "org/flowable/db/create/flowable.mysql.create.history.sql";

    @Bean
    public ProcessEngineConfigurationConfigurer flowableProcessEngineConfigurationConfigurer(DataSource dataSource) {
        return configuration -> bootstrapSchemaIfNeeded(configuration, dataSource);
    }

    private void bootstrapSchemaIfNeeded(SpringProcessEngineConfiguration configuration, DataSource dataSource) {
        String databaseType = configuration.getDatabaseType();
        if (databaseType != null && !"mysql".equalsIgnoreCase(databaseType)) {
            log.info("Skip Flowable schema bootstrap because database type is {}", databaseType);
            return;
        }

        try {
            if (!tableExists(dataSource, FLOWABLE_COMMON_TABLE)) {
                runScript(dataSource, FLOWABLE_COMMON_SQL, "Flowable common");
            }
            if (!tableExists(dataSource, FLOWABLE_ENGINE_TABLE)) {
                runScript(dataSource, FLOWABLE_ENGINE_SQL, "Flowable engine");
            }
            if (!tableExists(dataSource, FLOWABLE_HISTORY_TABLE)) {
                runScript(dataSource, FLOWABLE_HISTORY_SQL, "Flowable history");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Bootstrap Flowable schema failed", ex);
        }
    }

    private boolean tableExists(DataSource dataSource, String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            return lookupTable(metaData, catalog, tableName)
                    || lookupTable(metaData, catalog, tableName.toLowerCase())
                    || lookupTable(metaData, catalog, tableName.toUpperCase());
        }
    }

    private boolean lookupTable(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    private void runScript(DataSource dataSource, String classpathLocation, String label) {
        log.info("Bootstrap {} schema using {}", label, classpathLocation);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(true);
        populator.addScript(new ClassPathResource(classpathLocation));
        DatabasePopulatorUtils.execute(populator, dataSource);
    }
}
