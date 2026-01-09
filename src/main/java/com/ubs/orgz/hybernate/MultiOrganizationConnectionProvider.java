package com.ubs.orgz.hybernate;

import com.ubs.orgz.config.OrgDatasourceProperties;
import com.ubs.orgz.organization.OrganizationContext;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MultiOrganizationConnectionProvider implements MultiTenantConnectionProvider {

    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();

    /**
     * Initializes a MultiOrganizationConnectionProvider by configuring data sources for multiple organizations
     * using the provided datasource properties.
     *
     * @param props The properties containing datasource configurations for multiple organizations
     *              as defined in the application configuration file.
     */
    public MultiOrganizationConnectionProvider(OrgDatasourceProperties props) {
        Objects.requireNonNull(props, "OrgDatasourceProperties must not be null");

        if (props.getDatasources() == null || props.getDatasources().isEmpty()) {
            throw new IllegalStateException("No organization datasources configured under 'organizations.datasources'");
        }

        props.getDatasources().forEach((orgId, dsp) -> {
            if (orgId == null || orgId.isBlank()) {
                throw new IllegalStateException("Organization id must not be blank");
            }
            dataSources.put(orgId, createDataSource(dsp, dsp.getHikari(), orgId));
        });
    }

    /**
     * Creates and configures a HikariDataSource instance based on the provided datasource properties,
     * Hikari connection pool properties, and the organization identifier.
     *
     * @param p The datasource properties for the organization.
     * @param h The Hikari connection pool properties.
     * @param orgId The unique identifier for the organization, used to name the connection pool.
     *
     * @return A configured {@link HikariDataSource} instance, initialized with the specified properties.
     */
    private HikariDataSource createDataSource(
            OrgDatasourceProperties.DataSourceProperties p,
            OrgDatasourceProperties.HikariProperties h,
            String orgId) {
        if (p == null) {
            throw new IllegalStateException("Datasource properties missing for org: " + orgId);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(p.getUrl());
        config.setUsername(p.getUsername());
        config.setPassword(p.getPassword());
        config.setDriverClassName(p.getDriverClassName());

        if (h != null) {
            if (h.getMaximumPoolSize() != null) config.setMaximumPoolSize(h.getMaximumPoolSize());
            if (h.getMinimumIdle() != null) config.setMinimumIdle(h.getMinimumIdle());
            if (h.getConnectionTimeout() != null) config.setConnectionTimeout(h.getConnectionTimeout());
            if (h.getIdleTimeout() != null) config.setIdleTimeout(h.getIdleTimeout());
            if (h.getMaxLifetime() != null) config.setMaxLifetime(h.getMaxLifetime());
            if (h.getValidationTimeout() != null) config.setValidationTimeout(h.getValidationTimeout());
        }

        config.setPoolName("org-" + orgId);
        //config.setAutoCommit(false);
        return new HikariDataSource(config);
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        HikariDataSource admin = dataSources.get(OrganizationContext.ORGZ_ADMIN);
        if (admin != null) {
            return admin.getConnection();
        }
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
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void releaseConnection(String organizationId, Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return unwrapType != null && unwrapType.isInstance(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> unwrapType) {
        if (isUnwrappableAs(unwrapType)) {
            return (T) this;
        }
        throw new IllegalArgumentException("Not unwrappable as " + unwrapType);
    }

    @PreDestroy
    public void shutdown() {
        dataSources.values().forEach(ds -> {
            try {
                ds.close();
            } catch (Exception ignored) {
                ; // ignore
            }
        });
    }
}