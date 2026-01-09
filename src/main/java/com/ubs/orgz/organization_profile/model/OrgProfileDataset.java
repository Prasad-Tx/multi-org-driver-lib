package com.ubs.orgz.organization_profile.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OrgProfileDataset {

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("entity_key")
    private String entityKey;

    @JsonProperty("entity_value")
    private String entityValue;
}
