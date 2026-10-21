# School ERP - Module 09: Stakeholder Communication, WhatsApp/SMS Gateway & Parent App - API Documentation

{info}
Service `communicationservice` · package `com.kvs.communication` · port `8090` (test `18090`) · base URL `http://localhost:8090/communicationservice/api/v1/communication`.
Shared PostgreSQL database `authservice`; handles WhatsApp/SMS broadcast dispatching, 30-minute absence alerts, parent mobile application integration, multi-child profile switching, teacher-parent confidential messaging, digital student gate passes with QR verification, visitor appointment scheduling, digital school calendars, and parent grievance ticketing with SLA tracking.
{info}

h2. Conventions

* *Base path*: `/api/v1/communication`.
* *Authentication*: `Authorization: Bearer <authservice token>`. The token's `tenantId` claim isolates institutional data; `userId` identifies parents, teachers, and security staff.
* *Envelope*: success `{ success: true, message, data, traceId, timestamp }`; error `{ success: false, status, errorCode, message, path, traceId }`. `X-Trace-Id` is echoed in headers and payload.
* *Errors*: `400 VALIDATION_FAILED` / `MALFORMED_REQUEST`, `401 UNAUTHORIZED`, `403 FORBIDDEN`, `404 RESOURCE_NOT_FOUND`, `409 Conflict`.
* *Roles*: Enforced via Spring Security `@PreAuthorize(CommunicationRoles.<RULE>)`. `SUPER_ADMIN` has global tenant-crossing override.

h2. Roles Matrix (BRD Actor → Spring Role)

||BRD Actor||Role||Description||
|Public Relations Officer|`ROLE_PUBLIC_RELATIONS_OFFICER`|Broadcast circulars, calendar events, surveys, compliance archives|
|School Principal|`ROLE_PRINCIPAL`|Emergency weather broadcasts, pinned app circulars, leadership meeting slots|
|System Automation / Attendance Officer|`ROLE_SYSTEM_AUTOMATION`|30-minute absence escalations, delivery status fallbacks, document alerts|
|Parent|`ROLE_PARENT`|Mobile app profile switching, teacher messaging, early gate passes, grievance tickets|
|Faculty Member / Teacher|`ROLE_TEACHER`|Two-way confidential parent-teacher messaging and student consultations|
|Receptionist / Front-Office Counselor|`ROLE_RECEPTIONIST`|Visitor appointment booking, digital badge check-in, cafeteria & bus bulletins|
|Security Guard|`ROLE_SECURITY_GUARD`|Gate pass QR scanner verification, perimeter security audit logging|
|Accountant|`ROLE_ACCOUNTANT`|Automated digital fee receipt alert notifications|

---

{expand:title=US-09000: Broadcast official school circulars simultaneously via WhatsApp, SMS, and App Push}
*Primary Actor*: Public Relations Officer (`ROLE_PUBLIC_RELATIONS_OFFICER`, `ROLE_ADMIN`)

Dispatches official school announcements and circulars simultaneously across multiple delivery channels (WhatsApp, SMS, App Push, Email) with targeted audience segmentation (All Parents, Grade 10 Parents, All Teachers).

