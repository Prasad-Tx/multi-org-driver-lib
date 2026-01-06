package com.ubs.orgz.organization;

import com.ubs.commons.exception.GearsException;
import com.ubs.commons.exception.GearsResponseStatus;
import com.ubs.orgz.organization_profile.OrganizationProfileProcessor;
import com.ubs.orgz.organization_profile.ProfileRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class OrganizationFilter implements Filter {

    private static final String ORG_HEADER = "X-Organization-Id";

    private final OrganizationProfileProcessor profileProcessor;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final AtomicBoolean reloadInProgress = new AtomicBoolean(false);

    public OrganizationFilter(OrganizationProfileProcessor profileProcessor,
                              ThreadPoolTaskExecutor taskExecutor) {
        this.profileProcessor = profileProcessor;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String orgId = httpRequest.getHeader(ORG_HEADER);

        if (orgId == null || orgId.isBlank()) {
            httpResponse.sendError(HttpStatus.BAD_REQUEST.value(),
                    ORG_HEADER + " - header is missing or empty.");
            return;
        }

        try {
            if (!ProfileRegistry.exists(orgId)) {
                httpResponse.sendError(HttpStatus.NOT_FOUND.value(),
                        "Unknown organization: " + orgId);
                return;
            }
        } catch (IllegalStateException e) {
            httpResponse.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "ProfileRegistry not ready.");
            return;
        }

        OrganizationContext.setOrganization(orgId);
        try {
            chain.doFilter(request, response);
            if (OrganizationContext.BOOTSTRAP_ORG.equals(orgId)) {
                triggerReloadAsync();
            }

        } finally {
            OrganizationContext.clear();
        }
    }

    private void triggerReloadAsync() {
        if (reloadInProgress.compareAndSet(false, true)) {
            taskExecutor.execute(() -> {
                try {
                    profileProcessor.reload();
                } catch (Exception ex) {
                    throw new GearsException(GearsResponseStatus.INTERNAL_ERROR, "Failed to reload profiles");
                } finally {
                    reloadInProgress.set(false);
                }
            });
        }
    }
}

