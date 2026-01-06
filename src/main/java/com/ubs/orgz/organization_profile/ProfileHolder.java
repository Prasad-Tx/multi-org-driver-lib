package com.ubs.orgz.organization_profile;

import com.ubs.commons.exception.GearsException;
import com.ubs.commons.exception.GearsResponseStatus;
import com.ubs.orgz.organization.OrganizationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ProfileHolder {

    private final AtomicReference<ProfileSnapshot> snapshotRef =
            new AtomicReference<>(ProfileSnapshot.EMPTY);

    public void reload(Map<String, String> profiles,
                       Set<String> organizations) {
        snapshotRef.set(new ProfileSnapshot(profiles, organizations));
    }

    public String getConfig(String organizationId,
                            OrgProfileEntityType entityType,
                            String entityKey) {
        validate(organizationId, entityType, entityKey);
        ProfileSnapshot snapshot = snapshotRef.get();
        String compositeKey = organizationId + ":" + entityType + ":" + entityKey;
        return snapshot.getProfiles().get(compositeKey);
    }

    public boolean exists(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be null and blank");
        }

        return snapshotRef.get()
                .getOrganizationIds()
                .contains(organizationId);
    }

    public List<String> getOrganizations() {
        List<String> list = new ArrayList<>(snapshotRef.get().getOrganizationIds());
        list.remove(OrganizationContext.BOOTSTRAP_ORG);
        return list;
    }

    private static void validate(String orgId,
                                 OrgProfileEntityType type,
                                 String key) {

        if (orgId == null || type == null || key == null) {
            throw new GearsException(GearsResponseStatus.BAD_REQUEST,
                    "Requested parameters may null / not valid.");
        }
    }
}