*Business Rules*
* Validates `title`, `messageBody`, and `channel`.
* Creates initial recipient delivery log entries with status `SENT`.
* Logs audit trail `CREATE_BROADCAST`.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/broadcasts`|PUBLIC_RELATIONS_OFFICER, PRINCIPAL, SUPER_ADMIN|
|GET|`/api/v1/communication/broadcasts`|All authenticated users|
|GET|`/api/v1/communication/broadcasts/{broadcastId}`|All authenticated users|

*Request Example (`POST /api/v1/communication/broadcasts`)*
{code:language=json}
{
  "tenantId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "senderUserId": "5eed0009-0010-4000-8000-000000000001",
  "senderName": "Ananya Sen (PRO)",
  "title": "Annual Day Celebration 2026",
  "messageBody": "Annual Day will be celebrated on December 15. All parents are invited.",
  "targetAudience": "ALL_PARENTS",
  "channel": "MULTI_CHANNEL",
  "priority": "NORMAL"
}
{code}
{expand}

{expand:title=US-09001: Dispatch automated alerts to guardians within 30 minutes of unexcused student absence}
*Primary Actor*: System Automation / Attendance Officer (`ROLE_SYSTEM_AUTOMATION`, `ROLE_ADMIN`)

Monitors daily classroom morning attendance roll-calls and dispatches automated WhatsApp/SMS alerts to registered guardians within 30 minutes of unexcused student absence. Supports automated escalation to the grade counselor.

*Business Rules*
* Alerts require valid `studentId`, `guardianPhone`, and `absenceDate`.
* Allows automated escalation flagging when guardian does not acknowledge within threshold.
* Audit log `DISPATCH_ABSENCE_ALERT` recorded.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/absence-alerts`|SYSTEM_AUTOMATION, ATTENDANCE_OFFICER, SUPER_ADMIN|
|POST|`/api/v1/communication/absence-alerts/{alertId}/escalate`|SYSTEM_AUTOMATION, ATTENDANCE_OFFICER, SUPER_ADMIN|
|GET|`/api/v1/communication/absence-alerts`|SYSTEM_AUTOMATION, ATTENDANCE_OFFICER, SUPER_ADMIN|
{expand}

{expand:title=US-09002: Deliver native iOS and Android mobile apps for parents with real-time updates}
*Primary Actor*: Parent / Mobile User (`ROLE_PARENT`, `ROLE_STAFF`, authenticated)

Registers Firebase Cloud Messaging (FCM) or Apple Push Notification Service (APNS) device tokens with device platform and OS version to enable real-time push alerts.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/mobile/devices`|All authenticated users|
{expand}

{expand:title=US-09003: Enable multi-child profile switching under single parent credentials}
*Primary Actor*: Parent (`ROLE_PARENT`, `ROLE_ADMIN`)

Allows parents with multiple enrolled children to link profiles and switch seamlessly between active student contexts without logging out.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/mobile/child-mappings`|PARENT, SUPER_ADMIN|
|GET|`/api/v1/communication/mobile/parents/{parentUserId}/children`|PARENT, SUPER_ADMIN|
{expand}

{expand:title=US-09004: Facilitate secure two-way messaging between parents and class teachers}
*Primary Actor*: Parent / Teacher (`ROLE_PARENT`, `ROLE_TEACHER`)

Provides a confidential, student-anchored two-way messaging channel between guardians and subject/class teachers.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/messages`|PARENT, TEACHER, SUPER_ADMIN|
|GET|`/api/v1/communication/messages/threads/{threadId}`|PARENT, TEACHER, SUPER_ADMIN|
|GET|`/api/v1/communication/messages/inbox/{recipientId}`|PARENT, TEACHER, SUPER_ADMIN|
|PUT|`/api/v1/communication/messages/{messageId}/read`|PARENT, TEACHER, SUPER_ADMIN|
{expand}

{expand:title=US-09005: Generate digital gate passes on parent apps for early student pickups}
*Primary Actor*: Parent (`ROLE_PARENT`, `ROLE_RECEPTIONIST`)

Generates a secure early pickup student gate pass with an encrypted digital QR code token, authorized pickup person identity, and emergency reason.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/gate-passes`|PARENT, RECEPTIONIST, SUPER_ADMIN|
|GET|`/api/v1/communication/gate-passes`|All authenticated users|
{expand}

{expand:title=US-09006: Scan gate pass QR codes at security gates to authorize student departure}
*Primary Actor*: Security Guard (`ROLE_SECURITY_GUARD`, `ROLE_ADMIN`)

