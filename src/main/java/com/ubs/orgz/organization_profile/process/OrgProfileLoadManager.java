package com.ubs.orgz.organization_profile.process;

import lombok.Getter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@DependsOn("entityManagerFactory")
public class OrgProfileLoadManager {

    private final OrgProfileLoad profileLoad;

    public OrgProfileLoadManager(OrgProfileLoad profileLoad) {
        this.profileLoad = profileLoad;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() {
        profileLoad.loadOnStartup();
    }

    public void loadOnDemand() {
        profileLoad.loadOnDemand();
    }

    @Getter
    public static final class ReloadResult {
        final Map<String, String> profiles;
        final Set<String> organizations;

        public ReloadResult(Map<String, String> profiles, Set<String> organizations) {
            this.profiles = profiles;
            this.organizations = organizations;
        }
    }
}
