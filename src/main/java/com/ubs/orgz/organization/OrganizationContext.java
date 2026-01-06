package com.ubs.orgz.organization;

public class OrganizationContext {

    private static final ThreadLocal<String> CURRENT_ORG = new ThreadLocal<>();
    public static final String BOOTSTRAP_ORG = "org_admin";

    private OrganizationContext() {}

    public static void setOrganization(String tenantId) {
        CURRENT_ORG.set(tenantId);
    }

    public static String getOrganization() {
        return CURRENT_ORG.get();
    }

    public static void clear() {
        CURRENT_ORG.remove();
    }
}
