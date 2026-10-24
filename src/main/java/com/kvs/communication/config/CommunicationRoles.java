package com.kvs.communication.config;

/**
 * RBAC authorization expressions for Module 09 Stakeholder Communication, WhatsApp/SMS Gateway & Parent App.
 * User stories: US-09000 to US-09019.
 */
public final class CommunicationRoles {

    private CommunicationRoles() {
    }

    /** Administrators & Public Relations Officers */
    public static final String BROADCAST_MANAGE = "hasAnyRole('PUBLIC_RELATIONS_OFFICER', 'COMMUNICATION_OFFICER', 'PRINCIPAL', 'SCHOOL_PRINCIPAL', 'SUPER_ADMIN', 'ADMIN')";

    /** Automated systems, attendance officers, and admins */
    public static final String SYSTEM_OR_ADMIN = "hasAnyRole('SYSTEM_AUTOMATION', 'ATTENDANCE_OFFICER', 'SUPER_ADMIN', 'ADMIN')";

    /** Parent users */
    public static final String PARENT_ONLY = "hasAnyRole('PARENT', 'APPLICANT_PARENT', 'SUPER_ADMIN', 'ADMIN')";

    /** Teachers and Parents for confidential messaging */
    public static final String MESSAGING_ACCESS = "hasAnyRole('PARENT', 'APPLICANT_PARENT', 'TEACHER', 'SUBJECT_TEACHER', 'CLASS_TEACHER', 'SUPER_ADMIN', 'ADMIN')";

    /** Parent gate pass generation */
    public static final String GATE_PASS_REQUEST = "hasAnyRole('PARENT', 'APPLICANT_PARENT', 'RECEPTIONIST', 'SUPER_ADMIN', 'ADMIN')";

    /** Security Guards scanning gate passes & auditing gates */
    public static final String SECURITY_SCAN = "hasAnyRole('SECURITY_GUARD', 'SUPER_ADMIN', 'ADMIN')";

    /** Reception desk & Visitor handling */
    public static final String RECEPTION_DESK = "hasAnyRole('RECEPTIONIST', 'FRONT_OFFICE_COUNSELOR', 'ADMIN_CLERK', 'PRINCIPAL', 'SCHOOL_PRINCIPAL', 'SUPER_ADMIN', 'ADMIN')";

    /** Emergency announcements & Pinned circulars by leadership */
    public static final String LEADERSHIP_OR_ADMIN = "hasAnyRole('PRINCIPAL', 'SCHOOL_PRINCIPAL', 'SUPER_ADMIN', 'ADMIN')";

    /** Document notification dispatcher */
    public static final String DOCUMENT_DISPATCH = "hasAnyRole('SYSTEM_AUTOMATION', 'ACCOUNTANT', 'PAYROLL_ACCOUNTANT', 'SUPER_ADMIN', 'ADMIN')";

    /** School document library: folders, uploads and who can view a file */
    public static final String DOCUMENT_MANAGE = "hasAnyRole('PUBLIC_RELATIONS_OFFICER', 'COMMUNICATION_OFFICER', 'PRINCIPAL', 'SCHOOL_PRINCIPAL', 'ADMIN_CLERK', 'SUPER_ADMIN', 'ADMIN')";

    /** Any authenticated user in communication */
    public static final String ANY_AUTHENTICATED = "isAuthenticated()";
}
