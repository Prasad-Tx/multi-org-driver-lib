package com.ubs.orgz.organization_profile.repo;

import com.ubs.orgz.organization_profile.entity.OrganizationProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationProfileRepo extends JpaRepository<OrganizationProfileEntity, Long> {
/*

    *//**
     * Retrieves a list of organization profile configuration entities
     * based on the given organization identifier, excluding deleted entities.
     *
     * @param organizationId the identifier of the organization
     *                       whose profile configurations are to be retrieved
     * @return a list of {@code OrganizationProfileConfigEntity} instances that
     *         are associated with the specified organization and not marked as deleted
     *//*
    @Query("SELECT opc FROM OrganizationProfileConfigEntity opc " +
            "WHERE opc.organizationId = :organizationId " +
            "AND opc.deleted = false")
    List<OrganizationProfileConfigEntity> findByOrganizationId(@Param("organizationId") String organizationId);


    *//**
     * Retrieves an optional organization profile configuration entity based on the given organization
     * identifier and service type name, excluding deleted entities.
     *
     * @param organizationId the identifier of the organization used to filter the query
     * @param serviceTypeName the name of the service type used to filter the query
     * @return an {@code Optional} containing the matching {@code OrganizationProfileConfigEntity}
     *         instance if one is found and not marked as deleted; otherwise, an empty {@code Optional}
     *//*
    @Query("SELECT opc FROM OrganizationProfileConfigEntity opc " +
            "WHERE opc.organizationId = :organizationId " +
            "AND opc.serviceTypeName = :serviceTypeName " +
            "AND opc.deleted = false")
    Optional<OrganizationProfileConfigEntity> findByOrganizationIdAndServiceTypeName(
            @Param("organizationId") String organizationId,
            @Param("serviceTypeName") String serviceTypeName);


    */
    /**
     * Retrieves a list of all active organization profile configuration entities
     * that are not marked as deleted.
     *
     * @return a list of {@code OrganizationProfileConfigEntity} instances
     *         that are active and not marked as deleted
     */
    @Query("SELECT opc FROM OrganizationProfileEntity opc " +
            "WHERE opc.deleted = false")
    List<OrganizationProfileEntity> findAllActive();
}