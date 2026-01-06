package com.ubs.orgz.hybernate;

import com.ubs.orgz.organization_datasource.DatasourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiOrganizationConnectionProvider
        implements MultiTenantConnectionProvider {

    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();

    public MultiOrganizationConnectionProvider(DatasourceProperties props) {
        props.getDatasources().forEach((orgId, cfg) -> {
            dataSources.put(orgId, createDataSource(cfg, orgId));
        });
    }

    private HikariDataSource createDataSource(
            DatasourceProperties.DataSourceProperties p, String orgId) {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(p.getUrl());
        config.setUsername(p.getUsername());
        config.setPassword(p.getPassword());
        config.setDriverClassName(p.getDriverClassName());

        if (p.getHikari() != null) {
            config.setMaximumPoolSize(p.getHikari().getMaximumPoolSize());
            config.setMinimumIdle(p.getHikari().getMinimumIdle());
            config.setConnectionTimeout(p.getHikari().getConnectionTimeout());
            config.setIdleTimeout(p.getHikari().getIdleTimeout());
            config.setMaxLifetime(p.getHikari().getMaxLifetime());
        }

        config.setPoolName("org-" + orgId);
        config.setAutoCommit(false);

        return new HikariDataSource(config);
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSources.values().iterator().next().getConnection();
    }

    @Override
    public Connection getConnection(String organizationId) throws SQLException {
        HikariDataSource ds = dataSources.get(organizationId);
        if (ds == null) {
            throw new IllegalArgumentException("Unknown organization: " + organizationId);
        }
        return ds.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public void releaseConnection(String organizationId, Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    @PreDestroy
    public void shutdown() {
        dataSources.values().forEach(HikariDataSource::close);
    }
}