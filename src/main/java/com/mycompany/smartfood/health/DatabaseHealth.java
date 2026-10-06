package com.mycompany.smartfood.health;

import java.sql.Connection;

import javax.sql.DataSource;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;

@Readiness
@ApplicationScoped
public class DatabaseHealth implements HealthCheck {

    @Resource(lookup = "java:/PostgresDS")
    private DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return HealthCheckResponse.up("database");
            }
        } catch (Exception ignored) {
            return HealthCheckResponse.down("database");
        }
        return HealthCheckResponse.down("database");
    }
}
