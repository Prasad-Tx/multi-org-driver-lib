package com.ubs.orgz.hybernate;

import com.ubs.orgz.organization.OrganizationContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class OrganizationIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String org = OrganizationContext.getOrganization();
        return (org != null) ? org : OrganizationContext.ORGZ_ADMIN;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
