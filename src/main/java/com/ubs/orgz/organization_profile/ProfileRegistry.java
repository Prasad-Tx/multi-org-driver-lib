package com.ubs.orgz.organization_profile;

import com.ubs.commons.exception.GearsException;
import com.ubs.commons.exception.GearsResponseStatus;
import com.ubs.orgz.organization.OrganizationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class ProfileRegistry
        implements ApplicationContextAware {

    private static volatile ProfileHolder profileHolder;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        profileHolder = context.getBean(ProfileHolder.class);
    }

    private static ProfileHolder get() {
        if (profileHolder == null) {
            throw new GearsException(GearsResponseStatus.INTERNAL_ERROR, "ProfileHolder not initialized yet");
        }
        return profileHolder;
    }

    /**
     * Sets the current organization context by specifying the organization ID.
     * The organization ID must not be null or blank, and it must exist in the profile registry.
     *
     * @param organizationId the unique identifier of the organization to be set;
     *                       must not be null or blank and must exist in the system.
     * @throws IllegalArgumentException if the organization ID is null or blank.
     * @throws IllegalStateException if the organization ID does not exist.
     */
    public static void setOrganization(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be null and blank");
        }

        if (!get().exists(organizationId)) {
            throw new IllegalStateException("Unknown organization: " + organizationId);
        }

        OrganizationContext.setOrganization(organizationId);
    }

    /**
     * Retrieves the bean name corresponding to the given organization and bean key.
     * Throws a {@code GearsException} if the bean name is not available.
     *
     * @param organizationId the unique identifier of the organization; must not be null or blank
     * @param beanNameAsKey the key identifying the bean for the specified organization; must not be null or blank
     * @return the name of the bean associated with the given organization and bean key
     * @throws GearsException if the bean name is unavailable or the profile holder is not initialized
     */
    public static String getBean(String organizationId, String beanNameAsKey) {
        String ret = get().getConfig(organizationId, OrgProfileEntityType.SERVICE_BEAN, beanNameAsKey);
        if (ret == null) {
            throw new GearsException(GearsResponseStatus.RECORD_IN_USE_ERROR,
                    String.format("Requested profile value is not available [ORG=%s | BEAN=%s]",
                            organizationId,
                            beanNameAsKey));
        }
        return ret;
    }

    /**
     * Retrieves the runtime configuration value for a given organization and configuration key.
     * Throws a {@code GearsException} if the configuration value is not available.
     *
     * @param organizationId the unique identifier of the organization; must not be null or blank
     * @param configKey the key of the configuration to retrieve; must not be null or blank
     * @return the value of the runtime configuration associated with the given organization and configuration key
     * @throws GearsException if the configuration value is unavailable or the profile holder is not initialized
     */
    public static String getConfig(String organizationId, String configKey) {
        String ret = get().getConfig(organizationId, OrgProfileEntityType.RUNTIME_CONFIG, configKey);
        if (ret == null) {
            throw new GearsException(GearsResponseStatus.RECORD_IN_USE_ERROR,
                    String.format("Requested profile value is not available [ORG=%s | CONF=%s]",
                            organizationId,
                            configKey));
        }
        return ret;
    }

    /**
     * Retrieves a property value for a specific organization and property key.
     * If the property value is not available, an exception is thrown.
     *
     * @param organizationId the unique identifier of the organization; must not be null or blank
     * @param propertyKey the key identifying the property for the specified organization; must not be null or blank
     * @return the value of the property associated with the given organization and property key
     * @throws GearsException if the property value is unavailable or the profile holder is not initialized
     */
    public static String getProperty(String organizationId, String propertyKey) {
        String ret = get().getConfig(organizationId, OrgProfileEntityType.PROPERTY, propertyKey);
        if (ret == null) {
            throw new GearsException(GearsResponseStatus.RECORD_IN_USE_ERROR,
                    String.format("Requested profile value is not available [ORG=%s | PROP=%s]",
                            organizationId,
                            propertyKey));
        }
        return ret;
    }

    /**
     * Checks if an organization with the given ID exists in the profile registry.
     *
     * @param organizationId the ID of the organization to check; must not be null or blank
     * @return true if the organization exists, false otherwise
     */
    public static boolean exists(String organizationId) {
        return get().exists(organizationId);
    }

    /**
     * Retrieves a list of organizations available in the system, excluding the bootstrap organization.
     *
     * @return a list of organization IDs as strings.
     */
    public static List<String> getOrganizations() {
        return get().getOrganizations();
    }
}
