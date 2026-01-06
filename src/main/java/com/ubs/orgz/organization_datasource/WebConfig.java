package com.ubs.orgz.organization_datasource;

import com.ubs.orgz.organization.OrganizationFilter;
import com.ubs.orgz.organization_profile.OrganizationProfileProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class WebConfig {

    private final OrganizationProfileProcessor profileProcessor;

    public WebConfig(@Lazy OrganizationProfileProcessor profileProcessor) {
        this.profileProcessor = profileProcessor;
    }

    @Bean
    public ThreadPoolTaskExecutor profileReloadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(5);
        executor.setThreadNamePrefix("profile-reload-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    public FilterRegistrationBean<OrganizationFilter> organizationFilter() {
        FilterRegistrationBean<OrganizationFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(
                new OrganizationFilter(
                        profileProcessor,
                        profileReloadExecutor()
                )
        );

        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setAsyncSupported(true);

        return registration;
    }
}