Scans and validates gate pass QR codes at perimeter campus security gates. Verifies authorization status and marks the pass as `DEPARTED` with timestamp and guard identity.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/gate-passes/verify`|SECURITY_GUARD, SUPER_ADMIN|
{expand}

{expand:title=US-09007: Register visitor appointments and issue digital visitor passes at reception}
*Primary Actor*: Receptionist (`ROLE_RECEPTIONIST`, `ROLE_ADMIN`)

Registers campus visitor appointments, records government photo ID documents, host staff details, and issues digital visitor badges.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/visitors/appointments`|RECEPTIONIST, SUPER_ADMIN|
|PUT|`/api/v1/communication/visitors/appointments/{appointmentId}/check-in`|RECEPTIONIST, SUPER_ADMIN|
|PUT|`/api/v1/communication/visitors/appointments/{appointmentId}/check-out`|RECEPTIONIST, SUPER_ADMIN|
|GET|`/api/v1/communication/visitors/appointments`|RECEPTIONIST, SUPER_ADMIN|
{expand}

{expand:title=US-09008: Publish school calendar events, exam schedules, and photo galleries}
*Primary Actor*: Public Relations Officer (`ROLE_PUBLIC_RELATIONS_OFFICER`, `ROLE_ADMIN`)

Publishes official academic calendars, examination dates, cultural event milestones, and cloud photo gallery links.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/community/calendar-events`|PUBLIC_RELATIONS_OFFICER, PRINCIPAL, SUPER_ADMIN|
|GET|`/api/v1/communication/community/calendar-events`|All authenticated users|
{expand}

{expand:title=US-09009: Log parent grievance support tickets with automated SLA escalation rules}
*Primary Actor*: Parent (`ROLE_PARENT`, `ROLE_RECEPTIONIST`, `ROLE_ADMIN`)

Logs support tickets regarding transport, academics, fees, or campus facilities with automated SLA resolution deadlines (e.g. 24h, 48h) and status workflows.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/community/grievances`|PARENT, SUPER_ADMIN|
|PUT|`/api/v1/communication/community/grievances/{ticketId}`|RECEPTIONIST, SUPER_ADMIN|
|GET|`/api/v1/communication/community/grievances`|RECEPTIONIST, SUPER_ADMIN|
{expand}

{expand:title=US-09010: Send emergency weather closure announcements to all school stakeholders}
*Primary Actor*: School Principal (`ROLE_PRINCIPAL`, `ROLE_ADMIN`)

Sends instant, high-priority emergency notifications (severe weather, public emergency) with multi-channel override and automated 24-hour feed pinning.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/broadcasts/emergency`|PRINCIPAL, SUPER_ADMIN|
{expand}

{expand:title=US-09011: Track message delivery statuses and fallback from WhatsApp to SMS on failure}
*Primary Actor*: System Automation / Teacher (`ROLE_SYSTEM_AUTOMATION`, `ROLE_ADMIN`)

Monitors delivery status (SENT, DELIVERED, READ, FAILED) across third-party communication gateways and triggers automatic fallback from WhatsApp to SMS upon failure.

||Method||Path||Allowed Roles||
|PUT|`/api/v1/communication/broadcasts/delivery/{deliveryId}`|SYSTEM_AUTOMATION, SUPER_ADMIN|
|GET|`/api/v1/communication/broadcasts/{broadcastId}/delivery-logs`|PUBLIC_RELATIONS_OFFICER, SUPER_ADMIN|
{expand}

{expand:title=US-09012: Configure notification preferences for parents to manage alert channels}
*Primary Actor*: Parent (`ROLE_PARENT`, `ROLE_ADMIN`)

Allows parents to configure channel preferences (WhatsApp, SMS, Email, Push) and set quiet hours during which non-urgent notifications are suppressed.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/mobile/preferences`|PARENT, SUPER_ADMIN|
|GET|`/api/v1/communication/mobile/preferences`|PARENT, SUPER_ADMIN|
{expand}

{expand:title=US-09013: Publish daily cafeteria menus and school bus schedule updates}
*Primary Actor*: Receptionist / Administrator (`ROLE_RECEPTIONIST`, `ROLE_ADMIN`)

