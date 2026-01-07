package com.ubs.orgz.organization_datasource;

import com.ubs.orgz.organization.OrganizationContext;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class LiquibaseMultiOrganizationConfig {

    private final String ORG_PC = "{ORGZ}";

    @Value("${multi-org.liquibase.change-log:/db/changelog/" +ORG_PC+ "/db.changelog-master.xml}")
    private String changeLog;

    @Value("${multi-org.liquibase.change-log-admin:/db/changelog/org_admin/db.changelog-master.xml}")
    private String changeLogAdmin;

    private final com.ubs.orgz.organization_datasource.DatasourceProperties datasourceProperties;
    private final ResourceLoader resourceLoader;

    public LiquibaseMultiOrganizationConfig(DatasourceProperties datasourceProperties, ResourceLoader resourceLoader) {
        this.datasourceProperties = datasourceProperties;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void runLiquibasePerTenant() {
        datasourceProperties.getDatasources().forEach((organizationId, props) -> {
            DataSource ds = DataSourceBuilder.create()
                    .driverClassName(props.getDriverClassName())
                    .url(props.getUrl())
                    .username(props.getUsername())
                    .password(props.getPassword())
                    .build();

            OrganizationContext.setOrganization(organizationId);
            
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setResourceLoader(resourceLoader);
            liquibase.setDataSource(ds);
            if (OrganizationContext.BOOTSTRAP_ORG.equals(organizationId)) {
                liquibase.setChangeLog("classpath:" + changeLogAdmin);
            }
            else {
                liquibase.setChangeLog("classpath:" + changeLog.replace(ORG_PC, organizationId));
            }
            liquibase.setShouldRun(true);
            try {
                liquibase.afterPropertiesSet();
            } catch (Exception e) {
                throw new IllegalStateException("Liquibase failed for organization: " + organizationId, e);
            } finally {
                OrganizationContext.clear();
            }
        });
    }
}
