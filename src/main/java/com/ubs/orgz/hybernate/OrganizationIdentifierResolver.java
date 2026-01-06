package com.ubs.orgz.hybernate;

import com.ubs.orgz.organization.OrganizationContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class OrganizationIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static final String DEFAULT_ORG = "default";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String org = OrganizationContext.getOrganization();
        return (org != null) ? org : DEFAULT_ORG;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
