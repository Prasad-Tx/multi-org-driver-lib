package com.ubs.orgz.organization_datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "organizations")
public class DatasourceProperties {

    private Map<String, DataSourceProperties> datasources = new HashMap<>();

    public Map<String, DataSourceProperties> getDatasources() {
        return datasources;
    }

    public void setDatasources(Map<String, DataSourceProperties> datasources) {
        this.datasources = datasources;
    }

    public static class DataSourceProperties {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private HikariProperties hikari = new HikariProperties();

        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public HikariProperties getHikari() {
            return hikari;
        }

        public void setHikari(HikariProperties hikari) {
            this.hikari = hikari;
        }
    }

    public static class HikariProperties {
        // Pool sizing
        private Integer maximumPoolSize = 20;
        private Integer minimumIdle = 5;

        // Connection timeout settings
        private Long connectionTimeout = 30000L;        // 30 seconds
        private Long validationTimeout = 5000L;        // 5 seconds

        // Connection lifecycle
        private Long maxLifetime = 1800000L;            // 30 minutes
        private Long idleTimeout = 600000L;             // 10 minutes

        // Connection validation
        private String connectionTestQuery = "SELECT 1";
        private String connectionInitSql = "SET timezone = 'UTC'";

        // Connection keepalive (NEW - Critical for network resilience)
        private Long keepaliveTime = 300000L;           // 5 minutes

        // Leak detection
        private Long leakDetectionThreshold = 60000L;   // 60 seconds

        // Performance tuning
        private Long initializationFailTimeout = 1L;
        private Boolean allowPoolSuspension = false;
        private Boolean readOnly = false;
        private Boolean autoCommit = false;

        // Monitoring
        private Boolean registerMbeans = true;

        // Health check properties
        private Map<String, Object> healthCheckProperties = new HashMap<>();

        // Getters and Setters
        public Integer getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(Integer maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public Integer getMinimumIdle() {
            return minimumIdle;
        }

        public void setMinimumIdle(Integer minimumIdle) {
            this.minimumIdle = minimumIdle;
        }

        public Long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Long getValidationTimeout() {
            return validationTimeout;
        }

        public void setValidationTimeout(Long validationTimeout) {
            this.validationTimeout = validationTimeout;
        }

        public Long getMaxLifetime() {
            return maxLifetime;
        }

        public void setMaxLifetime(Long maxLifetime) {
            this.maxLifetime = maxLifetime;
        }

        public Long getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(Long idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public String getConnectionTestQuery() {
            return connectionTestQuery;
        }

        public void setConnectionTestQuery(String connectionTestQuery) {
            this.connectionTestQuery = connectionTestQuery;
        }

        public String getConnectionInitSql() {
            return connectionInitSql;
        }

        public void setConnectionInitSql(String connectionInitSql) {
            this.connectionInitSql = connectionInitSql;
        }

        public Long getKeepaliveTime() {
            return keepaliveTime;
        }

        public void setKeepaliveTime(Long keepaliveTime) {
            this.keepaliveTime = keepaliveTime;
        }

        public Long getLeakDetectionThreshold() {
            return leakDetectionThreshold;
        }

        public void setLeakDetectionThreshold(Long leakDetectionThreshold) {
            this.leakDetectionThreshold = leakDetectionThreshold;
        }

        public Long getInitializationFailTimeout() {
            return initializationFailTimeout;
        }

        public void setInitializationFailTimeout(Long initializationFailTimeout) {
            this.initializationFailTimeout = initializationFailTimeout;
        }

        public Boolean getAllowPoolSuspension() {
            return allowPoolSuspension;
        }

        public void setAllowPoolSuspension(Boolean allowPoolSuspension) {
            this.allowPoolSuspension = allowPoolSuspension;
        }

        public Boolean getReadOnly() {
            return readOnly;
        }

        public void setReadOnly(Boolean readOnly) {
            this.readOnly = readOnly;
        }

        public Boolean getAutoCommit() {
            return autoCommit;
        }

        public void setAutoCommit(Boolean autoCommit) {
            this.autoCommit = autoCommit;
        }

        public Boolean getRegisterMbeans() {
            return registerMbeans;
        }

        public void setRegisterMbeans(Boolean registerMbeans) {
            this.registerMbeans = registerMbeans;
        }

        public Map<String, Object> getHealthCheckProperties() {
            return healthCheckProperties;
        }

        public void setHealthCheckProperties(Map<String, Object> healthCheckProperties) {
            this.healthCheckProperties = healthCheckProperties;
        }
    }
}
