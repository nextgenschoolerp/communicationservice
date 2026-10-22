-- =====================================================================================================
-- Module 09 (communicationservice) Seed Data: Delhi Public School (tenant a0eebc99-…), Academic Year 2026-2027
-- =====================================================================================================
-- Run after authservice and studentservice seeds, after communicationservice has started once:
--     powershell -ExecutionPolicy Bypass -File .agents/skills/test-ui/scripts/db/db.ps1 -Script <this file>
--
-- Story coverage:
--   US-09000: Multi-channel broadcast circulars (WhatsApp, SMS, Email, App Push)
--   US-09001: Automated 30-minute student absence alerts and counselor escalations
--   US-09002: Native mobile push notification device token registrations (FCM & APNS)
--   US-09003: Multi-child profile linking (Rajesh Sharma -> Aarav & Ananya Sharma)
--   US-09004: Two-way confidential teacher-parent messaging thread
--   US-09005: Digital student gate pass generation with QR code (GP-SEED-DEMO-0001)
--   US-09006: Security gate QR scan verification and student departure authorization
--   US-09007: Visitor appointment registration, digital badge issuance (VIS-0901) & check-in
--   US-09008: School calendar events (Diwali holidays, Board exams) and gallery circulars
--   US-09009: Parent grievance support tickets with automated SLA tracking (Transport Bus 14)
--   US-09010: Emergency weather closure announcements with multi-channel override
--   US-09011: Gateway delivery status tracking with automatic SMS fallback upon WhatsApp failure
--   US-09012: Parent notification channel preferences and night-time quiet hours
--   US-09013: Daily cafeteria nutritional lunch menus and bus route schedule updates
--   US-09014: Parent feedback surveys and 5-star rating responses
--   US-09015: Instant digital fee receipt and academic report card notifications
--   US-09016: Principal meeting appointment booking slots and parent reservations;
--             Year 10 parent–teacher meeting bookings on 19 Oct 2026 (Calendar page reference data)
--   US-09017: Perimeter security gate audit logging for unauthorized entry attempts
--   US-09018: Pinned urgent announcements on parent mobile app feeds
--   US-09019: Statutory regulatory compliance archival of past broadcasts
--   Documents page: school document library folders, files, permissions and access history
--   Calendar page: events with times and organisers; Help & support tickets; Settings → Notifications switches
--
-- Re-runnable: every seed row has created_by = 'seed:module09' and UUIDs starting with 5eed0009-.
-- =====================================================================================================

BEGIN;

-- ---------------------------------------------------------------------------------------------------
-- 0. Clean previous Module 09 seed rows
-- ---------------------------------------------------------------------------------------------------
DELETE FROM support_ticket_messages   WHERE created_by = 'seed:module09' OR message_id::text LIKE '5eed0009-%' OR ticket_id::text LIKE '5eed0009-%';
DELETE FROM support_tickets           WHERE created_by = 'seed:module09' OR ticket_id::text LIKE '5eed0009-%';
DELETE FROM staff_alert_preferences   WHERE created_by = 'seed:module09' OR preference_id::text LIKE '5eed0009-%';
DELETE FROM document_access_logs      WHERE created_by = 'seed:module09' OR log_id::text LIKE '5eed0009-%' OR document_id::text LIKE '5eed0009-%';
DELETE FROM school_document_contents  WHERE document_id::text LIKE '5eed0009-%';
DELETE FROM school_documents          WHERE created_by = 'seed:module09' OR document_id::text LIKE '5eed0009-%';
DELETE FROM document_folders          WHERE created_by = 'seed:module09' OR folder_id::text LIKE '5eed0009-%';
DELETE FROM gate_audit_logs           WHERE created_by = 'seed:module09' OR audit_id::text LIKE '5eed0009-%';
DELETE FROM appointment_slots         WHERE created_by = 'seed:module09' OR slot_id::text LIKE '5eed0009-%';
DELETE FROM document_notifications    WHERE created_by = 'seed:module09' OR notification_id::text LIKE '5eed0009-%';
DELETE FROM survey_responses          WHERE created_by = 'seed:module09' OR response_id::text LIKE '5eed0009-%';
DELETE FROM feedback_surveys          WHERE created_by = 'seed:module09' OR survey_id::text LIKE '5eed0009-%';
DELETE FROM daily_bulletin_posts      WHERE created_by = 'seed:module09' OR bulletin_id::text LIKE '5eed0009-%';
DELETE FROM notification_preferences  WHERE created_by = 'seed:module09' OR preference_id::text LIKE '5eed0009-%';
DELETE FROM grievance_tickets         WHERE created_by = 'seed:module09' OR ticket_id::text LIKE '5eed0009-%';
DELETE FROM calendar_events           WHERE created_by = 'seed:module09' OR event_id::text LIKE '5eed0009-%';
DELETE FROM visitor_appointments      WHERE created_by = 'seed:module09' OR appointment_id::text LIKE '5eed0009-%';
DELETE FROM student_gate_passes       WHERE created_by = 'seed:module09' OR pass_id::text LIKE '5eed0009-%';
DELETE FROM confidential_messages     WHERE created_by = 'seed:module09' OR message_id::text LIKE '5eed0009-%';
DELETE FROM parent_student_mappings   WHERE created_by = 'seed:module09' OR mapping_id::text LIKE '5eed0009-%';
DELETE FROM device_registrations      WHERE created_by = 'seed:module09' OR registration_id::text LIKE '5eed0009-%';
DELETE FROM student_absence_alerts    WHERE created_by = 'seed:module09' OR alert_id::text LIKE '5eed0009-%';
DELETE FROM broadcast_delivery_logs   WHERE created_by = 'seed:module09' OR delivery_id::text LIKE '5eed0009-%';
DELETE FROM communication_broadcasts  WHERE created_by = 'seed:module09' OR broadcast_id::text LIKE '5eed0009-%';
DELETE FROM user_roles                WHERE created_by = 'seed:module09';
DELETE FROM user_credentials          WHERE created_by = 'seed:module09';
DELETE FROM users                     WHERE created_by = 'seed:module09';

