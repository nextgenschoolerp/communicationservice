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
--   US-09016: Principal meeting appointment booking slots and parent reservations
--   US-09017: Perimeter security gate audit logging for unauthorized entry attempts
--   US-09018: Pinned urgent announcements on parent mobile app feeds
--   US-09019: Statutory regulatory compliance archival of past broadcasts
--
-- Re-runnable: every seed row has created_by = 'seed:module09' and UUIDs starting with 5eed0009-.
-- =====================================================================================================

BEGIN;

-- ---------------------------------------------------------------------------------------------------
-- 0. Clean previous Module 09 seed rows
-- ---------------------------------------------------------------------------------------------------
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
INSERT INTO roles (role_id, role_name, description) VALUES
    ('9001', 'ROLE_PUBLIC_RELATIONS_OFFICER', 'Public Relations and Communication Officer'),
    ('9002', 'ROLE_SECURITY_GUARD', 'Perimeter Campus Security Guard'),
    ('9003', 'ROLE_RECEPTIONIST', 'Front-Office Reception Desk Staff')
ON CONFLICT (role_id) DO NOTHING;

-- Seed Accounts (Password@123 hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi)
INSERT INTO users (user_id, tenant_id, username, email, first_name, last_name, is_active, created_by) VALUES
    ('5eed0009-0000-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'pro_ananya', 'pro.ananya@dps.edu.in', 'Ananya', 'Sen', true, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'guard_ramsingh', 'guard.ramsingh@dps.edu.in', 'Ram', 'Singh', true, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'receptionist_meena', 'receptionist.meena@dps.edu.in', 'Meena', 'Kumari', true, 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'parent_rajesh', 'parent.rajesh@kvs.local', 'Rajesh', 'Sharma', true, 'seed:module09')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_credentials (user_id, password_hash, created_by) VALUES
    ('5eed0009-0000-4000-8000-000000000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'seed:module09')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, created_by) VALUES
    ('5eed0009-0000-4000-8000-000000000001', '9001', 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000002', '9002', 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000003', '9003', 'seed:module09'),
    ('5eed0009-0000-4000-8000-000000000004', '3',    'seed:module09') -- ROLE_PARENT
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------------------------------
-- 2. Communication Broadcasts & Delivery Logs (US-09000, US-09010, US-09011, US-09018, US-09019)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO communication_broadcasts (
    broadcast_id, tenant_id, sender_user_id, sender_name, title, message_body,
    target_audience, channel, priority, status, is_pinned, pinned_until, is_archived, created_by
) VALUES
    ('5eed0009-0001-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0000-4000-8000-000000000001',
     'Ananya Sen (PRO)', 'Annual Sports Day 2026 Schedule & Guidelines',
     'Dear Parents and Students, the 2026 Annual Athletics Meet will be held on December 12 at Campus Stadium.',
     'ALL_PARENTS', 'MULTI_CHANNEL', 'NORMAL', 'ACTIVE', true, CURRENT_TIMESTAMP + INTERVAL '7 days', false, 'seed:module09'),

    ('5eed0009-0001-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0001-0001-4000-8000-000000000002',
     'Principal Dr. Sunita Rao', 'EMERGENCY: School Closure Due to Severe Weather Warning',
     'Due to meteorological department red alert for heavy rain and waterlogging, all physical classes remain suspended today.',
     'ALL_STAKEHOLDERS', 'MULTI_CHANNEL', 'EMERGENCY', 'ACTIVE', true, CURRENT_TIMESTAMP + INTERVAL '1 day', false, 'seed:module09'),

    ('5eed0009-0001-4000-8000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '5eed0009-0000-4000-8000-000000000001',
     'Ananya Sen (PRO)', 'Academic Session 2025 Annual Fee Regulatory Circular (Archived)',
     'Compliance archive of previous year mandatory fee regulation notices and parent consent receipts.',
     'ALL_PARENTS', 'EMAIL', 'NORMAL', 'ARCHIVED', false, null, true, 'seed:module09');

INSERT INTO broadcast_delivery_logs (
    delivery_id, broadcast_id, recipient_phone, recipient_email, recipient_user_id,
    channel, delivery_status, gateway_response_id, fallback_triggered, created_by
) VALUES
    ('5eed0009-0002-4000-8000-000000000001', '5eed0009-0001-4000-8000-000000000001',
     '+919876543210', 'parent.rajesh@kvs.local', '5eed0009-0000-4000-8000-000000000004',
     'WHATSAPP', 'DELIVERED', 'WA-GW-998811', false, 'seed:module09'),

    ('5eed0009-0002-4000-8000-000000000002', '5eed0009-0001-4000-8000-000000000002',
     '+919876543210', 'parent.rajesh@kvs.local', '5eed0009-0000-4000-8000-000000000004',
     'SMS', 'DELIVERED', 'SMS-FALLBACK-887711', true, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 3. Student Absence Escalation Nudges (US-09001)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO student_absence_alerts (
    alert_id, tenant_id, student_id, student_name, guardian_phone, guardian_name,
    absence_date, alert_channel, delivery_status, escalated, created_by
) VALUES
    ('5eed0009-0003-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0003-0001-4000-8000-000000000001', 'Aarav Sharma', '+919876543210', 'Rajesh Sharma',
     CURRENT_DATE, 'WHATSAPP', 'SENT', false, 'seed:module09'),

    ('5eed0009-0003-4000-8000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0003-0001-4000-8000-000000000002', 'Ananya Sharma', '+919876543210', 'Rajesh Sharma',
     CURRENT_DATE - INTERVAL '1 day', 'WHATSAPP', 'DELIVERED', true, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 4. Device Registrations & Multi-Child Mappings (US-09002, US-09003, US-09012)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO device_registrations (
    registration_id, tenant_id, user_id, device_token, platform, app_version, is_active, created_by
) VALUES
    ('5eed0009-0004-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', 'fcm-dps-parent-rajesh-token-9988', 'ANDROID', '2.5.0', true, 'seed:module09');

INSERT INTO parent_student_mappings (
    mapping_id, tenant_id, parent_user_id, parent_phone, parent_name,
    student_id, student_name, grade, section, is_primary, relationship, created_by
) VALUES
    ('5eed0009-0004-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', '+919876543210', 'Rajesh Sharma',
     '5eed0003-0001-4000-8000-000000000001', 'Aarav Sharma', '10', 'A', true, 'FATHER', 'seed:module09'),

    ('5eed0009-0004-4000-8000-000000000012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', '+919876543210', 'Rajesh Sharma',
     '5eed0003-0001-4000-8000-000000000002', 'Ananya Sharma', '7', 'B', false, 'FATHER', 'seed:module09');

INSERT INTO notification_preferences (
    preference_id, tenant_id, user_id, whatsapp_enabled, sms_enabled,
    email_enabled, push_enabled, quiet_hours_start, quiet_hours_end, created_by
) VALUES
    ('5eed0009-0004-4000-8000-000000000021', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', true, true, true, true, '22:00', '07:00', 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 5. Teacher-Parent Messaging (US-09004)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO confidential_messages (
    message_id, tenant_id, sender_id, sender_name, sender_role,
    recipient_id, recipient_name, student_id, thread_id, subject, content, is_read, created_by
) VALUES
    ('5eed0009-0005-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', 'Rajesh Sharma', 'ROLE_PARENT',
     '5eed0001-0001-4000-8000-000000000005', 'Dr. Kavya Nair', '5eed0003-0001-4000-8000-000000000001',
     '5eed0009-0004-4000-8000-000000000001', 'Mathematics Chapter 4 Revision Clarification',
     'Dear Teacher, please confirm if quadratic equations formulas will be tested in Monday quiz.', false, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 6. Digital Student Gate Passes & Security Audits (US-09005, US-09006, US-09017)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO student_gate_passes (
    pass_id, tenant_id, student_id, student_name, grade, section, guardian_id, guardian_name,
    pickup_person_name, pickup_person_phone, reason, qr_code, status, guardian_approved, created_by
) VALUES
    ('5eed0009-0006-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0003-0001-4000-8000-000000000001', 'Aarav Sharma', '10', 'A',
     '5eed0009-0000-4000-8000-000000000004', 'Rajesh Sharma', 'Rajesh Sharma', '+919876543210',
     'Scheduled dentist root canal procedure at City Hospital', 'GP-SEED-DEMO-0001', 'ISSUED', true, 'seed:module09');

INSERT INTO gate_audit_logs (
    audit_id, tenant_id, gate_id, gate_name, pass_id, pass_type, person_name,
    event_type, outcome, notes, created_by
) VALUES
    ('5eed0009-0006-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'MAIN_GATE', 'Perimeter Main Gate', '5eed0009-0006-4000-8000-000000000001', 'STUDENT_GATE_PASS',
     'Rajesh Sharma', 'EXIT', 'GRANTED', 'Verified gate pass GP-SEED-DEMO-0001 for student Aarav Sharma', 'seed:module09'),

    ('5eed0009-0006-4000-8000-000000000012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'WEST_GATE', 'West Service Gate', null, 'VISITOR_PASS',
     'Unknown Pedestrian', 'UNAUTHORIZED_ATTEMPT', 'DENIED', 'Turned away due to lack of photo ID document', 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 7. Visitor Appointments & Meeting Slots (US-09007, US-09016)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO visitor_appointments (
    appointment_id, tenant_id, visitor_name, visitor_phone, visitor_id_type, visitor_id_number,
    purpose, host_staff_id, host_staff_name, appointment_time, badge_number, status, created_by
) VALUES
    ('5eed0009-0007-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Suresh Singhania', '+919811002233', 'AADHAAR', '987654321098',
     'New student admission inquiry for Grade 11 Science', '5eed0001-0001-4000-8000-000000000002',
     'Principal Dr. Sunita Rao', CURRENT_TIMESTAMP + INTERVAL '2 hours', 'VIS-0901', 'SCHEDULED', 'seed:module09');

INSERT INTO appointment_slots (
    slot_id, tenant_id, host_user_id, host_name, slot_date, start_time, end_time, is_booked, created_by
) VALUES
    ('5eed0009-0007-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0001-0001-4000-8000-000000000002', 'Principal Dr. Sunita Rao',
     CURRENT_DATE + INTERVAL '2 days', '10:00', '10:30', false, 'seed:module09'),

    ('5eed0009-0007-4000-8000-000000000012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0001-0001-4000-8000-000000000002', 'Principal Dr. Sunita Rao',
     CURRENT_DATE + INTERVAL '2 days', '11:00', '11:30', true, 'seed:module09');

-- ---------------------------------------------------------------------------------------------------
-- 8. Calendar Events, Grievances, Bulletins, Surveys & Documents (US-09008, 09, 13, 14, 15)
-- ---------------------------------------------------------------------------------------------------
INSERT INTO calendar_events (
    event_id, tenant_id, title, description, event_type, start_date, end_date, venue, target_audience, is_published, created_by
) VALUES
    ('5eed0009-0008-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Diwali Vacation Break 2026', 'Campus remains closed for auspicious Diwali celebrations.',
     'HOLIDAY', CURRENT_DATE + INTERVAL '10 days', CURRENT_DATE + INTERVAL '17 days', 'Campus', 'ALL', true, 'seed:module09');

INSERT INTO grievance_tickets (
    ticket_id, tenant_id, parent_user_id, parent_name, student_id, student_name,
    category, priority, subject, description, status, sla_due_hours, created_by
) VALUES
    ('5eed0009-0009-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0000-4000-8000-000000000004', 'Rajesh Sharma', '5eed0003-0001-4000-8000-000000000001', 'Aarav Sharma',
     'TRANSPORT', 'HIGH', 'Bus 14 Morning Arrival Delay',
     'Bus 14 has arrived 20 minutes late three days in a row at Mayur Vihar stop.', 'OPEN', 24, 'seed:module09');

INSERT INTO daily_bulletin_posts (
    bulletin_id, tenant_id, bulletin_type, title, content_details, effective_date, meal_type, created_by
) VALUES
    ('5eed0009-0013-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'CAFETERIA_MENU', 'Today Special Nutritious Lunch',
     'Paneer Makhani, Multigrain Chapatis, Jeera Pulao, Cucumber Mint Raita, Sweet Kheer', CURRENT_DATE, 'LUNCH', 'seed:module09');

INSERT INTO feedback_surveys (
    survey_id, tenant_id, title, description, survey_type, is_active, created_by
) VALUES
    ('5eed0009-0014-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'Parent Satisfaction Survey: STEM Lab Facilities',
     'Please evaluate the recently upgraded AI and Robotics laboratory equipment and curriculum.',
     'ACADEMIC', true, 'seed:module09');

INSERT INTO survey_responses (
    response_id, tenant_id, survey_id, parent_user_id, parent_name, rating_score, comments, created_by
) VALUES
    ('5eed0009-0014-4000-8000-000000000011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     '5eed0009-0014-4000-8000-000000000001', '5eed0009-0000-4000-8000-000000000004',
     'Rajesh Sharma', 5, 'My son Aarav has developed immense interest in robotics coding. Superb faculty guidance!', 'seed:module09');

INSERT INTO document_notifications (
    notification_id, tenant_id, document_type, document_id, student_id,
    parent_phone, parent_email, download_url, delivery_status, created_by
) VALUES
    ('5eed0009-0015-4000-8000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'FEE_RECEIPT', 'DPS-REC-2026-Q1-0044', '5eed0003-0001-4000-8000-000000000001',
     '+919876543210', 'parent.rajesh@kvs.local',
     'https://cdn.dps.edu.in/receipts/2026/DPS-REC-2026-Q1-0044.pdf', 'DELIVERED', 'seed:module09');

COMMIT;
