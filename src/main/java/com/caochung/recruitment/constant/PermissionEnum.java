package com.caochung.recruitment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionEnum {
    // 1. COMPANIES
    COMPANY_CREATE("COMPANY_CREATE", "/api/v1/companies", "POST", "COMPANIES"),
    COMPANY_UPDATE("COMPANY_UPDATE", "/api/v1/companies", "PUT", "COMPANIES"),
    COMPANY_DELETE("COMPANY_DELETE", "/api/v1/companies/{id}", "DELETE", "COMPANIES"),
    COMPANY_VIEW_DETAIL("COMPANY_VIEW_DETAIL", "/api/v1/companies/{id}", "GET", "COMPANIES"),
    COMPANY_VIEW_ALL("COMPANY_VIEW_ALL", "/api/v1/companies", "GET", "COMPANIES"),

    // 2. JOBS
    JOB_CREATE("JOB_CREATE", "/api/v1/jobs", "POST", "JOBS"),
    JOB_UPDATE("JOB_UPDATE", "/api/v1/jobs", "PUT", "JOBS"),
    JOB_DELETE("JOB_DELETE", "/api/v1/jobs/{id}", "DELETE", "JOBS"),
    JOB_VIEW_DETAIL("JOB_VIEW_DETAIL", "/api/v1/jobs/{id}", "GET", "JOBS"),
    JOB_VIEW_ALL("JOB_VIEW_ALL", "/api/v1/jobs", "GET", "JOBS"),

    // 3. SKILLS
    SKILL_CREATE("SKILL_CREATE", "/api/v1/skills", "POST", "SKILLS"),
    SKILL_UPDATE("SKILL_UPDATE", "/api/v1/skills", "PUT", "SKILLS"),
    SKILL_DELETE("SKILL_DELETE", "/api/v1/skills/{id}", "DELETE", "SKILLS"),
    SKILL_VIEW_DETAIL("SKILL_VIEW_DETAIL", "/api/v1/skills/{id}", "GET", "SKILLS"),
    SKILL_VIEW_ALL("SKILL_VIEW_ALL", "/api/v1/skills", "GET", "SKILLS"),


    // 4. PERMISSIONS
    PERMISSION_CREATE("PERMISSION_CREATE", "/api/v1/permissions", "POST", "PERMISSIONS"),
    PERMISSION_UPDATE("PERMISSION_UPDATE", "/api/v1/permissions", "PUT", "PERMISSIONS"),
    PERMISSION_DELETE("PERMISSION_DELETE", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"),
    PERMISSION_VIEW_DETAIL("PERMISSION_VIEW_DETAIL", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"),
    PERMISSION_VIEW_ALL("PERMISSION_VIEW_ALL", "/api/v1/permissions", "GET", "PERMISSIONS"),

    // 5. RESUMES
    RESUME_CREATE("RESUME_CREATE", "/api/v1/resumes", "POST", "RESUMES"),
    RESUME_UPDATE("RESUME_UPDATE", "/api/v1/resumes", "PUT", "RESUMES"),
    RESUME_DELETE("RESUME_DELETE", "/api/v1/resumes/{id}", "DELETE", "RESUMES"),
    RESUME_VIEW_DETAIL("RESUME_VIEW_DETAIL", "/api/v1/resumes/{id}", "GET", "RESUMES"),
    RESUME_VIEW_ALL("RESUME_VIEW_ALL", "/api/v1/resumes", "GET", "RESUMES"),
    RESUME_VIEW_OWN("RESUME_VIEW_OWN", "/api/v1/resumes/by-user", "GET", "RESUMES"),
    RESUME_VIEW_COMPANY("RESUME_VIEW_COMPANY", "/api/v1/resumes/by-company", "GET", "RESUMES"),

    // 6. ROLES
    ROLE_CREATE("ROLE_CREATE", "/api/v1/roles", "POST", "ROLES"),
    ROLE_UPDATE("ROLE_UPDATE", "/api/v1/roles", "PUT", "ROLES"),
    ROLE_DELETE("ROLE_DELETE", "/api/v1/roles/{id}", "DELETE", "ROLES"),
    ROLE_VIEW_DETAIL("ROLE_VIEW_DETAIL", "/api/v1/roles/{id}", "GET", "ROLES"),
    ROLE_VIEW_ALL("ROLE_VIEW_ALL", "/api/v1/roles", "GET", "ROLES"),

    // 7. USERS
    USER_CREATE("USER_CREATE", "/api/v1/users", "POST", "USERS"),
    USER_UPDATE("USER_UPDATE", "/api/v1/users", "PUT", "USERS"),
    USER_DELETE("USER_DELETE", "/api/v1/users/{id}", "DELETE", "USERS"),
    USER_VIEW_DETAIL("USER_VIEW_DETAIL", "/api/v1/users/{id}", "GET", "USERS"),
    USER_VIEW_ALL("USER_VIEW_ALL", "/api/v1/users", "GET", "USERS"),

    // 8. SUBSCRIBERS
    SUBSCRIBER_CREATE("SUBSCRIBER_CREATE", "/api/v1/subscribers", "POST", "SUBSCRIBERS"),
    SUBSCRIBER_UPDATE("SUBSCRIBER_UPDATE", "/api/v1/subscribers", "PUT", "SUBSCRIBERS"),
    SUBSCRIBER_DELETE("SUBSCRIBER_DELETE", "/api/v1/subscribers/{id}", "DELETE", "SUBSCRIBERS"),
    SUBSCRIBER_VIEW_DETAIL("SUBSCRIBER_VIEW_DETAIL", "/api/v1/subscribers/{id}", "GET", "SUBSCRIBERS"),
    SUBSCRIBER_VIEW_ALL("SUBSCRIBER_VIEW_ALL", "/api/v1/subscribers", "GET", "SUBSCRIBERS"),

    // 9. FILES
    FILE_UPLOAD("FILE_UPLOAD", "/api/v1/files/uploads", "POST", "FILES");

    private final String name;
    private final String apiPath;
    private final String method;
    private final String module;
}
