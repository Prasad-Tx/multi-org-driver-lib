package com.ubs.orgz.organization_profile;

import com.ubs.commons.exception.GearsException;
import com.ubs.commons.exception.GearsResponseStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class OrganizationProfileResolver {

    private final ApplicationContext context;

    public OrganizationProfileResolver(ApplicationContext context) {
        this.context = context;
    }

    public <T> T resolve(String organizationId, Class<T> serviceType) {
        String beanName = ProfileRegistry.getBean(organizationId, serviceType.getSimpleName());
        try {
            return context.getBean(beanName, serviceType);
        }
        catch (Exception e) {
            throw new GearsException(GearsResponseStatus.INTERNAL_ERROR, e.getMessage());
        }
    }
}
