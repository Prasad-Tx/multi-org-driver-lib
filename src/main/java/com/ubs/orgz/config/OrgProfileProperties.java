package com.ubs.orgz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "organizations.admin")
@Getter @Setter
public class OrgProfileProperties {

    public static final String ADMIN_DB = "ADMIN_DB";
    public static final String ADMIN_JSON_FILE = "ADMIN_JSON_FILE";

    /**** Instance of yaml > organizations.admin.liquibase ****/
    private Liquibase liquibase;

    /**** Instance of yaml > organizations.admin.load-file ****/
    private LoadFile loadFile;

    /**** Value of yaml > organizations.admin.mode ****/
    private AdminMode mode;

    public enum AdminMode {
        ADMIN_DB,
        ADMIN_JSON_FILE
    }

    @Getter @Setter
    public static class Liquibase {
        private String changeLog;
        private String changeLogAdmin;
    }

    @Getter @Setter
    public static class LoadFile {
        private String path;
    }
}