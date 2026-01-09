package com.ubs.orgz.organization;

public final class OrganizationContext {

    private static final ThreadLocal<String> CURRENT_ORG = new ThreadLocal<>();

    public static final String ORGZ_ADMIN = "orgz_admin";

    private OrganizationContext() {
    }

    public static void setOrganization(String tenantId) {
        if (tenantId == null) {
            CURRENT_ORG.remove();
            return;
        }
        String tenantIdLocal = tenantId.trim();
        if (tenantIdLocal.isEmpty()) {
            CURRENT_ORG.remove();
            return;
        }
        CURRENT_ORG.set(tenantIdLocal);
    }

    public static String getOrganization() {
        return CURRENT_ORG.get();
    }

    public static void clear() {
        CURRENT_ORG.remove();
    }
}