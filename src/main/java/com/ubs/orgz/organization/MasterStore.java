package com.ubs.orgz.organization;

import com.ubs.commons.exception.GearsException;
import com.ubs.commons.exception.GearsResponseStatus;
import com.ubs.orgz.organization_profile.OrgProfileEntityType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class MasterStore {

    /**
     * Immutable snapshot of all in-memory data.
     */
    private static final class Snapshot {
        private final Set<String> orgIds;
        private final Map<String, String> profiles;

        private Snapshot(Set<String> orgIds, Map<String, String> profiles) {
            this.orgIds = (orgIds == null) ? Set.of() : Set.copyOf(orgIds);
            this.profiles = (profiles == null) ? Map.of() : Map.copyOf(profiles);
        }
    }

    private final AtomicReference<Snapshot> snapshotRef =
            new AtomicReference<>(new Snapshot(Set.of(), Map.of()));

    /**** Getter and Setter : Organizations IDs ****/
    public Set<String> getOrgList() {
        // orgIds is already immutable (Set.copyOf in Snapshot ctor)
        return snapshotRef.get().orgIds;
    }

    /**
     * Partial update: updates only organizations while preserving current profiles.
     * Uses updateAndGet to avoid lost updates under concurrent writers.
     */
    public void setOrgList(Set<String> orglist) {
        snapshotRef.updateAndGet(current -> new Snapshot(orglist, current.profiles));
    }

    /**** Getter and Setter : Profiles ****/
    public Map<String, String> getProfiles() {
        return snapshotRef.get().profiles;
    }

    /**
     * Partial update: updates only profiles while preserving current organizations.
     * Uses updateAndGet to avoid lost updates under concurrent writers.
     */
    public void setProfiles(Map<String, String> profiles) {
        snapshotRef.updateAndGet(current -> new Snapshot(current.orgIds, profiles));
    }

    /**
     * Atomic swap of both organizations and profiles.
     * Prefer this when organizations and profiles must be refreshed together.
     */
    public void replaceAll(Set<String> organizations, Map<String, String> profiles) {
        snapshotRef.set(new Snapshot(organizations, profiles));
    }

    public boolean isOrgInList(String orgId) {
        return orgId != null && snapshotRef.get().orgIds.contains(orgId);
    }

    /**
     * Retrieves the configuration value associated with the specified organization ID,
     * entity type, and entity key.
     */
    public String getConfig(String organizationId, OrgProfileEntityType entityType, String entityKey) {
        if (organizationId == null || entityType == null || entityKey == null) {
            throw new GearsException(
                    GearsResponseStatus.BAD_REQUEST,
                    "organizationId, entityType and entityKey must be non-null."
            );
        }

        String compositeKey = MasterStoreRegistry.getCompositeKey(
                organizationId,
                entityType.toString(),
                entityKey
        );

        return snapshotRef.get().profiles.get(compositeKey);
    }
}
