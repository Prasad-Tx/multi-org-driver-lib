package com.ubs.orgz.organization;

import com.ubs.commons.exception.GearsException;
import com.ubs.commons.exception.GearsResponseStatus;
import com.ubs.orgz.organization_profile.OrgProfileEntityType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class MasterStoreRegistry {

    private static final AtomicReference<MasterStore> MASTER_STORE =
            new AtomicReference<>(new MasterStore());

    private MasterStoreRegistry() {
    }

    private static MasterStore get() {
        MasterStore masterStore = MASTER_STORE.get();
        if (masterStore == null) {
            throw new GearsException(GearsResponseStatus.INTERNAL_ERROR, "MasterStore not initialized yet");
        }
        return masterStore;
    }

    /**** setters  ****/
    public static void setProfiles(Map<String, String> profiles) {
        get().setProfiles(profiles);
    }

    public static void setOrgList(Set<String> orglist) {
        get().setOrgList(orglist);
    }

    /**** swap organizations + profiles together ****/
    public static void replaceAll(Set<String> organizations, Map<String, String> profiles) {
        get().replaceAll(organizations, profiles);
    }

    public static boolean isReady() {
        return !getOrgList().isEmpty();
    }

    public static boolean isOrganizationUnknown(String orgId) {
        return !get().isOrgInList(orgId);
    }

    public static Set<String> getOrgList() {
        Set<String> orglist = get().getOrgList();
        if (orglist == null || orglist.isEmpty()) {
            return Set.of();
        }
        Set<String> copy = new HashSet<>(orglist);
        copy.remove(OrganizationContext.ORGZ_ADMIN);
        return Set.copyOf(copy);
    }

    public static String getBean(String organizationId, String beanNameAsKey) {
        String ret = get().getConfig(organizationId, OrgProfileEntityType.SERVICE_BEAN, beanNameAsKey);
        if (ret == null) {
            throw new GearsException(GearsResponseStatus.RECORD_IN_USE_ERROR,
                    String.format("Requested profile value is not available [ORG=%s | BEAN=%s]",
                            organizationId,
                            beanNameAsKey));
        }
        return ret;
    }

    public static String getConfig(String organizationId, String configKey) {
        String ret = get().getConfig(organizationId, OrgProfileEntityType.RUNTIME_CONFIG, configKey);
        if (ret == null) {
            throw new GearsException(GearsResponseStatus.RECORD_IN_USE_ERROR,
                    String.format("Requested profile value is not available [ORG=%s | CONF=%s]",
                            organizationId,
                            configKey));
        }
        return ret;
    }

    public static String getProperty(String organizationId, String propertyKey) {
        String ret = get().getConfig(organizationId, OrgProfileEntityType.PROPERTY, propertyKey);
        if (ret == null) {
            throw new GearsException(GearsResponseStatus.RECORD_IN_USE_ERROR,
                    String.format("Requested profile value is not available [ORG=%s | PROP=%s]",
                            organizationId,
                            propertyKey));
        }
        return ret;
    }

    public static String getCompositeKey(String orgId, String entityType, String entityKey) {
        return String.join(":", orgId, entityType, entityKey);
    }
}