package com.hiking.treasure.service.impl;

import com.hiking.treasure.domain.vo.system.SystemHealthVO;
import com.hiking.treasure.service.SystemHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemHealthServiceImpl implements SystemHealthService {

    private final DataSource dataSource;

    @Value("${spring.application.name:admin-template}")
    private String applicationName;

    @Override
    public SystemHealthVO getHealth() {
        boolean databaseReady = isDatabaseReady();
        SystemHealthVO vo = new SystemHealthVO();
        vo.setStatus(databaseReady ? "ok" : "degraded");
        vo.setApplication(applicationName);
        vo.setDatabaseReady(databaseReady);
        vo.setTimestamp(System.currentTimeMillis());
        return vo;
    }

    private boolean isDatabaseReady() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            return resultSet.next();
        } catch (SQLException error) {
            log.warn("System health database probe failed: {}", error.getMessage());
            return false;
        }
    }
}
