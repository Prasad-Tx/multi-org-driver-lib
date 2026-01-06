package com.ubs.orgz.organization_profile;

import com.ubs.orgz.organization.OrganizationContext;
import com.ubs.orgz.organization_profile.entity.OrganizationProfileEntity;
import com.ubs.orgz.organization_profile.repo.OrganizationProfileRepo;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@DependsOn("entityManagerFactory")
public class OrganizationProfileProcessor {

    private final OrganizationProfileRepo repo;
    private final TransactionTemplate txTemplate;
    private final ProfileHolder profileHolder;

    public OrganizationProfileProcessor(
            OrganizationProfileRepo repo,
            PlatformTransactionManager txManager,
            ProfileHolder profileHolder) {

        this.repo = repo;
        this.profileHolder = profileHolder;

        this.txTemplate = new TransactionTemplate(txManager);
        this.txTemplate.setTimeout(60);
        this.txTemplate.setReadOnly(true);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() {
        reload();
    }

    public void reload() {
        OrganizationContext.setOrganization(OrganizationContext.BOOTSTRAP_ORG);

        try {
            ReloadResult result = txTemplate.execute(status -> {
                Set<String> orgs = new HashSet<>();
                Map<String, String> profiles = new HashMap<>();

                orgs.add(OrganizationContext.BOOTSTRAP_ORG);

                for (OrganizationProfileEntity e
                        : repo.findAllActive()) {

                    orgs.add(e.getOrganizationId());

                    String key = buildKey(e);
                    profiles.put(key, e.getEntityValue());
                }

                return new ReloadResult(profiles, orgs);
            });

            if (result == null) {
                throw new IllegalStateException(
                        "Profile reload failed: transaction returned null"
                );
            }

            profileHolder.reload(
                    result.profiles,
                    result.organizations
            );

        } finally {
            OrganizationContext.clear();
        }
    }

    private static String buildKey(
            OrganizationProfileEntity e) {

        return e.getOrganizationId() + ":" +
                e.getEntityType() + ":" +
                e.getEntityKey();
    }

    private static final class ReloadResult {
        final Map<String, String> profiles;
        final Set<String> organizations;

        ReloadResult(Map<String, String> profiles,
                     Set<String> organizations) {
            this.profiles = profiles;
            this.organizations = organizations;
        }
    }
}