Publishes daily lunch/snack nutritional menus and transport schedule changes for instant parental visibility.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/community/bulletins`|RECEPTIONIST, SUPER_ADMIN|
|GET|`/api/v1/communication/community/bulletins`|All authenticated users|
{expand}

{expand:title=US-09014: Collect parent feedback surveys on school events and academic programs}
*Primary Actor*: Public Relations Officer / Parent (`ROLE_PUBLIC_RELATIONS_OFFICER`, `ROLE_PARENT`)

Creates structured feedback surveys and collects parent ratings (1 to 5 stars) and qualitative feedback comments.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/community/surveys`|PUBLIC_RELATIONS_OFFICER, SUPER_ADMIN|
|POST|`/api/v1/communication/community/surveys/responses`|PARENT, SUPER_ADMIN|
|GET|`/api/v1/communication/community/surveys/{surveyId}/responses`|PUBLIC_RELATIONS_OFFICER, SUPER_ADMIN|
{expand}

{expand:title=US-09015: Notify parents instantly when digital fee receipts or report cards are issued}
*Primary Actor*: Accountant / System Automation (`ROLE_SYSTEM_AUTOMATION`, `ROLE_ACCOUNTANT`)

Sends automated instant alerts with secure expiring PDF download links whenever a fee receipt or academic term report card is generated.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/community/documents/notifications`|SYSTEM_AUTOMATION, ACCOUNTANT, SUPER_ADMIN|
|GET|`/api/v1/communication/community/documents/notifications`|All authenticated users|
{expand}

{expand:title=US-09016: Manage receptionist appointment booking slots for principal meetings}
*Primary Actor*: Receptionist / Parent (`ROLE_RECEPTIONIST`, `ROLE_PARENT`)

Creates available 30-minute meeting slots with school leadership and allows parents to book reserved appointment slots.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/visitors/slots`|RECEPTIONIST, SUPER_ADMIN|
|POST|`/api/v1/communication/visitors/slots/{slotId}/book`|All authenticated users|
|GET|`/api/v1/communication/visitors/slots`|All authenticated users|
{expand}

{expand:title=US-09017: Audit security gate entry and exit logs for unauthorized visitor attempts}
*Primary Actor*: Security Guard (`ROLE_SECURITY_GUARD`, `ROLE_ADMIN`)

Audits perimeter gate security events and logs denied attempts for unverified visitors or students without valid departure gate passes.

||Method||Path||Allowed Roles||
|POST|`/api/v1/communication/gate-passes/audit-logs`|SECURITY_GUARD, SUPER_ADMIN|
|GET|`/api/v1/communication/gate-passes/audit-logs`|SECURITY_GUARD, SUPER_ADMIN|
{expand}

{expand:title=US-09018: Pin urgent announcements to the top of parent mobile app feeds}
*Primary Actor*: School Principal (`ROLE_PRINCIPAL`, `ROLE_ADMIN`)

Pins critical school announcements to a sticky top banner on the parent mobile application feed with configurable expiration dates.

||Method||Path||Allowed Roles||
|PUT|`/api/v1/communication/broadcasts/{broadcastId}/pin`|PRINCIPAL, SUPER_ADMIN|
|GET|`/api/v1/communication/broadcasts/pinned`|All authenticated users|
{expand}

{expand:title=US-09019: Archive past communication broadcasts for regulatory compliance records}
*Primary Actor*: Public Relations Officer (`ROLE_PUBLIC_RELATIONS_OFFICER`, `ROLE_ADMIN`)

Archives completed broadcast circulars for statutory regulatory audits and retrieves historical communication records.

||Method||Path||Allowed Roles||
|PUT|`/api/v1/communication/broadcasts/{broadcastId}/archive`|PUBLIC_RELATIONS_OFFICER, SUPER_ADMIN|
|GET|`/api/v1/communication/broadcasts/archived`|PUBLIC_RELATIONS_OFFICER, SUPER_ADMIN|
{expand}