-- ---------------------------------------------------------------------------------------------------
-- 1. Ensure Module 09 Security Roles & Seed User Accounts in authservice
-- ---------------------------------------------------------------------------------------------------
INSERT INTO roles (name, description, active, created_by, updated_by, version)
SELECT v.name, v.description, true, 'seed:module09', NULL, 0
FROM (VALUES
    ('PUBLIC_RELATIONS_OFFICER', 'Public Relations and Communication Officer'),
    ('SECURITY_GUARD', 'Perimeter Campus Security Guard'),
    ('RECEPTIONIST', 'Front-Office Reception Desk Staff')
) AS v(name, description)
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.name = v.name);

-- Seed Accounts (Password@123 hash: $2a$10$8liDxOW98AN4N7eJQbqKd.LMq6EZ7rg6Q11jCO3sxeqrp39CXcXTq)
INSERT INTO users (user_id, tenant_id, branch_id, username, email, first_name, last_name, user_type, is_active, is_mfa_enabled, version, created_by) VALUES
    ('5eed0009-0000-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'DPS-DELHI-NORTH', 'pro_ananya', 'pro.ananya@dps.edu.in', 'Ananya', 'Sen', 'PUBLIC_RELATIONS_OFFICER', true, false, 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'DPS-DELHI-NORTH', 'guard_ramsingh', 'guard.ramsingh@dps.edu.in', 'Ram', 'Singh', 'SECURITY_GUARD', true, false, 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'DPS-DELHI-NORTH', 'receptionist_meena', 'receptionist.meena@dps.edu.in', 'Meena', 'Kumari', 'RECEPTIONIST', true, false, 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'DPS-DELHI-NORTH', 'parent_rajesh', 'parent.rajesh@kvs.local', 'Rajesh', 'Sharma', 'APPLICANT_PARENT', true, false, 0, 'seed:module09')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_credentials (user_id, password_hash, password_salt, failed_login_attempts, must_change_password, version, created_by) VALUES
    ('5eed0009-0000-4000-8000-000000000001', '$2a$10$8liDxOW98AN4N7eJQbqKd.LMq6EZ7rg6Q11jCO3sxeqrp39CXcXTq', 'salt_pro', 0, false, 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000002', '$2a$10$8liDxOW98AN4N7eJQbqKd.LMq6EZ7rg6Q11jCO3sxeqrp39CXcXTq', 'salt_grd', 0, false, 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000003', '$2a$10$8liDxOW98AN4N7eJQbqKd.LMq6EZ7rg6Q11jCO3sxeqrp39CXcXTq', 'salt_rec', 0, false, 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000004', '$2a$10$8liDxOW98AN4N7eJQbqKd.LMq6EZ7rg6Q11jCO3sxeqrp39CXcXTq', 'salt_par', 0, false, 0, 'seed:module09')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_code, version, created_by) VALUES
    ('5eed0009-0000-4000-8000-000000000001', 'PUBLIC_RELATIONS_OFFICER', 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000002', 'SECURITY_GUARD', 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000003', 'RECEPTIONIST', 0, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000004', 'APPLICANT_PARENT', 0, 'seed:module09')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------------------------------
-- 2. Communication Broadcasts & Delivery Logs (US-09000, US-09010, US-09011, US-09018, US-09019)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO communication_broadcasts (
    broadcast_id, tenant_id, sender_user_id, sender_name, title, message_body,
    target_audience, channel, priority, status, is_pinned, pinned_until, is_archived, version, created_by
) VALUES
    ('5eed0009-0001-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0000-4000-8000-000000000001',
     'Ananya Sen (PRO)', 'Annual Sports Day 2026 Schedule & Guidelines',
     'Dear Parents and Students, the 2026 Annual Athletics Meet will be held on December 12 at Campus Stadium.',
     'ALL_PARENTS', 'MULTI_CHANNEL', 'NORMAL', 'ACTIVE', true, CURRENT_TIMESTAMP + INTERVAL '7 days', false, 0, 'seed:module09'),

    ('5eed0009-0001-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0001-0001-4000-8000-000000000002',
     'Principal Dr. Sunita Rao', 'EMERGENCY: School Closure Due to Severe Weather Warning',
     'Due to meteorological department red alert for heavy rain and waterlogging, all physical classes remain suspended today.',
     'ALL_STAKEHOLDERS', 'MULTI_CHANNEL', 'EMERGENCY', 'ACTIVE', true, CURRENT_TIMESTAMP + INTERVAL '1 day', false, 0, 'seed:module09'),

    ('5eed0009-0001-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0000-4000-8000-000000000001',
     'Ananya Sen (PRO)', 'Academic Session 2025 Annual Fee Regulatory Circular (Archived)',
     'Compliance archive of previous year mandatory fee regulation notices and parent consent receipts.',
     'ALL_PARENTS', 'EMAIL', 'NORMAL', 'ARCHIVED', false, null, true, 0, 'seed:module09');

INSERT INTO broadcast_delivery_logs (
    delivery_id, broadcast_id, recipient_phone, recipient_email, recipient_user_id,
    channel, delivery_status, gateway_response_id, fallback_triggered, version, created_by
) VALUES
    ('5eed0009-0002-4000-8000-000000000001', '5eed0009-0001-4000-8000-000000000001',
     '+919876543210', 'parent.rajesh@kvs.local', '5eed0009-0000-4000-8000-000000000004',
     'WHATSAPP', 'DELIVERED', 'WA-GW-998811', false, 0, 'seed:module09'),

    ('5eed0009-0002-4000-8000-000000000002', '5eed0009-0001-4000-8000-000000000002',
     '+919876543210', 'parent.rajesh@kvs.local', '5eed0009-0000-4000-8000-000000000004',
     'SMS', 'DELIVERED', 'SMS-FALLBACK-887711', true, 0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 3. Student Absence Escalation Nudges (US-09001)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO student_absence_alerts (
    alert_id, tenant_id, student_id, student_name, guardian_phone, guardian_name,
    absence_date, alert_channel, delivery_status, escalated, version, created_by
) VALUES
    ('5eed0009-0003-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0003-0001-4000-8000-000000000001', 'Aarav Sharma', '+919876543210', 'Rajesh Sharma',
     CURRENT_DATE, 'WHATSAPP', 'SENT', false, 0, 'seed:module09'),

    ('5eed0009-0003-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0003-0001-4000-8000-000000000002', 'Ananya Sharma', '+919876543210', 'Rajesh Sharma',
     CURRENT_DATE - INTERVAL '1 day', 'WHATSAPP', 'DELIVERED', true, 0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 4. Device Registrations & Multi-Child Mappings (US-09002, US-09003, US-09012)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO device_registrations (
    registration_id, tenant_id, user_id, device_token, platform, app_version, is_active, version, created_by
) VALUES
    ('5eed0009-0004-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', 'fcm-dps-parent-rajesh-token-9988', 'ANDROID', '2.5.0', true, 0, 'seed:module09');

INSERT INTO parent_student_mappings (
    mapping_id, tenant_id, parent_user_id, parent_phone, parent_name,
    student_id, student_name, grade, section, is_primary, relationship, version, created_by
) VALUES
    ('5eed0009-0004-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', '+919876543210', 'Rajesh Sharma',
     '5eed0003-0001-4000-8000-000000000001', 'Aarav Sharma', '10', 'A', true, 'FATHER', 0, 'seed:module09'),

    ('5eed0009-0004-4000-8000-000000000012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', '+919876543210', 'Rajesh Sharma',
     '5eed0003-0001-4000-8000-000000000002', 'Ananya Sharma', '7', 'B', false, 'FATHER', 0, 'seed:module09');

INSERT INTO notification_preferences (
    preference_id, tenant_id, user_id, whatsapp_enabled, sms_enabled,
    email_enabled, push_enabled, quiet_hours_start, quiet_hours_end, version, created_by
) VALUES
    ('5eed0009-0004-4000-8000-000000000021', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', true, true, true, true, '22:00', '07:00', 0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 5. Teacher-Parent Messaging (US-09004)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO confidential_messages (
    message_id, tenant_id, sender_id, sender_name, sender_role,
    recipient_id, recipient_name, student_id, thread_id, subject, content, is_read, version, created_by
) VALUES
    ('5eed0009-0005-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', 'Rajesh Sharma', 'ROLE_PARENT',
     '5eed0001-0001-4000-8000-000000000005', 'Dr. Kavya Nair', '5eed0003-0001-4000-8000-000000000001',
     '5eed0009-0004-4000-8000-000000000001', 'Mathematics Chapter 4 Revision Clarification',
     'Dear Teacher, please confirm if quadratic equations formulas will be tested in Monday quiz.', false, 0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 6. Digital Student Gate Passes & Security Audits (US-09005, US-09006, US-09017)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO student_gate_passes (
    pass_id, tenant_id, student_id, student_name, grade, section, guardian_id, guardian_name,
    pickup_person_name, pickup_person_phone, reason, qr_code, status, guardian_approved, version, created_by
) VALUES
    ('5eed0009-0006-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0003-0001-4000-8000-000000000001', 'Aarav Sharma', '10', 'A',
     '5eed0009-0000-4000-8000-000000000004', 'Rajesh Sharma', 'Rajesh Sharma', '+919876543210',
     'Scheduled dentist root canal procedure at City Hospital', 'GP-SEED-DEMO-0001', 'ISSUED', true, 0, 'seed:module09');

INSERT INTO gate_audit_logs (
    audit_id, tenant_id, gate_id, gate_name, pass_id, pass_type, person_name,
    event_type, outcome, notes, timestamp, version, created_by
) VALUES
    ('5eed0009-0006-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'MAIN_GATE', 'Perimeter Main Gate', '5eed0009-0006-4000-8000-000000000001', 'STUDENT_GATE_PASS',
     'Rajesh Sharma', 'EXIT', 'GRANTED', 'Verified gate pass GP-SEED-DEMO-0001 for student Aarav Sharma', CURRENT_TIMESTAMP, 0, 'seed:module09'),

    ('5eed0009-0006-4000-8000-000000000012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'WEST_GATE', 'West Service Gate', null, 'VISITOR_PASS',
     'Unknown Pedestrian', 'UNAUTHORIZED_ATTEMPT', 'DENIED', 'Turned away due to lack of photo ID document', CURRENT_TIMESTAMP, 0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 7. Visitor Appointments & Meeting Slots (US-09007, US-09016)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO visitor_appointments (
    appointment_id, tenant_id, visitor_name, visitor_phone, visitor_id_type, visitor_id_number,
    purpose, host_staff_id, host_staff_name, appointment_time, badge_number, status, version, created_by
) VALUES
    ('5eed0009-0007-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Suresh Singhania', '+919811002233', 'AADHAAR', '987654321098',
     'New student admission inquiry for Grade 11 Science', '5eed0001-0001-4000-8000-000000000002',
     'Principal Dr. Sunita Rao', CURRENT_TIMESTAMP + INTERVAL '2 hours', 'VIS-0901', 'SCHEDULED', 0, 'seed:module09');

INSERT INTO appointment_slots (
    slot_id, tenant_id, host_user_id, host_name, slot_date, start_time, end_time, is_booked, version, created_by
) VALUES
    ('5eed0009-0007-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0001-0001-4000-8000-000000000002', 'Principal Dr. Sunita Rao',
     CURRENT_DATE + INTERVAL '2 days', '10:00', '10:30', false, 0, 'seed:module09'),

    ('5eed0009-0007-4000-8000-000000000012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0001-0001-4000-8000-000000000002', 'Principal Dr. Sunita Rao',
     CURRENT_DATE + INTERVAL '2 days', '11:00', '11:30', true, 0, 'seed:module09');

-- Year 10 parent–teacher meetings on 19 Oct 2026 (Calendar page: "View appointments"); subject kept in host_name
INSERT INTO appointment_slots (
    slot_id, tenant_id, host_user_id, host_name, slot_date, start_time, end_time, is_booked, booked_by_parent_name, version, created_by
) VALUES
    ('5eed0009-0007-4000-8000-000000000021', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0007-4000-8000-000000000101', 'Ms Sarah Collins (English)',   DATE '2026-10-19', '15:30', '15:40', true, 'Patel family',    0, 'seed:module09'),
    ('5eed0009-0007-4000-8000-000000000022', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0007-4000-8000-000000000102', 'Mr David Chen (Mathematics)',  DATE '2026-10-19', '15:30', '15:40', true, 'Okafor family',   0, 'seed:module09'),
    ('5eed0009-0007-4000-8000-000000000023', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0007-4000-8000-000000000103', 'Dr Amira Hassan (Science)',    DATE '2026-10-19', '15:40', '15:50', true, 'Williams family', 0, 'seed:module09'),
    ('5eed0009-0007-4000-8000-000000000024', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0007-4000-8000-000000000101', 'Ms Sarah Collins (English)',   DATE '2026-10-19', '15:40', '15:50', true, 'Nowak family',    0, 'seed:module09'),
    ('5eed0009-0007-4000-8000-000000000025', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0007-4000-8000-000000000104', 'Mr James Reid (History)',      DATE '2026-10-19', '15:50', '16:00', true, 'Thompson family', 0, 'seed:module09'),
    ('5eed0009-0007-4000-8000-000000000026', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0007-4000-8000-000000000102', 'Mr David Chen (Mathematics)',  DATE '2026-10-19', '15:50', '16:00', true, 'Garcia family',   0, 'seed:module09'),
    ('5eed0009-0007-4000-8000-000000000027', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0007-4000-8000-000000000105', 'Ms Laura Bennett (French)',    DATE '2026-10-19', '16:00', '16:10', true, 'Ahmed family',    0, 'seed:module09'),
    ('5eed0009-0007-4000-8000-000000000028', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0007-4000-8000-000000000103', 'Dr Amira Hassan (Science)',    DATE '2026-10-19', '16:00', '16:10', true, 'Murphy family',   0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 8. Calendar Events, Grievances, Bulletins, Surveys & Documents (US-09008, 09, 13, 14, 15)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO calendar_events (
    event_id, tenant_id, title, description, event_type, start_date, end_date, start_time, end_time, venue, organiser,
    target_audience, is_published, version, created_by
) VALUES
    ('5eed0009-0008-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Diwali Vacation Break 2026', 'Campus remains closed for auspicious Diwali celebrations.',
     'HOLIDAY', CURRENT_DATE + INTERVAL '10 days', CURRENT_DATE + INTERVAL '17 days', NULL, NULL, 'Campus', NULL, 'ALL', true, 0, 'seed:module09'),

    ('5eed0009-0008-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Year 10 parent–teacher meetings', 'Meet subject teachers to discuss progress, learning goals and support before the mid-term reports are published.',
     'MEETING', DATE '2026-10-19', DATE '2026-10-19', '15:30', '18:00', 'Main hall', 'Michael Brooks', 'GRADE_10', true, 0, 'seed:module09'),

    ('5eed0009-0008-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Autumn sports day', 'Track and field events for every year group; families are welcome from 10:00.',
     'SPORTS', DATE '2026-10-21', DATE '2026-10-21', '09:00', '14:00', 'Sports grounds', 'James Reid', 'ALL', true, 0, 'seed:module09'),

    ('5eed0009-0008-4000-8000-000000000004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Mid-term progress reports due', 'Class teachers submit mid-term grades and comments for every student.',
     'DEADLINE', DATE '2026-10-23', DATE '2026-10-23', '16:00', NULL, 'All teaching staff', 'Helen Morris', 'ALL_TEACHERS', true, 0, 'seed:module09'),

    ('5eed0009-0008-4000-8000-000000000005', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Half-term break', 'School is closed for half-term; registers and recurring lessons pause.',
     'HOLIDAY', DATE '2026-10-26', DATE '2026-10-30', NULL, NULL, 'Campus', NULL, 'ALL', true, 0, 'seed:module09'),

    ('5eed0009-0008-4000-8000-000000000006', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Year 11 mock examinations', 'Mock GCSE papers in the exam hall; timetables are on the parent portal.',
     'EXAM', DATE '2026-11-16', DATE '2026-11-20', '09:00', '12:30', 'Exam hall', 'James Reid', 'GRADE_11', true, 0, 'seed:module09'),

    ('5eed0009-0008-4000-8000-000000000007', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Winter concert', 'Choir, orchestra and drama showcase for families.',
     'CULTURAL', DATE '2026-12-10', DATE '2026-12-10', '18:00', '20:00', 'Main hall', 'Laura Bennett', 'ALL', true, 0, 'seed:module09');

INSERT INTO grievance_tickets (
    ticket_id, tenant_id, parent_user_id, parent_name, student_id, student_name,
    category, priority, subject, description, status, sla_due_hours, version, created_by
) VALUES
    ('5eed0009-0009-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', 'Rajesh Sharma', '5eed0003-0001-4000-8000-000000000001', 'Aarav Sharma',
     'TRANSPORT', 'HIGH', 'Bus 14 Morning Arrival Delay',
     'Bus 14 has arrived 20 minutes late three days in a row at Mayur Vihar stop.', 'OPEN', 24, 0, 'seed:module09');

INSERT INTO daily_bulletin_posts (
    bulletin_id, tenant_id, bulletin_type, title, content_details, effective_date, meal_type, version, created_by
) VALUES
    ('5eed0009-0013-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'CAFETERIA_MENU', 'Today Special Nutritious Lunch',
     'Paneer Makhani, Multigrain Chapatis, Jeera Pulao, Cucumber Mint Raita, Sweet Kheer', CURRENT_DATE, 'LUNCH', 0, 'seed:module09');

INSERT INTO feedback_surveys (
    survey_id, tenant_id, title, description, survey_type, is_active, version, created_by
) VALUES
    ('5eed0009-0014-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Parent Satisfaction Survey: STEM Lab Facilities',
     'Please evaluate the recently upgraded AI and Robotics laboratory equipment and curriculum.',
     'ACADEMIC', true, 0, 'seed:module09');

INSERT INTO survey_responses (
    response_id, tenant_id, survey_id, parent_user_id, parent_name, rating_score, comments, version, created_by
) VALUES
    ('5eed0009-0014-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0014-4000-8000-000000000001', '5eed0009-0000-4000-8000-000000000004',
     'Rajesh Sharma', 5, 'My son Aarav has developed immense interest in robotics coding. Superb faculty guidance!', 0, 'seed:module09');

INSERT INTO document_notifications (
    notification_id, tenant_id, document_type, document_id, student_id,
    parent_phone, parent_email, download_url, delivery_status, version, created_by
) VALUES
    ('5eed0009-0015-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'FEE_RECEIPT', 'DPS-REC-2026-Q1-0044', '5eed0003-0001-4000-8000-000000000001',
     '+919876543210', 'parent.rajesh@kvs.local',
     'https://cdn.dps.edu.in/receipts/2026/DPS-REC-2026-Q1-0044.pdf', 'DELIVERED', 0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 9. School Document Library (Documents page): folders, files and access history
--    Reference rows carry no file bytes (has_content = false); the page renders a placeholder PDF for them.
-- ---------------------------------------------------------------------------------------------------
INSERT INTO document_folders (folder_id, tenant_id, name, default_access, summary, sort_order, version, created_by) VALUES
    ('5eed0009-0010-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Student records', 'ADMINISTRATORS', '1,248 folders', 1, 0, 'seed:module09'),
    ('5eed0009-0010-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Staff records', 'ADMINISTRATORS', '92 folders', 2, 0, 'seed:module09'),
    ('5eed0009-0010-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'School policies', 'ALL_STAFF', NULL, 3, 0, 'seed:module09'),
    ('5eed0009-0010-4000-8000-000000000004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Finance & billing', 'ADMINISTRATORS', '186 documents', 4, 0, 'seed:module09');

INSERT INTO school_documents (
    document_id, tenant_id, folder_id, file_name, content_type, size_bytes, owner_name, access_level, doc_version,
    modified_on, is_shared, is_archived, has_content, version, created_by
) VALUES
    ('5eed0009-0011-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Safeguarding policy 2026.pdf', 'application/pdf', 1258291, 'Helen Morris', 'ALL_STAFF', '3.0', DATE '2026-09-01', true, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Staff handbook 2026–27.pdf', 'application/pdf', 700416, 'Michael Brooks', 'STAFF_AND_PARENTS', '2.1', DATE '2026-08-28', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Autumn term newsletter.pdf', 'application/pdf', 253952, 'Sarah Williams', 'PUBLIC', '1.0', DATE '2026-10-15', true, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Behaviour policy.pdf', 'application/pdf', 913408, 'Sarah Williams', 'ALL_STAFF', '2.0', DATE '2026-09-01', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000005', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Admissions policy.pdf', 'application/pdf', 782336, 'Michael Brooks', 'STAFF_AND_PARENTS', '1.0', DATE '2026-09-02', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000006', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'First aid procedures.pdf', 'application/pdf', 2516582, 'Grace Taylor', 'ALL_STAFF', '1.0', DATE '2026-09-05', true, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000007', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Attendance policy.pdf', 'application/pdf', 88064, 'Sarah Williams', 'ALL_STAFF', '1.4', DATE '2026-10-10', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000008', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Anti-bullying policy.pdf', 'application/pdf', 421888, 'Helen Morris', 'STAFF_AND_PARENTS', '1.0', DATE '2026-09-03', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000009', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Online safety policy.pdf', 'application/pdf', 550912, 'David Chen', 'STAFF_AND_PARENTS', '1.0', DATE '2026-09-12', true, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000010', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Data protection policy.pdf', 'application/pdf', 1153434, 'Michael Brooks', 'ALL_STAFF', '4.0', DATE '2026-08-30', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Equality and diversity policy.pdf', 'application/pdf', 364544, 'Grace Taylor', 'PUBLIC', '1.0', DATE '2026-09-08', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Health and safety policy.pdf', 'application/pdf', 1887437, 'James Reid', 'ALL_STAFF', '1.0', DATE '2026-09-15', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000013', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'SEND policy.pdf', 'application/pdf', 966656, 'Laura Bennett', 'STAFF_AND_PARENTS', '1.0', DATE '2026-09-20', true, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000014', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Uniform policy.pdf', 'application/pdf', 217088, 'Sarah Williams', 'PUBLIC', '1.0', DATE '2026-08-25', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000015', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Homework policy.pdf', 'application/pdf', 182272, 'David Chen', 'STAFF_AND_PARENTS', '1.0', DATE '2026-09-24', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000016', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Complaints procedure.pdf', 'application/pdf', 305152, 'Michael Brooks', 'PUBLIC', '1.0', DATE '2026-09-10', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000017', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Whistleblowing policy.pdf', 'application/pdf', 270336, 'Helen Morris', 'ALL_STAFF', '1.0', DATE '2026-08-29', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000018', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Exams policy 2026–27.pdf', 'application/pdf', 636928, 'James Reid', 'STAFF_AND_PARENTS', '1.2', DATE '2026-10-02', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000019', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Educational visits policy.pdf', 'application/pdf', 487424, 'Laura Bennett', 'ALL_STAFF', '1.0', DATE '2026-09-18', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000020', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Mobile phone policy.pdf', 'application/pdf', 137216, 'Sarah Williams', 'STAFF_AND_PARENTS', '1.0', DATE '2026-10-06', true, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000021', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Staff code of conduct.pdf', 'application/pdf', 397312, 'Helen Morris', 'ALL_STAFF', '2.0', DATE '2026-08-27', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000022', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Fire evacuation plan.pdf', 'application/pdf', 1572864, 'James Reid', 'ALL_STAFF', '1.0', DATE '2026-09-28', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000023', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Policy review schedule.csv', 'text/csv', 18432, 'Michael Brooks', 'ADMINISTRATORS', '1.0', DATE '2026-10-12', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000024', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Medicines in school policy.pdf', 'application/pdf', 411648, 'Grace Taylor', 'STAFF_AND_PARENTS', '1.0', DATE '2026-10-08', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000025', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Behaviour policy 2025.pdf', 'application/pdf', 880640, 'Sarah Williams', 'ALL_STAFF', '1.0', DATE '2025-09-01', false, true, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000026', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Safeguarding policy 2025.pdf', 'application/pdf', 1153434, 'Helen Morris', 'ALL_STAFF', '2.0', DATE '2025-09-01', false, true, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000027', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000003', 'Staff handbook 2025–26.pdf', 'application/pdf', 667648, 'Michael Brooks', 'STAFF_AND_PARENTS', '1.0', DATE '2025-08-28', false, true, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000028', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000001', 'Admissions register 2026–27.csv', 'text/csv', 98304, 'Michael Brooks', 'ADMINISTRATORS', '1.0', DATE '2026-10-14', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000029', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000001', 'Year 10 options summary.pdf', 'application/pdf', 319488, 'David Chen', 'ALL_STAFF', '1.0', DATE '2026-10-05', true, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000030', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000001', 'Consent forms tracker.csv', 'text/csv', 41984, 'Grace Taylor', 'ADMINISTRATORS', '1.0', DATE '2026-09-22', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000031', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000002', 'Staff training log 2026.csv', 'text/csv', 59392, 'Helen Morris', 'ADMINISTRATORS', '1.0', DATE '2026-10-09', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000032', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000002', 'DBS check register.pdf', 'application/pdf', 208896, 'Helen Morris', 'ADMINISTRATORS', '1.0', DATE '2026-09-04', false, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000033', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000004', 'Term 1 fee schedule.pdf', 'application/pdf', 149504, 'Michael Brooks', 'STAFF_AND_PARENTS', '1.0', DATE '2026-08-20', true, false, false, 0, 'seed:module09'),
    ('5eed0009-0011-4000-8000-000000000034', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0010-4000-8000-000000000004', 'Outstanding balances October.csv', 'text/csv', 73728, 'Michael Brooks', 'ADMINISTRATORS', '1.0', DATE '2026-10-16', false, false, false, 0, 'seed:module09');

INSERT INTO document_access_logs (log_id, tenant_id, document_id, actor_name, action, occurred_at, version, created_by) VALUES
    ('5eed0009-0012-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0011-4000-8000-000000000001', 'Helen Morris', 'Uploaded version 3.0', TIMESTAMP WITH TIME ZONE '2026-09-01 08:30:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0012-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0011-4000-8000-000000000001', 'Michael Brooks', 'Viewed', TIMESTAMP WITH TIME ZONE '2026-09-02 10:05:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0012-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0011-4000-8000-000000000001', 'Helen Morris', 'Share link copied', TIMESTAMP WITH TIME ZONE '2026-09-03 14:20:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0012-4000-8000-000000000004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0011-4000-8000-000000000001', 'Grace Taylor', 'Downloaded', TIMESTAMP WITH TIME ZONE '2026-10-16 09:12:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0012-4000-8000-000000000005', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0011-4000-8000-000000000003', 'Sarah Williams', 'Uploaded version 1.0', TIMESTAMP WITH TIME ZONE '2026-10-15 16:40:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0012-4000-8000-000000000006', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0011-4000-8000-000000000003', 'Sarah Williams', 'Access changed to Public', TIMESTAMP WITH TIME ZONE '2026-10-15 16:45:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0012-4000-8000-000000000007', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0011-4000-8000-000000000007', 'Sarah Williams', 'Uploaded version 1.4', TIMESTAMP WITH TIME ZONE '2026-10-10 11:00:00+05:30', 0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 11. Help & support tickets with their conversations, and Settings → Notifications switches
--     (requester / user: admin_rajesh, a1111111-1111-4111-8111-111111111111)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO support_tickets (
    ticket_id, tenant_id, ticket_number, subject, category, priority, status, requester_user_id, requester_name, requester_email,
    last_activity_at, resolved_at, created_at, version, created_by
) VALUES
    ('5eed0009-0013-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SUP-1032', 'Update parent portal email', 'ACCOUNT_ACCESS', 'NORMAL', 'RESOLVED',
     'a1111111-1111-4111-8111-111111111111', 'Rajesh Admin', 'rajesh.admin@dps.edu.in',
     TIMESTAMP WITH TIME ZONE '2026-10-14 11:20:00+05:30', TIMESTAMP WITH TIME ZONE '2026-10-14 11:20:00+05:30',
     TIMESTAMP WITH TIME ZONE '2026-10-12 09:40:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0013-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SUP-1048', 'Fee statement export formatting', 'FEES', 'NORMAL', 'IN_PROGRESS',
     'a1111111-1111-4111-8111-111111111111', 'Rajesh Admin', 'rajesh.admin@dps.edu.in',
     CURRENT_TIMESTAMP - INTERVAL '2 hours', NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', 0, 'seed:module09');

INSERT INTO support_ticket_messages (message_id, ticket_id, author_name, author_role, body, sent_at, version, created_by) VALUES
    ('5eed0009-0014-4000-8000-000000000001', '5eed0009-0013-4000-8000-000000000001', 'Rajesh Admin', 'REQUESTER',
     'Please change the reply-to address on parent portal emails from office@ to admissions@.', TIMESTAMP WITH TIME ZONE '2026-10-12 09:40:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0014-4000-8000-000000000002', '5eed0009-0013-4000-8000-000000000001', 'Workspace support', 'SUPPORT',
     'Done: portal emails now reply to admissions@. Let us know if anything looks wrong.', TIMESTAMP WITH TIME ZONE '2026-10-14 11:20:00+05:30', 0, 'seed:module09'),
    ('5eed0009-0014-4000-8000-000000000003', '5eed0009-0013-4000-8000-000000000002', 'Rajesh Admin', 'REQUESTER',
     'The Term 1 fee statement CSV shows a stray character before the currency symbol when opened in Excel.', CURRENT_TIMESTAMP - INTERVAL '1 day', 0, 'seed:module09'),
    ('5eed0009-0014-4000-8000-000000000004', '5eed0009-0013-4000-8000-000000000002', 'Workspace support', 'SUPPORT',
     'Thanks, we can reproduce it and are preparing a fix. We will update this ticket today.', CURRENT_TIMESTAMP - INTERVAL '2 hours', 0, 'seed:module09');

INSERT INTO staff_alert_preferences (preference_id, tenant_id, user_id, attendance_alerts, fee_reminders, leave_requests, daily_digest, version, created_by) VALUES
    ('5eed0009-0015-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a1111111-1111-4111-8111-111111111111', true, true, true, false, 0, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 10. Created timestamps: seeded rows bypass JPA auditing, so fill created_at (circulars are staggered so the
--     notification bell and Communications list them newest first)
-- ---------------------------------------------------------------------------------------------------
DO $$
DECLARE t text;
BEGIN
    FOREACH t IN ARRAY ARRAY['communication_broadcasts', 'broadcast_delivery_logs', 'student_absence_alerts', 'device_registrations',
        'parent_student_mappings', 'notification_preferences', 'confidential_messages', 'student_gate_passes', 'gate_audit_logs',
        'visitor_appointments', 'appointment_slots', 'calendar_events', 'grievance_tickets', 'daily_bulletin_posts', 'feedback_surveys',
        'survey_responses', 'document_notifications', 'document_folders', 'school_documents', 'document_access_logs',
        'support_ticket_messages', 'staff_alert_preferences']
    LOOP
        EXECUTE format('UPDATE %I SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP) WHERE created_by = %L', t, 'seed:module09');
    END LOOP;
END $$;
UPDATE communication_broadcasts
SET created_at = CURRENT_TIMESTAMP - (row_number_offset * INTERVAL '1 day')
FROM (SELECT broadcast_id AS id, ROW_NUMBER() OVER (ORDER BY broadcast_id) AS row_number_offset
      FROM communication_broadcasts WHERE created_by = 'seed:module09') ranked
WHERE broadcast_id = ranked.id;

COMMIT;
