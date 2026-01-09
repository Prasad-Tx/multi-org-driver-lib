package com.ubs.orgz.organization_profile.process.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.orgz.organization.MasterStoreRegistry;
import com.ubs.orgz.config.OrgProfileProperties;
import com.ubs.orgz.organization_profile.process.OrgProfileLoad;
import com.ubs.orgz.organization_profile.process.OrgProfileLoadManager;
import com.ubs.orgz.organization_profile.model.OrgProfileDataset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
@ConditionalOnProperty(
        name = "organizations.admin.mode",
        havingValue = OrgProfileProperties.ADMIN_JSON_FILE,
        matchIfMissing = true
)
public class OrgProfileLoadJsonf implements OrgProfileLoad {

    private final OrgProfileProperties properties;
    private final ObjectMapper objectMapper;

    public OrgProfileLoadJsonf(OrgProfileProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void loadOnStartup() {
        reload();
    }

    @Override
    public void loadOnDemand() {
        reload();
    }

    private void reload() {
        Resource resource = resolveFromFileSystem();

        if (!resource.exists()) {
            throw new IllegalStateException("Dataset resource not found");
        }

        try (InputStream is = resource.getInputStream()) {
            List<OrgProfileDataset> datasets = objectMapper.readValue(
                    is, new TypeReference<List<OrgProfileDataset>>() {});

            OrgProfileLoadManager.ReloadResult result = transform(datasets);
            MasterStoreRegistry.setProfiles(result.getProfiles());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load dataset");
        }
    }

    private OrgProfileLoadManager.ReloadResult transform(List<OrgProfileDataset> datasets) {
        if (datasets == null) {
            throw new IllegalArgumentException("datasets must not be null");
        }

        Map<String, String> profiles = new HashMap<>();
        Set<String> organizations = new HashSet<>();

        for (OrgProfileDataset dataset : datasets) {
            if (dataset == null) {
                continue;
            }

            String orgId = dataset.getOrganizationId();
            if (orgId == null || orgId.isBlank()) {
                throw new IllegalStateException("Invalid dataset entry: organizationId is null/blank");
            }

            /**** check if the organization is loaded for datasource, ignore otherwise ****/
            if (MasterStoreRegistry.isOrganizationUnknown(orgId)) {
                continue;
            }

            String entityType = dataset.getEntityType();
            String entityKey = dataset.getEntityKey();
            if (entityType == null || entityType.isBlank() || entityKey == null || entityKey.isBlank()) {
                throw new IllegalStateException("Invalid dataset entry for org " + orgId +
                        ": entityType/entityKey is null/blank");
            }

            /**** Valid organization found, add to the registry
             * and profiles map with the composite key
             * ****/
            organizations.add(orgId);
            String profileKey = MasterStoreRegistry.getCompositeKey(orgId, entityType, entityKey);
            profiles.put(profileKey, dataset.getEntityValue());
        }

        return new OrgProfileLoadManager.ReloadResult(profiles, organizations);
    }

    private Resource resolveFromFileSystem() {
        String path = safePath();
        if (path.startsWith("classpath:")) {
            return new ClassPathResource(path.substring("classpath:".length()));
        }
        return new FileSystemResource(path);
    }

    private String safePath() {
        if (properties == null || properties.getLoadFile() == null || properties.getLoadFile().getPath() == null) {
            throw new IllegalStateException("organizations.admin.load-file.path is not configured");
        }
        String path = properties.getLoadFile().getPath();
        if (path.isBlank()) {
            throw new IllegalStateException("organizations.admin.load-file.path is blank");
        }
        return path;
    }

}
