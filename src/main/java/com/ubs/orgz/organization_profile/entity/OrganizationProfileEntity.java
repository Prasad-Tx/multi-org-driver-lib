package com.ubs.orgz.organization_profile.entity;

import com.ubs.commons.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Getter
@Setter
@Table(name = OrganizationProfileEntity.TABLE,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_org_profile_id_type_key",
                        columnNames = {"organization_id", "entity_type", "entity_key"})
        })
@Entity
public class OrganizationProfileEntity extends BaseEntity {

    public static final String TABLE = "organization_profiles";
    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_ENTITY_TYPE = "entity_type";
    public static final String COL_ENTITY_KEY = "entity_key";
    public static final String COL_ENTITY_VALUE = "entity_value";

    @Column(name = COL_ORGANIZATION_ID, nullable = false, length = 63)
    private String organizationId;

    @Column(name = COL_ENTITY_TYPE, nullable = false, length = 63)
    private String entityType;

    @Column(name = COL_ENTITY_KEY, nullable = false, length = 63)
    private String entityKey;

    @Column(name = COL_ENTITY_VALUE, nullable = false, length = 1023)
    private String entityValue;
}