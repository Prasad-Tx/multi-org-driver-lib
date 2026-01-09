package com.ubs.orgz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "organizations")
@Getter @Setter
public class OrgDatasourceProperties {

    /**** Map of instances yaml > organizations.datasource ****/
    private Map<String, DataSourceProperties> datasources = new HashMap<>();

    @Getter @Setter
    public static class DataSourceProperties {
        private String driverClassName;
        private String url;
        private String username;
        private String password;

        /**** map yaml > organizations.datasources.hikari ****/
        private HikariProperties hikari = new HikariProperties();
    }

    @Getter @Setter
    public static class HikariProperties {

        private Integer maximumPoolSize = 20;
        private Integer minimumIdle = 5;
        private Long connectionTimeout = 30000L;
        private Long validationTimeout = 5000L;
        private Long maxLifetime = 1800000L;
        private Long idleTimeout = 600000L;
    }
}
