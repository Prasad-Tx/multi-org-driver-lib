package com.ubs.orgz.organization_profile;

import java.util.Map;
import java.util.Set;
import java.util.Objects;

public final class ProfileSnapshot {

    private final Map<String, String> profiles;
    private final Set<String> organizationIds;

    public static final ProfileSnapshot EMPTY =
            new ProfileSnapshot(Map.of(), Set.of());

    public ProfileSnapshot(Map<String, String> profiles,
                           Set<String> organizationIds) {

        this.profiles = Map.copyOf(
                Objects.requireNonNull(profiles, "profiles"));
        this.organizationIds = Set.copyOf(
                Objects.requireNonNull(organizationIds, "organizations"));
    }

    public Map<String, String> getProfiles() {
        return profiles;
    }

    public Set<String> getOrganizationIds() {
        return organizationIds;
    }
}
