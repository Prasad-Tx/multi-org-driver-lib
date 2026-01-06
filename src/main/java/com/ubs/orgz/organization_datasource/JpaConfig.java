package com.ubs.orgz.organization_datasource;

import com.ubs.orgz.hybernate.MultiOrganizationConnectionProvider;
import com.ubs.orgz.hybernate.OrganizationIdentifierResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@EnableJpaRepositories(
        basePackages = "com.ubs",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@Configuration
public class JpaConfig {

    @Bean
    @Lazy
    public MultiOrganizationConnectionProvider multiTenantConnectionProvider(
            DatasourceProperties orgProps) {
        return new MultiOrganizationConnectionProvider(orgProps);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Lazy MultiOrganizationConnectionProvider provider) {

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.multiTenancy", org.hibernate.MultiTenancyStrategy.DATABASE);
        jpaProps.put("hibernate.multi_tenant_connection_provider", provider);
        jpaProps.put("hibernate.tenant_identifier_resolver", new OrganizationIdentifierResolver());
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProps.put("hibernate.hbm2ddl.auto", "none");

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPackagesToScan("com.ubs");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(jpaProps);

        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
