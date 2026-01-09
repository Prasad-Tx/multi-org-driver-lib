package com.ubs.orgz.organization;

import com.ubs.orgz.organization_profile.process.OrgProfileLoadManager;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class OrganizationFilter implements Filter {

    private static final String ORG_HEADER = "X-Organization-Id";

    private final OrgProfileLoadManager profileManager;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final AtomicBoolean reloadInProgress = new AtomicBoolean(false);

    public OrganizationFilter(OrgProfileLoadManager profileManager, ThreadPoolTaskExecutor taskExecutor) {
        this.profileManager = profileManager;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String orgId = httpRequest.getHeader(ORG_HEADER);
        if (orgId == null) {
            httpResponse.sendError(HttpStatus.BAD_REQUEST.value(), ORG_HEADER + " - header is missing or empty.");
            return;
        }

        /**** admin org : do not further check nor set context.
         * will then do async reload later. ****/
        if (!OrganizationContext.ORGZ_ADMIN.equals(orgId)) {
            if (!MasterStoreRegistry.isReady()) {
                httpResponse.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Organization registry is not ready.");
                return;
            }
            if (MasterStoreRegistry.isOrganizationUnknown(orgId)) {
                httpResponse.sendError(HttpStatus.NOT_FOUND.value(), "Unknown organization: " + orgId);
                return;
            }
            OrganizationContext.setOrganization(orgId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            OrganizationContext.clear();
        }

        if (OrganizationContext.ORGZ_ADMIN.equals(orgId)) {
            triggerReloadAsync();
        }
    }

    private void triggerReloadAsync() {
        if (!reloadInProgress.compareAndSet(false, true)) {
            return;
        }

        taskExecutor.execute(() -> {
            try {
                OrganizationContext.setOrganization(OrganizationContext.ORGZ_ADMIN);
                profileManager.loadOnDemand();
            } catch (Exception ex) {
                ; // ignore
            } finally {
                OrganizationContext.clear();
                reloadInProgress.set(false);
            }
        });
    }
}

