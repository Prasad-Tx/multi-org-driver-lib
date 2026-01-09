package com.ubs.orgz.organization_profile.process.impl;

import com.ubs.orgz.organization.MasterStoreRegistry;
import com.ubs.orgz.config.OrgDatasourceProperties;
import com.ubs.orgz.config.OrgProfileProperties;
import com.ubs.orgz.organization.OrganizationContext;
import com.ubs.orgz.organization_profile.process.OrgProfileLoad;
import com.ubs.orgz.organization_profile.process.OrgProfileLoadManager;
import com.ubs.orgz.organization_profile.entity.OrganizationProfileEntity;
import com.ubs.orgz.organization_profile.repo.OrganizationProfileRepo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service implementation for loading organization profiles from a database.
 * This class handles the initialization and reloading of organization
 * profiles into memory upon application startup or demand.
 *
 * The service is conditionally loaded based on the property
 * "organizations.admin.mode" with a value of "ADMIN_DB". If the property
 * is not set, this implementation will be used by default.
 *
 * Dependencies:
 * - OrganizationProfileRepo: Repository to fetch organization profile data.
 * - PlatformTransactionManager: Manages transactions for database operations.
 * - OrgDatasourceProperties: Configuration properties related to
 *   organization datasource settings.
 *
 * Key Features:
 * - Loads profiles on application startup.
 * - Supports profile reloads on demand.
 * - Ensures transaction boundaries and read-only mode for data integrity.
 * - Stores loaded profiles and organizations in a central registry (MasterStoreRegistry).
 */
@Service
@ConditionalOnProperty(
        name = "organizations.admin.mode",
        havingValue = OrgProfileProperties.ADMIN_DB,
        matchIfMissing = true
)
public class OrgProfileLoadDatabase implements OrgProfileLoad {

    private final OrganizationProfileRepo repo;
    private final TransactionTemplate txTemplate;
    private final OrgDatasourceProperties datasourceProperties;

    public OrgProfileLoadDatabase(OrganizationProfileRepo repo,
                                  PlatformTransactionManager txManager,
                                  OrgDatasourceProperties datasourceProperties) {
        this.repo = repo;
        this.txTemplate = new TransactionTemplate(txManager);
        this.datasourceProperties = datasourceProperties;
        this.txTemplate.setTimeout(60);
        this.txTemplate.setReadOnly(true);
    }

    @Override
    public void loadOnStartup() {
        reload();
    }

    @Override
    public void loadOnDemand() {
        reload();
    }

    public void reload() {
        OrganizationContext.setOrganization(OrganizationContext.ORGZ_ADMIN);

        try {
            OrgProfileLoadManager.ReloadResult result = txTemplate.execute(status -> {
                Set<String> orgs = new HashSet<>();
                Map<String, String> profiles = new HashMap<>();

                for (OrganizationProfileEntity e : repo.findAllActive()) {
                    /**** check if the organization is loaded for datasource, ignore otherwise ****/
                    if (MasterStoreRegistry.isOrganizationUnknown(e.getOrganizationId())) {
                        continue;
                    }

                    /**** Valid organization found, add to the registry
                     * and profiles map with the composite key
                     * ****/
                    orgs.add(e.getOrganizationId());
                    profiles.put(buildKey(e), e.getEntityValue());
                }
                return new OrgProfileLoadManager.ReloadResult(profiles, orgs);
            });

            if (result == null) {
                throw new IllegalStateException("Profile reload failed: transaction returned null");
            }
            MasterStoreRegistry.setProfiles(result.getProfiles());
        } finally {
            OrganizationContext.clear();
        }
    }

    private static String buildKey(OrganizationProfileEntity e) {
        return MasterStoreRegistry.getCompositeKey(e.getOrganizationId(), e.getEntityType(), e.getEntityKey());
    }
}
