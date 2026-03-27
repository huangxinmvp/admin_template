package com.hiking.treasure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
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
import java.util.Objects;

@Slf4j
@Configuration
public class TemplateSystemBootstrapConfig {

    private static final String CONFIG_BOOTSTRAP_SQL = "static/sql/bootstrap-system-config.sql";
    private static final String SESSION_BOOTSTRAP_SQL = "static/sql/bootstrap-user-session.sql";
    private static final String PERMISSION_BOOTSTRAP_SQL = "static/sql/bootstrap-template-permissions.sql";
    private static final String TENANT_TABLE = "sys_tenant";
    private static final String ROLE_TABLE = "sys_role";
    private static final String PERMISSION_TABLE = "sys_permission";

    @Bean
    public ApplicationRunner templateSystemBootstrapRunner(DataSource dataSource) {
        return args -> bootstrap(dataSource);
    }

    private void bootstrap(DataSource dataSource) throws SQLException {
        runScript(dataSource, SESSION_BOOTSTRAP_SQL);
        runScript(dataSource, CONFIG_BOOTSTRAP_SQL);
        if (!tableExists(dataSource, TENANT_TABLE)
                || !tableExists(dataSource, ROLE_TABLE)
                || !tableExists(dataSource, PERMISSION_TABLE)) {
            log.info("Skip template permission bootstrap because core RBAC tables are not ready");
            return;
        }
        runScript(dataSource, PERMISSION_BOOTSTRAP_SQL);
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

    private void runScript(DataSource dataSource, String classpathLocation) {
        try {
            log.info("Bootstrap SaaS template enhancements using {}", classpathLocation);
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(false);
            populator.setIgnoreFailedDrops(true);
            populator.addScript(new ClassPathResource(classpathLocation));
            DatabasePopulatorUtils.execute(populator, dataSource);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Bootstrap SaaS template enhancements failed for " + Objects.toString(classpathLocation),
                    ex
            );
        }
    }
}
