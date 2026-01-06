package com.ubs.orgz.organization_profile;

import lombok.Getter;

@Getter
public enum OrgProfileEntityType {
    RUNTIME_CONFIG("RUNTIME_CONFIG"),
    SERVICE_BEAN("SERVICE_BEAN"),
    PROPERTY("PROPERTY");

    private final String configType;

    OrgProfileEntityType(String entityType) {
        this.configType = entityType;
    }

    @Override
    public String toString() {
        return configType;
    }
}
