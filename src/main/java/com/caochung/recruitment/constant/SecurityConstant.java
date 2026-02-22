package com.caochung.recruitment.constant;

public final class SecurityConstant {

    private SecurityConstant() {}

    // ================= 1. COMPANIES =================
    public static final String COMPANY_CREATE = "hasAuthority('COMPANY_CREATE')";
    public static final String COMPANY_UPDATE = "hasAuthority('COMPANY_UPDATE') and @customSecurity.isCompanyOwner(#id)";
    public static final String COMPANY_DELETE = "hasAuthority('COMPANY_DELETE')";
    public static final String COMPANY_VIEW_DETAIL = "hasAuthority('COMPANY_VIEW_DETAIL')";
    public static final String COMPANY_VIEW_ALL = "hasAuthority('COMPANY_VIEW_ALL')";

    // ================= 2. JOBS =================
    public static final String JOB_CREATE = "hasAuthority('JOB_CREATE')";
    public static final String JOB_UPDATE = "hasAuthority('JOB_UPDATE') and @customSecurity.isJobOwner(#id)";
    public static final String JOB_DELETE = "hasAuthority('JOB_DELETE') and @customSecurity.isJobOwner(#id)";
    public static final String JOB_VIEW_DETAIL = "hasAuthority('JOB_VIEW_DETAIL')";
    public static final String JOB_VIEW_ALL = "hasAuthority('JOB_VIEW_ALL')";

    // ================= 3. SKILLS =================
    public static final String SKILL_CREATE = "hasAuthority('SKILL_CREATE')";
    public static final String SKILL_UPDATE = "hasAuthority('SKILL_UPDATE')";
    public static final String SKILL_DELETE = "hasAuthority('SKILL_DELETE')";
    public static final String SKILL_VIEW_DETAIL = "hasAuthority('SKILL_VIEW_DETAIL')";
    public static final String SKILL_VIEW_ALL = "hasAuthority('SKILL_VIEW_ALL')";

    // ================= 4. PERMISSIONS =================
    public static final String PERMISSION_CREATE = "hasAuthority('PERMISSION_CREATE')";
    public static final String PERMISSION_UPDATE = "hasAuthority('PERMISSION_UPDATE')";
    public static final String PERMISSION_DELETE = "hasAuthority('PERMISSION_DELETE')";
    public static final String PERMISSION_VIEW_DETAIL = "hasAuthority('PERMISSION_VIEW_DETAIL')";
    public static final String PERMISSION_VIEW_ALL = "hasAuthority('PERMISSION_VIEW_ALL')";

    // ================= 5. RESUMES =================
    public static final String RESUME_CREATE = "hasAuthority('RESUME_CREATE')";
    public static final String RESUME_UPDATE = "hasAuthority('RESUME_UPDATE') and @customSecurity.isResumeInRecruiterCompany(#id)";
    public static final String RESUME_DELETE = "hasAuthority('RESUME_DELETE') and @customSecurity.isResumeOwner(#id)";
    public static final String RESUME_VIEW_DETAIL = "hasAuthority('RESUME_VIEW_DETAIL') and (@customSecurity.isResumeOwner(#id) or @customSecurity.isResumeInRecruiterCompany(#id))";
    public static final String RESUME_VIEW_ALL = "hasAuthority('RESUME_VIEW_ALL')";
    public static final String RESUME_VIEW_OWN = "hasAuthority('RESUME_VIEW_OWN')";
    public static final String RESUME_VIEW_COMPANY = "hasAuthority('RESUME_VIEW_COMPANY')";

    // ================= 6. ROLES =================
    public static final String ROLE_CREATE = "hasAuthority('ROLE_CREATE')";
    public static final String ROLE_UPDATE = "hasAuthority('ROLE_UPDATE')";
    public static final String ROLE_DELETE = "hasAuthority('ROLE_DELETE')";
    public static final String ROLE_VIEW_DETAIL = "hasAuthority('ROLE_VIEW_DETAIL')";
    public static final String ROLE_VIEW_ALL = "hasAuthority('ROLE_VIEW_ALL')";

    // ================= 7. USERS =================
    public static final String USER_CREATE = "hasAuthority('USER_CREATE')";
    public static final String USER_UPDATE = "hasAuthority('USER_UPDATE') and @customSecurity.isUserOwner(#id)";
    public static final String USER_DELETE = "hasAuthority('USER_DELETE') and @customSecurity.isUserOwner(#id)";
    public static final String USER_VIEW_DETAIL = "hasAuthority('USER_VIEW_DETAIL') and @customSecurity.isUserOwner(#id)";
    public static final String USER_VIEW_ALL = "hasAuthority('USER_VIEW_ALL')";

    // ================= 8. SUBSCRIBERS =================
    public static final String SUBSCRIBER_CREATE = "hasAuthority('SUBSCRIBER_CREATE')";
    public static final String SUBSCRIBER_UPDATE = "hasAuthority('SUBSCRIBER_UPDATE')";
    public static final String SUBSCRIBER_DELETE = "hasAuthority('SUBSCRIBER_DELETE')";
    public static final String SUBSCRIBER_VIEW_DETAIL = "hasAuthority('SUBSCRIBER_VIEW_DETAIL')";
    public static final String SUBSCRIBER_VIEW_ALL = "hasAuthority('SUBSCRIBER_VIEW_ALL')";

    // ================= 9. FILES =================
    public static final String FILE_UPLOAD = "hasAuthority('FILE_UPLOAD')";
}