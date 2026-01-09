package com.ubs.orgz.organization_datasource;

import com.ubs.orgz.organization.MasterStoreRegistry;
import com.ubs.orgz.config.OrgProfileProperties;
import com.ubs.orgz.config.OrgDatasourceProperties;
import com.ubs.orgz.organization.OrganizationContext;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class LiquibaseMultiOrganizationConfig {

    private final String ORG_PLACEHOLDER = "{ORGZ}";
    private final OrgProfileProperties orgProfileProperties;
    private final OrgDatasourceProperties orgDatasourceProperties;
    private final ResourceLoader resourceLoader;

    public LiquibaseMultiOrganizationConfig(OrgProfileProperties orgProfileProperties,
                                            OrgDatasourceProperties orgDatasourceProperties,
                                            ResourceLoader resourceLoader) {
        this.orgProfileProperties = orgProfileProperties;
        this.orgDatasourceProperties = orgDatasourceProperties;
        this.resourceLoader = resourceLoader;
    }

    private DataSource getDataSource(OrgDatasourceProperties.DataSourceProperties dataSourceProp) {
        return DataSourceBuilder.create()
                .driverClassName(dataSourceProp.getDriverClassName())
                .url(dataSourceProp.getUrl())
                .username(dataSourceProp.getUsername())
                .password(dataSourceProp.getPassword())
                .build();
    }

    private void validate() {
        Objects.requireNonNull(orgDatasourceProperties, "orgDatasourceProperties");
        Objects.requireNonNull(orgProfileProperties, "orgProfileProperties");

        if (orgDatasourceProperties.getDatasources() == null || orgDatasourceProperties.getDatasources().isEmpty()) {
            throw new IllegalStateException("No organization datasources configured under 'organizations.datasources'");
        }
        if (orgProfileProperties.getLiquibase() == null) {
            throw new IllegalStateException("organizations.admin.liquibase is not configured");
        }
        if (orgProfileProperties.getMode() == null) {
            throw new IllegalStateException("organizations.admin.mode is not configured");
        }
    }

    @PostConstruct
    public void runLiquibasePerTenant() {
        validate();
        AtomicBoolean adminMigrated = new AtomicBoolean(false);
        Set<String> orgsDsLoaded = new HashSet<>();

        orgDatasourceProperties.getDatasources().forEach((organizationId, dsProp) -> {
            if (organizationId == null || organizationId.isBlank()) {
                throw new IllegalStateException("Organization id must not be blank");
            }

            // Skip admin liquibase if mode is JSON (admin DB not required)
            if (OrganizationContext.ORGZ_ADMIN.equals(organizationId)
                    && orgProfileProperties.getMode() != OrgProfileProperties.AdminMode.ADMIN_DB) {
                return;
            }

            OrganizationContext.setOrganization(organizationId);
            try (HikariDataSource ds = createLiquibaseDataSource(dsProp, organizationId)) {
                SpringLiquibase liquibase = new SpringLiquibase();
                liquibase.setResourceLoader(resourceLoader);
                liquibase.setDataSource(ds);

                String changelog;
                if (OrganizationContext.ORGZ_ADMIN.equals(organizationId)) {
                    changelog = "classpath:" + orgProfileProperties.getLiquibase().getChangeLogAdmin();
                } else {
                    changelog = "classpath:" + orgProfileProperties.getLiquibase().getChangeLog()
                            .replace(ORG_PLACEHOLDER, organizationId);
                }

                liquibase.setChangeLog(changelog);
                liquibase.setShouldRun(true);

                liquibase.afterPropertiesSet();

                orgsDsLoaded.add(organizationId);
                if (OrganizationContext.ORGZ_ADMIN.equals(organizationId)) {
                    adminMigrated.set(true);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Liquibase failed for organization: " + organizationId, e);
            } finally {
                OrganizationContext.clear();
            }
        });

        if (orgProfileProperties.getMode() == OrgProfileProperties.AdminMode.ADMIN_DB && !adminMigrated.get()) {
            throw new IllegalStateException("Admin datasource not migrated but mode=ADMIN_DB");
        }
        MasterStoreRegistry.setOrgList(orgsDsLoaded);
    }

    private HikariDataSource createLiquibaseDataSource(OrgDatasourceProperties.DataSourceProperties p, String orgId) {
        if (p == null) {
            throw new IllegalStateException("Datasource properties missing for org: " + orgId);
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName(p.getDriverClassName());
        cfg.setJdbcUrl(p.getUrl());
        cfg.setUsername(p.getUsername());
        cfg.setPassword(p.getPassword());

        // Liquibase does not need large pools; keep it small and close after use.
        cfg.setMaximumPoolSize(2);
        cfg.setMinimumIdle(0);
        cfg.setPoolName("liquibase-" + orgId);

        return new HikariDataSource(cfg);
    }

}
