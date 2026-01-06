package com.ubs.orgz.organization_datasource;

import com.ubs.orgz.organization.OrganizationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

public class StartupOrganizationCleaner implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        OrganizationContext.clear();
    }
}
