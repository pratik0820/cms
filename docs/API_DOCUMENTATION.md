# Class Management System API Documentation

## 1. Document Purpose

This document maps the current backend APIs in this repository and the remaining APIs that need to be created to support the full application shown in the Figma screens.

This document is meant for:
- Backend developers
- Frontend developers
- Product and QA teams

It answers:
- Which APIs already exist
- Which APIs are still missing
- Why each API exists
- Where each API is used in the application
- What request and response contract should be used

## 2. Scope

This document is based on:
- The current backend code in this repository
- The Figma screenshots shared for Admin, Teacher, and Student portals

Status labels used in this document:
- `CREATED`: API already exists in backend
- `PARTIAL`: API or flow exists, but is incomplete
- `PROPOSED`: API does not exist yet and should be created

## 3. Base URL and Common Standards

### Base URL

All REST APIs are served under:

```text
http://localhost:8095/cms
```

Example:

```text
http://localhost:8095/cms/api/auth/login
```

### Common Headers

For public endpoints:

```http
Content-Type: application/json
```

For authenticated endpoints:

```http
Content-Type: application/json
Authorization: Bearer <access_token>
```

For multipart upload endpoints:

```http
Authorization: Bearer <access_token>
Content-Type: multipart/form-data
```

### Standard Response Envelope

Most current APIs follow this response shape:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {},
  "errorCode": null,
  "timestamp": "2026-04-25T20:00:00"
}
```

### Pagination Response Shape

```json
{
  "success": true,
  "message": null,
  "data": {
    "content": [],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "timestamp": "2026-04-25T20:00:00"
}
```

### Authentication Model Notes

Current backend behavior:
- Staff users log in using email
- Student creation generates a `loginId`
- Student and parent portal in Figma should use `loginId`, but current login API still expects email

This means the student and parent login flow is currently not fully compatible with the Figma experience.

## 4. Current Created APIs

## 4.1 Tenant Registration and Institute Bootstrap

### API: Register Tenant / Institute

- Status: `CREATED`
- Purpose: Create a new institute tenant and its default owner account
- Description: Creates tenant, default branch, and institute owner login
- Endpoint: `POST /api/tenants/register`
- Headers:
  - `Content-Type: application/json`
- Payload:

```json
{
  "instituteName": "Greenfield Public School",
  "subdomain": "greenfield",
  "adminEmail": "owner@greenfield.com",
  "adminPassword": "Password@123",
  "adminName": "Institute Owner",
  "contactPhone": "9876543210",
  "planType": "STARTER"
}
```

- Response:

```json
{
  "success": true,
  "message": "Institute registered successfully. Check your email for login details.",
  "data": {
    "id": "tenant-uuid",
    "name": "Greenfield Public School",
    "subdomain": "greenfield",
    "contactEmail": "owner@greenfield.com",
    "contactPhone": "9876543210",
    "planType": "STARTER",
    "isActive": true,
    "maxStudents": 100,
    "maxBranches": 1
  }
}
```

- Where to use:
  - SaaS onboarding screen
  - Institute registration flow
  - Super admin onboarding flow

### API: Get Tenant by ID

- Status: `CREATED`
- Purpose: Fetch tenant details
- Description: Available for platform-level admin use
- Endpoint: `GET /api/tenants/{tenantId}`
- Headers:
  - `Authorization: Bearer <token>`
- Path Parameters:
  - `tenantId`: UUID
- Response:
  - Tenant object
- Where to use:
  - Platform admin panel
  - Tenant management console

### API: Activate Tenant

- Status: `CREATED`
- Purpose: Enable institute account
- Description: Activates a tenant from the platform side
- Endpoint: `POST /api/tenants/{tenantId}/activate`
- Headers:
  - `Authorization: Bearer <token>`
- Response:

```json
{
  "success": true,
  "message": "Tenant activated",
  "data": null
}
```

- Where to use:
  - Platform admin tenant controls

### API: Deactivate Tenant

- Status: `CREATED`
- Purpose: Disable institute account
- Description: Deactivates a tenant from the platform side
- Endpoint: `POST /api/tenants/{tenantId}/deactivate`
- Headers:
  - `Authorization: Bearer <token>`
- Response:

```json
{
  "success": true,
  "message": "Tenant deactivated",
  "data": null
}
```

- Where to use:
  - Platform admin tenant controls

## 4.2 Authentication APIs

### API: Login

- Status: `CREATED`
- Purpose: Authenticate user and issue JWT tokens
- Description: Logs in admin, teacher, or other staff using email and password
- Endpoint: `POST /api/auth/login`
- Headers:
  - `Content-Type: application/json`
- Payload:

```json
{
  "email": "admin@school.com",
  "password": "Password@123",
  "tenantSubdomain": "greenfield",
  "fcmToken": "optional-device-token"
}
```

- Response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "refresh-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "user": {
      "id": "user-uuid",
      "email": "admin@school.com",
      "fullName": "Admin User",
      "roles": ["ADMIN"],
      "tenantId": "tenant-uuid",
      "tenantName": "Greenfield Public School",
      "branchId": "branch-uuid",
      "branchName": "Main"
    }
  }
}
```

- Where to use:
  - Admin login
  - Teacher login
  - Institute owner login

### API: Refresh Token

- Status: `CREATED`
- Purpose: Generate new access token
- Description: Uses refresh token to rotate session and get new access token
- Endpoint: `POST /api/auth/refresh`
- Headers:
  - `Content-Type: application/json`
- Payload:

```json
{
  "refreshToken": "existing-refresh-token"
}
```

- Response:
  - Same response shape as login
- Where to use:
  - Frontend auth interceptor
  - Silent session renewal

### API: Logout

- Status: `CREATED`
- Purpose: Revoke current refresh token
- Description: Logs out from current device
- Endpoint: `POST /api/auth/logout`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "refreshToken": "existing-refresh-token"
}
```

- Response:

```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

- Where to use:
  - Logout button
  - Session termination

### API: Logout All Devices

- Status: `CREATED`
- Purpose: Revoke all refresh tokens for current user
- Description: Logs user out from all devices
- Endpoint: `POST /api/auth/logout-all`
- Headers:
  - `Authorization: Bearer <token>`
- Response:

```json
{
  "success": true,
  "message": "Logged out from all devices",
  "data": null
}
```

- Where to use:
  - Security settings
  - Profile account controls

### API: Change Password

- Status: `CREATED`
- Purpose: Change password for current user
- Description: Requires current password
- Endpoint: `PUT /api/auth/change-password`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "currentPassword": "OldPassword@123",
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```

- Response:

```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

- Where to use:
  - My profile
  - Security settings

### API: Get Current User

- Status: `CREATED`
- Purpose: Fetch current user profile basics
- Description: Returns role and tenant context for route guards and UI shell
- Endpoint: `GET /api/auth/me`
- Headers:
  - `Authorization: Bearer <token>`
- Response:

```json
{
  "success": true,
  "data": {
    "id": "user-uuid",
    "email": "admin@school.com",
    "roles": ["ADMIN"],
    "tenantId": "tenant-uuid",
    "branchId": "branch-uuid"
  }
}
```

- Where to use:
  - Frontend bootstrap
  - Sidebar and role-based navigation
  - Route protection

## 4.3 User Management APIs

## 4.3.1 Admin APIs

### API: Create Admin

- Status: `CREATED`
- Purpose: Create institute admin user
- Description: Creates admin account under current tenant
- Endpoint: `POST /api/users/admins`
- Headers:
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- Payload:

```json
{
  "fullName": "Branch Admin",
  "email": "branch.admin@school.com",
  "phone": "9876543210",
  "branchId": "branch-uuid"
}
```

- Response:

```json
{
  "success": true,
  "message": "Admin account created successfully.",
  "data": {
    "id": "admin-uuid",
    "fullName": "Branch Admin",
    "email": "branch.admin@school.com",
    "phone": "9876543210",
    "role": "ADMIN",
    "branchId": "branch-uuid",
    "branchName": "Main",
    "isActive": true,
    "createdAt": "2026-04-25T20:00:00"
  }
}
```

- Where to use:
  - Admin management
  - System settings
  - Institute staff setup

## 4.3.2 Teacher APIs

### API: Create Teacher

- Status: `CREATED`
- Purpose: Register teacher and login account
- Description: Creates teacher profile and linked user account
- Endpoint: `POST /api/users/teachers`
- Headers:
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- Payload:

```json
{
  "fullName": "Rahul Sharma",
  "email": "rahul.sharma@school.com",
  "phone": "9876543210",
  "qualification": "M.Sc., B.Ed.",
  "joiningDate": "2026-04-01",
  "hourlyRate": 500,
  "branchId": "branch-uuid"
}
```

- Response:

```json
{
  "success": true,
  "message": "Teacher registered. Share the generated credentials with the teacher.",
  "data": {
    "id": "teacher-uuid",
    "userId": "user-uuid",
    "fullName": "Rahul Sharma",
    "email": "rahul.sharma@school.com",
    "phone": "9876543210",
    "qualification": "M.Sc., B.Ed.",
    "joiningDate": "2026-04-01",
    "hourlyRate": 500,
    "branchId": "branch-uuid",
    "branchName": "Main",
    "isActive": true,
    "generatedLoginEmail": "rahul.sharma@school.com",
    "generatedPassword": "TempPass@123"
  }
}
```

- Where to use:
  - Teacher add form
  - Teacher management

### API: List Teachers

- Status: `CREATED`
- Purpose: Fetch teachers with pagination
- Description: Lists teacher records for current tenant
- Endpoint: `GET /api/users/teachers?page=0&size=20`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `page`
  - `size`
- Response:
  - Paginated teacher list
- Where to use:
  - Admin teacher list screen
  - Teacher selection dropdowns

### API: Get Teacher Details

- Status: `CREATED`
- Purpose: Fetch single teacher details
- Description: Returns teacher information by ID
- Endpoint: `GET /api/users/teachers/{id}`
- Headers:
  - `Authorization: Bearer <token>`
- Path Parameters:
  - `id`: teacher UUID
- Response:
  - Teacher detail object
- Where to use:
  - Teacher profile drawer
  - Teacher details page

### API: Reset Teacher Password

- Status: `CREATED`
- Purpose: Reset teacher password by admin
- Description: Admin override password reset
- Endpoint: `POST /api/users/teachers/{id}/reset-password`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```

- Response:

```json
{
  "success": true,
  "message": "Teacher password reset successfully.",
  "data": null
}
```

- Where to use:
  - Teacher profile actions
  - Admin recovery actions

### API: Deactivate Teacher

- Status: `CREATED`
- Purpose: Disable teacher account
- Description: Blocks teacher login
- Endpoint: `POST /api/users/teachers/{id}/deactivate`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Success envelope
- Where to use:
  - Teacher status controls

### API: Activate Teacher

- Status: `CREATED`
- Purpose: Re-enable teacher account
- Description: Restores teacher login access
- Endpoint: `POST /api/users/teachers/{id}/activate`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Success envelope
- Where to use:
  - Teacher status controls

## 4.3.3 Student APIs

### API: Create Student

- Status: `CREATED`
- Purpose: Enroll student and create linked login
- Description: Creates student in draft admission state
- Endpoint: `POST /api/users/students`
- Headers:
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- Payload:

```json
{
  "name": "Aarav Singh",
  "dateOfBirth": "2012-03-14",
  "gender": "Male",
  "mobile": "9876543210",
  "parentName": "Rahul Singh",
  "parentPhone": "9876543211",
  "email": "aarav@example.com",
  "address": "21 Main Street",
  "schoolName": "Greenfield Public School",
  "standard": "8th",
  "board": "CBSE",
  "branchId": "branch-uuid",
  "batchId": "batch-uuid"
}
```

- Response:

```json
{
  "success": true,
  "message": "Student enrolled in DRAFT state. Upload photo and then finalise admission.",
  "data": {
    "id": "student-uuid",
    "userId": "user-uuid",
    "name": "Aarav Singh",
    "loginId": "STU-A3X9KL",
    "email": "aarav@example.com",
    "mobile": "9876543210",
    "parentName": "Rahul Singh",
    "parentPhone": "9876543211",
    "standard": "8th",
    "board": "CBSE",
    "schoolName": "Greenfield Public School",
    "isAdmissionFinal": false,
    "branchId": "branch-uuid",
    "branchName": "Main",
    "generatedLoginId": "STU-A3X9KL",
    "generatedPassword": "TempPass@123",
    "parentLoginNote": "Parent can use the same Login ID and Password to access the parent portal."
  }
}
```

- Where to use:
  - Add student form
  - Admission conversion flow

### API: List Students

- Status: `CREATED`
- Purpose: Fetch student list
- Description: Returns paginated student records
- Endpoint: `GET /api/users/students?page=0&size=20`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `page`
  - `size`
- Response:
  - Paginated student list
- Where to use:
  - Students listing page
  - Admission management

### API: Get Student Details

- Status: `CREATED`
- Purpose: Fetch one student record
- Description: Returns detailed student information
- Endpoint: `GET /api/users/students/{id}`
- Headers:
  - `Authorization: Bearer <token>`
- Path Parameters:
  - `id`: student UUID
- Response:
  - Student detail object
- Where to use:
  - Student detail drawer
  - Student overview tab

### API: Upload Student Photo

- Status: `PARTIAL`
- Purpose: Upload student photo required before admission finalisation
- Description: Mentioned in flow, but controller endpoint is currently commented out
- Proposed Final Endpoint: `POST /api/users/students/{id}/photo`
- Headers:
  - `Authorization: Bearer <token>`
  - `Content-Type: multipart/form-data`
- Payload:
  - Form field: `photo`
- Response:

```json
{
  "success": true,
  "message": "Photo uploaded successfully.",
  "data": "https://cdn.example.com/student-photo.jpg"
}
```

- Where to use:
  - Add student form
  - Admission edit flow

### API: Finalise Student Admission

- Status: `CREATED`
- Purpose: Mark admission as complete
- Description: Requires uploaded photo
- Endpoint: `POST /api/users/students/{id}/finalise`
- Headers:
  - `Authorization: Bearer <token>`
- Path Parameters:
  - `id`: student UUID
- Response:
  - Updated student response object
- Where to use:
  - Admission completion flow
  - Add student save workflow

### API: Reset Student Password

- Status: `CREATED`
- Purpose: Reset student login
- Description: Resets shared student/parent credentials
- Endpoint: `POST /api/users/students/{id}/reset-password`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```

- Response:
  - Success envelope
- Where to use:
  - Student profile actions
  - Parent access recovery

### API: Deactivate Student

- Status: `CREATED`
- Purpose: Disable student and parent access
- Description: Blocks login
- Endpoint: `POST /api/users/students/{id}/deactivate`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Success envelope
- Where to use:
  - Student status actions

### API: Activate Student

- Status: `CREATED`
- Purpose: Restore student access
- Description: Reactivates student account
- Endpoint: `POST /api/users/students/{id}/activate`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Success envelope
- Where to use:
  - Student status actions

## 4.4 Notification APIs

### API: Get Notifications

- Status: `CREATED`
- Purpose: Fetch user notifications
- Description: Returns paginated in-app notifications
- Endpoint: `GET /api/v1/notifications?page=0&size=20`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `page`
  - `size`
- Response:
  - Paginated notification list
- Where to use:
  - Notification page
  - Notification drawer

### API: Get Unread Notification Count

- Status: `CREATED`
- Purpose: Badge counter
- Description: Returns unread count for current user
- Endpoint: `GET /api/v1/notifications/unread-count`
- Headers:
  - `Authorization: Bearer <token>`
- Response:

```json
{
  "success": true,
  "data": 7
}
```

- Where to use:
  - Header bell icon
  - Sidebar notification badge

### API: Mark Notification as Read

- Status: `CREATED`
- Purpose: Update single notification status
- Description: Marks one notification as read
- Endpoint: `PATCH /api/v1/notifications/{id}/read`
- Headers:
  - `Authorization: Bearer <token>`
- Path Parameters:
  - `id`: notification UUID
- Response:
  - Success envelope
- Where to use:
  - Notification list item action

### API: Mark All Notifications as Read

- Status: `CREATED`
- Purpose: Bulk notification update
- Description: Marks all notifications as read
- Endpoint: `PATCH /api/v1/notifications/mark-all-read`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Success envelope
- Where to use:
  - Notification center toolbar

## 4.5 WebSocket API

### WebSocket: Real-Time Notifications

- Status: `CREATED`
- Purpose: Push real-time events to frontend
- Description: STOMP WebSocket endpoint for live notifications
- Endpoint: `/ws`
- Protocol:
  - SockJS / STOMP
- Subscribe Topics:
  - `/topic/user/{userId}/notifications`
- Where to use:
  - Notification badge refresh
  - Live activity updates

## 5. Missing APIs Required by Figma

This section defines the APIs that need to be created to support the Figma screens.

## 5.1 Master Data APIs

These should be created first because many screens depend on them.

### API: List Branches

- Status: `PROPOSED`
- Purpose: Populate branch selector
- Description: Returns branch list for current tenant
- Endpoint: `GET /api/branches`
- Headers:
  - `Authorization: Bearer <token>`
- Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "branch-uuid",
      "name": "Main Branch",
      "city": "Delhi",
      "isActive": true
    }
  ]
}
```

- Where to use:
  - Header branch switcher
  - Student form
  - Teacher form
  - Reports filters

### API: List Batches / Classes / Sections

- Status: `PROPOSED`
- Purpose: Populate class and batch selectors
- Description: Returns classes/batches by branch and academic year
- Endpoint: `GET /api/batches`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `branchId`
  - `academicYear`
  - `standard`
- Response:
  - Batch list with standard, section, board, class teacher
- Where to use:
  - Timetable
  - Attendance
  - Tests
  - Homework
  - Students filters

### API: List Subjects

- Status: `PROPOSED`
- Purpose: Populate subject selectors
- Description: Returns subjects by class/batch or institute
- Endpoint: `GET /api/subjects`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `batchId`
- Response:
  - Subject list
- Where to use:
  - Timetable
  - Syllabus
  - Homework
  - Tests
  - Teacher weekly plan

## 5.2 Dashboard APIs

### API: Admin Dashboard Summary

- Status: `PROPOSED`
- Purpose: Populate admin dashboard KPI cards and widgets
- Description: Returns counts, pending tasks, activity feed, attendance summary, fee summary, and smart insights
- Endpoint: `GET /api/dashboard/admin`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `branchId` optional
  - `date` optional
- Response:

```json
{
  "success": true,
  "data": {
    "kpis": {
      "todayAdmissions": 12,
      "feeCollectedToday": 45600,
      "activeClasses": 28,
      "teachersLive": 14,
      "pendingFees": 136,
      "newLeads": 23
    },
    "pendingTasks": [],
    "liveActivity": [],
    "attendanceSnapshot": {
      "totalStudents": 735,
      "present": 612,
      "absent": 75,
      "late": 48
    },
    "feeSummary": {},
    "testPerformance": {},
    "syllabusProgress": {},
    "stationeryStatus": {},
    "smartInsights": []
  }
}
```

- Where to use:
  - Admin dashboard home

### API: Teacher Dashboard Summary

- Status: `PROPOSED`
- Purpose: Populate teacher dashboard
- Description: Returns next class, today schedule, recent activity, pending work
- Endpoint: `GET /api/dashboard/teacher`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Teacher dashboard summary
- Where to use:
  - Teacher home screen

### API: Student Dashboard Summary

- Status: `PROPOSED`
- Purpose: Populate student dashboard
- Description: Returns today’s classes, assignments, homework, tests, announcements, progress
- Endpoint: `GET /api/dashboard/student`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Student dashboard summary
- Where to use:
  - Student home screen

## 5.3 Leads and Admissions APIs

### API: Create Inquiry Lead

- Status: `PROPOSED`
- Purpose: Capture admission lead
- Description: Creates inquiry record before admission
- Endpoint: `POST /api/leads`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "studentName": "Aarav Singh",
  "dateOfBirth": "2012-03-14",
  "gender": "Male",
  "currentStandard": "8th",
  "schoolName": "ABC School",
  "board": "CBSE",
  "subjectsInterested": ["Math", "Science"],
  "preferredBranchId": "branch-uuid",
  "parentName": "Rahul Singh",
  "relationship": "Father",
  "mobile": "9876543210",
  "alternateMobile": "9876543211",
  "email": "parent@example.com",
  "occupation": "Business",
  "address": "Sample address",
  "source": "Parent Referral",
  "subSource": "Existing Parent",
  "classInterestedIn": "8th",
  "preferredBatchTiming": "Morning",
  "followUpDate": "2026-04-28",
  "remarks": "Interested in math and science",
  "requirements": "Need weekday batch"
}
```

- Response:
  - Lead object with status `NEW`
- Where to use:
  - Add new inquiry page

### API: List Leads

- Status: `PROPOSED`
- Purpose: Show inquiry pipeline
- Description: Returns leads with filters and status counts
- Endpoint: `GET /api/leads`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `status`
  - `source`
  - `followUpBy`
  - `classInterestedIn`
  - `search`
  - `page`
  - `size`
- Response:
  - Paginated leads list plus summary counts
- Where to use:
  - Leads page
  - Admissions pipeline

### API: Get Lead Details

- Status: `PROPOSED`
- Purpose: Open inquiry side panel
- Description: Returns lead details, follow-up history, notes, and source information
- Endpoint: `GET /api/leads/{leadId}`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Lead detail object
- Where to use:
  - Lead details drawer
  - Lead summary panel

### API: Update Lead

- Status: `PROPOSED`
- Purpose: Edit lead details
- Description: Updates inquiry information and remarks
- Endpoint: `PUT /api/leads/{leadId}`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:
  - Same as create lead with editable fields
- Response:
  - Updated lead
- Where to use:
  - Edit lead form

### API: Convert Lead to Admission

- Status: `PROPOSED`
- Purpose: Convert inquiry to confirmed student
- Description: Creates admission/student from lead
- Endpoint: `POST /api/leads/{leadId}/convert-to-admission`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "branchId": "branch-uuid",
  "batchId": "batch-uuid",
  "feeStructureId": "fee-structure-uuid",
  "admissionDate": "2026-04-25"
}
```

- Response:
  - Student/admission summary
- Where to use:
  - Lead details panel
  - Admissions conversion action

### API: Mark Lead as Not Interested / Lost

- Status: `PROPOSED`
- Purpose: Close lead pipeline
- Description: Marks lead as lost or not interested with reason
- Endpoint: `POST /api/leads/{leadId}/close`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "status": "NOT_INTERESTED",
  "reason": "Joined another institute"
}
```

- Response:
  - Success envelope
- Where to use:
  - Lead detail action buttons

### API: Create Follow-Up

- Status: `PROPOSED`
- Purpose: Schedule inquiry follow-up
- Description: Adds follow-up task for lead
- Endpoint: `POST /api/leads/{leadId}/follow-ups`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "followUpDate": "2026-04-29",
  "followUpTime": "10:00:00",
  "mode": "PHONE_CALL",
  "priority": "MEDIUM",
  "assignedToUserId": "user-uuid",
  "notes": "Discuss batch timings"
}
```

- Response:
  - Follow-up object
- Where to use:
  - Follow-up form
  - Lead management

### API: List Follow-Ups

- Status: `PROPOSED`
- Purpose: Follow-up tracking page
- Description: Returns follow-up list with completed and pending counts
- Endpoint: `GET /api/follow-ups`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `status`
  - `assignedTo`
  - `dateFrom`
  - `dateTo`
  - `page`
  - `size`
- Response:
  - Paginated follow-up list
- Where to use:
  - Follow-up page

## 5.4 Student Management APIs

### API: Update Student

- Status: `PROPOSED`
- Purpose: Edit student profile
- Description: Updates student details shown in student drawer/profile
- Endpoint: `PUT /api/students/{studentId}`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:
  - Student profile fields
- Response:
  - Updated student
- Where to use:
  - Student details drawer
  - Edit student profile

### API: Student Full Profile

- Status: `PROPOSED`
- Purpose: Fetch complete student tabs
- Description: Returns overview, academic info, attendance summary, fee summary, test summary
- Endpoint: `GET /api/students/{studentId}/profile`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Rich student profile object
- Where to use:
  - Student side panel tabs
  - Student profile page

### API: Deactivate Student Admission

- Status: `PROPOSED`
- Purpose: Archive student / passed out / left institute
- Description: Adds business state beyond simple login activation
- Endpoint: `POST /api/students/{studentId}/status`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "status": "PASSED_OUT",
  "reason": "Academic year completed"
}
```

- Response:
  - Updated status object
- Where to use:
  - Student admin actions

## 5.5 Teacher Management APIs

### API: Update Teacher

- Status: `PROPOSED`
- Purpose: Edit teacher profile
- Description: Updates teacher information
- Endpoint: `PUT /api/teachers/{teacherId}`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:
  - Teacher editable fields
- Response:
  - Updated teacher
- Where to use:
  - Teacher management
  - Teacher profile drawer

### API: Teacher Full Profile

- Status: `PROPOSED`
- Purpose: Show teacher profile details
- Description: Returns class assignments, subject list, timetable, attendance, statistics
- Endpoint: `GET /api/teachers/{teacherId}/profile`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Teacher profile object
- Where to use:
  - Teacher profile page

### API: Assign Subjects to Teacher

- Status: `PROPOSED`
- Purpose: Assign academic responsibility
- Description: Maps teacher to subjects and classes
- Endpoint: `POST /api/teachers/{teacherId}/subjects`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "assignments": [
    {
      "batchId": "batch-uuid",
      "subjectId": "subject-uuid"
    }
  ]
}
```

- Response:
  - Success envelope
- Where to use:
  - Teacher setup
  - Timetable dependency

## 5.6 Attendance APIs

### API: Mark Student Attendance

- Status: `PROPOSED`
- Purpose: Save student attendance
- Description: Saves attendance for class/date with remarks
- Endpoint: `POST /api/attendance/students`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "date": "2026-04-25",
  "batchId": "batch-uuid",
  "subjectId": "subject-uuid",
  "entries": [
    {
      "studentId": "student-uuid",
      "status": "PRESENT",
      "remarks": "On time"
    }
  ]
}
```

- Response:
  - Attendance save summary
- Where to use:
  - Mark student attendance page

### API: Mark Teacher Attendance

- Status: `PROPOSED`
- Purpose: Save teacher attendance
- Description: Records teacher presence, half day, absent, and remarks
- Endpoint: `POST /api/attendance/teachers`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "date": "2026-04-25",
  "entries": [
    {
      "teacherId": "teacher-uuid",
      "status": "HALF_DAY",
      "remarks": "Medical appointment"
    }
  ]
}
```

- Response:
  - Attendance save summary
- Where to use:
  - Mark teacher attendance page

### API: Attendance Dashboard Summary

- Status: `PROPOSED`
- Purpose: KPI cards and charts
- Description: Returns student and teacher attendance summaries
- Endpoint: `GET /api/attendance/summary`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `date`
  - `batchId`
  - `branchId`
- Response:
  - Attendance aggregate object
- Where to use:
  - Attendance page
  - Dashboard widgets

### API: Student Attendance Report

- Status: `PROPOSED`
- Purpose: Student monthly/term attendance analytics
- Description: Returns attendance trend and subject-wise attendance
- Endpoint: `GET /api/students/{studentId}/attendance`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Attendance trend object
- Where to use:
  - Student portal attendance page
  - Student admin drawer

## 5.7 Timetable APIs

### API: Create Timetable

- Status: `PROPOSED`
- Purpose: Create weekly timetable
- Description: Saves timetable structure for batch/section
- Endpoint: `POST /api/timetables`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "batchId": "batch-uuid",
  "section": "A",
  "effectiveFrom": "2026-05-01",
  "viewType": "WEEKLY",
  "periods": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "08:30:00",
      "endTime": "09:15:00",
      "subjectId": "subject-uuid",
      "teacherId": "teacher-uuid",
      "roomNo": "204"
    }
  ]
}
```

- Response:
  - Timetable object
- Where to use:
  - Admin timetable creation page

### API: Get Timetable

- Status: `PROPOSED`
- Purpose: Fetch timetable by batch/teacher/student
- Description: Returns weekly timetable
- Endpoint: `GET /api/timetables`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `batchId`
  - `teacherId`
  - `studentId`
  - `weekStart`
- Response:
  - Structured timetable object
- Where to use:
  - Admin timetable page
  - Teacher my schedule
  - Student my schedule

### API: Update Timetable

- Status: `PROPOSED`
- Purpose: Edit timetable
- Description: Updates timetable entries
- Endpoint: `PUT /api/timetables/{timetableId}`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:
  - Timetable update object
- Response:
  - Updated timetable
- Where to use:
  - Edit timetable modal

## 5.8 Fees APIs

### API: Fees Dashboard Summary

- Status: `PROPOSED`
- Purpose: Fees cards and collection summary
- Description: Returns monthly totals, pending, overdue, collected
- Endpoint: `GET /api/fees/summary`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `month`
  - `batchId`
  - `feeType`
  - `paymentStatus`
- Response:
  - Fee dashboard object
- Where to use:
  - Fees page

### API: List Student Fees

- Status: `PROPOSED`
- Purpose: Show fee table
- Description: Returns fee records per student
- Endpoint: `GET /api/fees`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `month`
  - `batchId`
  - `status`
  - `studentId`
  - `page`
  - `size`
- Response:
  - Paginated fee list
- Where to use:
  - Fees list
  - Due fee list

### API: Collect Fees

- Status: `PROPOSED`
- Purpose: Record payment
- Description: Saves one or multiple fee component payments and receipt data
- Endpoint: `POST /api/fees/collections`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "studentId": "student-uuid",
  "paidDate": "2026-04-25",
  "paymentMode": "CASH",
  "referenceNo": "RCPT-1001",
  "components": [
    {
      "feeType": "TUITION",
      "amount": 4000,
      "discount": 0,
      "fine": 0
    }
  ],
  "remarks": "April fee paid"
}
```

- Response:
  - Payment summary and receipt details
- Where to use:
  - Collect fees screen

### API: Fee Receipt

- Status: `PROPOSED`
- Purpose: View/download receipt
- Description: Returns printable receipt data
- Endpoint: `GET /api/fees/collections/{receiptId}`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Receipt object
- Where to use:
  - Receipt preview
  - Print/download receipt

## 5.9 Syllabus APIs

### API: Get Syllabus by Class and Subject

- Status: `PROPOSED`
- Purpose: Show syllabus modules
- Description: Returns chapters/topics by class, subject, year
- Endpoint: `GET /api/syllabus`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `batchId`
  - `subjectId`
  - `academicYear`
- Response:
  - Syllabus content
- Where to use:
  - Admin syllabus page
  - Teacher syllabus progress

### API: Create or Update Syllabus

- Status: `PROPOSED`
- Purpose: Manage syllabus content
- Description: Create or update chapter/topic plan
- Endpoint: `POST /api/syllabus`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "batchId": "batch-uuid",
  "subjectId": "subject-uuid",
  "academicYear": "2026-27",
  "chapters": [
    {
      "title": "Linear Equations",
      "description": "Basic algebraic equations",
      "month": "APRIL",
      "topics": ["Variables", "Solving equations"]
    }
  ]
}
```

- Response:
  - Syllabus object
- Where to use:
  - Add/edit syllabus

### API: Syllabus Progress

- Status: `PROPOSED`
- Purpose: Track completion
- Description: Returns chapter-level completion and in-progress counts
- Endpoint: `GET /api/syllabus/progress`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `teacherId`
  - `batchId`
  - `subjectId`
- Response:
  - Progress object
- Where to use:
  - Teacher syllabus progress page

## 5.10 Tests and Marks APIs

### API: Create Test

- Status: `PROPOSED`
- Purpose: Create unit test/exam
- Description: Saves test definition for a batch and subject
- Endpoint: `POST /api/tests`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "batchId": "batch-uuid",
  "subjectId": "subject-uuid",
  "title": "Maths - Unit Test 2",
  "testType": "UNIT_TEST",
  "date": "2026-05-12",
  "durationMinutes": 90,
  "totalMarks": 50,
  "questions": []
}
```

- Response:
  - Test object
- Where to use:
  - Admin tests page
  - Teacher create test page

### API: List Tests

- Status: `PROPOSED`
- Purpose: Show tests table
- Description: Returns tests with status, date, score summaries
- Endpoint: `GET /api/tests`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `batchId`
  - `subjectId`
  - `testType`
  - `status`
  - `academicYear`
  - `page`
  - `size`
- Response:
  - Paginated test list
- Where to use:
  - Admin tests page
  - Teacher tests and marks page
  - Student tests list

### API: Test Analytics Summary

- Status: `PROPOSED`
- Purpose: Show test analytics charts
- Description: Returns average score, top performers, score bands, subject strengths/weaknesses
- Endpoint: `GET /api/tests/{testId}/analytics`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Analytics object
- Where to use:
  - Test analytics page

### API: Student Performance by Test

- Status: `PROPOSED`
- Purpose: Show all student results in one test
- Description: Returns ranking and status for all students
- Endpoint: `GET /api/tests/{testId}/students-performance`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Paginated ranked results
- Where to use:
  - All students performance page

### API: Individual Student Test Performance

- Status: `PROPOSED`
- Purpose: Show question analysis for one student
- Description: Returns per-question answer correctness and section summary
- Endpoint: `GET /api/tests/{testId}/students/{studentId}/performance`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Detailed student performance object
- Where to use:
  - Student performance page
  - Admin test review

### API: Student Test History

- Status: `PROPOSED`
- Purpose: Student portal test history
- Description: Returns test list, scores, status, subject-wise performance
- Endpoint: `GET /api/student/tests`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Student test history
- Where to use:
  - Student tests and marks page

### API: Test Detail for Student

- Status: `PROPOSED`
- Purpose: View test detail and analysis in student portal
- Description: Returns score, percentile, section analysis, downloadable resources
- Endpoint: `GET /api/student/tests/{testId}`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Student test detail object
- Where to use:
  - Student test detail page

### API: Start Test / Submit Test

- Status: `PROPOSED`
- Purpose: Attempt online test
- Description: Supports student exam attempt flow
- Endpoint: `POST /api/student/tests/{testId}/attempts`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "answers": [
    {
      "questionId": "question-uuid",
      "selectedOption": "B"
    }
  ]
}
```

- Response:
  - Attempt summary
- Where to use:
  - Student attempt test page

## 5.11 Homework and Assignment APIs

### API: Create Homework

- Status: `PROPOSED`
- Purpose: Assign homework from admin or teacher side
- Description: Creates homework with instructions, due date, attachments, and students/batch assignment
- Endpoint: `POST /api/homeworks`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "batchId": "batch-uuid",
  "subjectId": "subject-uuid",
  "title": "Linear Equation Worksheet",
  "description": "Solve all questions",
  "assignedDate": "2026-05-10",
  "dueDate": "2026-05-20",
  "allowLateSubmission": false,
  "allowResubmission": false,
  "students": ["student-uuid-1", "student-uuid-2"]
}
```

- Response:
  - Homework object
- Where to use:
  - Admin add homework page
  - Teacher create homework page

### API: List Homework / Assignments

- Status: `PROPOSED`
- Purpose: Show homework and assignments lists
- Description: Returns list by batch, subject, teacher, status, date range
- Endpoint: `GET /api/homeworks`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `batchId`
  - `subjectId`
  - `assignedBy`
  - `status`
  - `fromDate`
  - `toDate`
  - `page`
  - `size`
- Response:
  - Paginated homework list
- Where to use:
  - Admin homework page
  - Teacher homework page
  - Student homework page

### API: Homework Submission Report

- Status: `PROPOSED`
- Purpose: Show submitted, pending, overdue analytics
- Description: Returns submission summary and student-wise records
- Endpoint: `GET /api/homeworks/{homeworkId}/submission-report`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Submission report object
- Where to use:
  - Submission report page

### API: Student Homework Detail

- Status: `PROPOSED`
- Purpose: Show homework detail in student portal
- Description: Returns homework instructions, tasks, attachments, submission status
- Endpoint: `GET /api/student/homeworks/{homeworkId}`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Homework detail object
- Where to use:
  - Student homework detail page

### API: Upload Homework Submission

- Status: `PROPOSED`
- Purpose: Submit homework from student portal
- Description: Uploads answer files and marks submission time
- Endpoint: `POST /api/student/homeworks/{homeworkId}/submissions`
- Headers:
  - `Authorization: Bearer <token>`
  - `Content-Type: multipart/form-data`
- Payload:
  - file attachments
- Response:
  - Submission summary
- Where to use:
  - Student homework submission page

## 5.12 Reports APIs

### API: Reports Summary

- Status: `PROPOSED`
- Purpose: Fill reports dashboard cards and charts
- Description: Returns aggregate counts for students, attendance, tests, fees, homework
- Endpoint: `GET /api/reports/summary`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `reportType`
  - `batchId`
  - `dateFrom`
  - `dateTo`
- Response:
  - Report dashboard object
- Where to use:
  - Reports page

### API: Download Reports

- Status: `PROPOSED`
- Purpose: Export reporting data
- Description: Returns file or signed download URL
- Endpoint: `GET /api/reports/export`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `reportType`
  - `batchId`
  - `dateFrom`
  - `dateTo`
  - `format=pdf|xlsx|csv`
- Response:
  - File response or URL
- Where to use:
  - Export buttons

## 5.13 Stationery APIs

### API: Stationery Summary

- Status: `PROPOSED`
- Purpose: Inventory dashboard cards
- Description: Returns stock counts, low stock, out-of-stock, total value
- Endpoint: `GET /api/stationery/summary`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Inventory summary object
- Where to use:
  - Stationery page

### API: List Stationery Items

- Status: `PROPOSED`
- Purpose: Inventory table
- Description: Returns stationery items with stock and value
- Endpoint: `GET /api/stationery/items`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `category`
  - `itemStatus`
  - `stockStatus`
  - `search`
  - `page`
  - `size`
- Response:
  - Paginated inventory items
- Where to use:
  - Stationery list

### API: Stock In / Stock Out

- Status: `PROPOSED`
- Purpose: Track inventory movement
- Description: Records quantity increase or decrease
- Endpoint: `POST /api/stationery/transactions`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "itemId": "item-uuid",
  "type": "STOCK_IN",
  "quantity": 100,
  "unitPrice": 10,
  "remarks": "New purchase"
}
```

- Response:
  - Transaction summary
- Where to use:
  - Stock in button
  - Stock out button

## 5.14 Daily Timesheet APIs

### API: Timesheet Summary

- Status: `PROPOSED`
- Purpose: Daily timesheet cards and history
- Description: Returns total weekly hours, today hours, monthly hours, overtime, history
- Endpoint: `GET /api/timesheets/me`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `month`
- Response:
  - Timesheet dashboard object
- Where to use:
  - Daily timesheet page

### API: Create Timesheet Entry

- Status: `PROPOSED`
- Purpose: Save work log
- Description: Records check-in, check-out, work details, total hours
- Endpoint: `POST /api/timesheets`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "date": "2026-05-14",
  "checkInTime": "09:05:00",
  "checkOutTime": "18:10:00",
  "workDescription": "Prepared reports and verified student payments"
}
```

- Response:
  - Timesheet entry
- Where to use:
  - Daily timesheet form

## 5.15 Notification Management APIs

### API: Send Notification

- Status: `PROPOSED`
- Purpose: Compose and send notification
- Description: Creates notification job for audience
- Endpoint: `POST /api/notifications/send`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "title": "Fee Reminder",
  "message": "Please clear pending fee.",
  "priority": "HIGH",
  "sendImmediately": true,
  "templateId": null,
  "audience": {
    "roles": ["PARENT"],
    "batchIds": ["batch-uuid"]
  },
  "channels": ["IN_APP", "EMAIL"]
}
```

- Response:
  - Notification campaign summary
- Where to use:
  - Send new notification page

### API: Notification History

- Status: `PROPOSED`
- Purpose: Show sent, scheduled, drafts
- Description: Returns notification history by type and status
- Endpoint: `GET /api/notifications/history`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `status`
  - `audience`
  - `page`
  - `size`
- Response:
  - Paginated campaign list
- Where to use:
  - Notifications admin page

### API: Notification Templates

- Status: `PROPOSED`
- Purpose: Reuse templates
- Description: CRUD for notification templates
- Endpoint: `GET /api/notifications/templates`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Template list
- Where to use:
  - Notification template management

## 5.16 Teacher Workflow APIs

### API: Teacher Schedule

- Status: `PROPOSED`
- Purpose: Show teacher daily/weekly schedule
- Description: Returns classes by day or week
- Endpoint: `GET /api/teacher/my-schedule`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `view=day|week|month`
  - `fromDate`
  - `toDate`
- Response:
  - Schedule object
- Where to use:
  - Teacher my schedule screen

### API: Start Class

- Status: `PROPOSED`
- Purpose: Begin live class session
- Description: Records class in state and planned coverage
- Endpoint: `POST /api/teacher/classes/start`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "batchId": "batch-uuid",
  "subjectId": "subject-uuid",
  "chapterId": "chapter-uuid",
  "subtopics": ["Linear equations", "Practice problems"]
}
```

- Response:
  - Class session object
- Where to use:
  - Start class (IN) page

### API: End Class

- Status: `PROPOSED`
- Purpose: Complete class session
- Description: Records actual topics covered, homework, remarks, duration
- Endpoint: `POST /api/teacher/classes/{sessionId}/end`
- Headers:
  - `Authorization: Bearer <token>`
- Payload:

```json
{
  "coveredTopics": ["Linear equations", "Practice problems"],
  "remarks": "Students understood basics well",
  "homeworkId": "homework-uuid"
}
```

- Response:
  - Session summary
- Where to use:
  - End class modal

### API: Weekly Plan

- Status: `PROPOSED`
- Purpose: Teacher planning board
- Description: Returns planned classes, assignments, goals, notes
- Endpoint: `GET /api/teacher/weekly-plan`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `weekStart`
- Response:
  - Weekly plan object
- Where to use:
  - Weekly plan screen

### API: Teacher Students List

- Status: `PROPOSED`
- Purpose: Show students under teacher classes
- Description: Returns students taught by current teacher with attendance/performance indicators
- Endpoint: `GET /api/teacher/students`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `batchId`
  - `section`
  - `status`
  - `search`
- Response:
  - Paginated student list
- Where to use:
  - Teacher students page

## 5.17 Student Portal APIs

### API: Student My Schedule

- Status: `PROPOSED`
- Purpose: Student schedule view
- Description: Returns daily or weekly timetable for current student
- Endpoint: `GET /api/student/my-schedule`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Schedule object
- Where to use:
  - Student my schedule page

### API: Student Assignments List

- Status: `PROPOSED`
- Purpose: Show assignments to student
- Description: Returns all, pending, submitted, overdue assignments
- Endpoint: `GET /api/student/assignments`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `status`
  - `search`
  - `page`
  - `size`
- Response:
  - Paginated assignment list
- Where to use:
  - Student assignments page

### API: Student Assignment Detail

- Status: `PROPOSED`
- Purpose: Show assignment detail and submission
- Description: Returns assignment instructions, attachments, review status
- Endpoint: `GET /api/student/assignments/{assignmentId}`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Assignment detail object
- Where to use:
  - Assignment detail page

### API: Student Attendance Summary

- Status: `PROPOSED`
- Purpose: Student self-attendance page
- Description: Returns overview, trend, records, subject-wise attendance
- Endpoint: `GET /api/student/attendance`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Attendance dashboard object
- Where to use:
  - Student attendance page

### API: Student Announcements

- Status: `PROPOSED`
- Purpose: Announcement center
- Description: Returns school announcements by category
- Endpoint: `GET /api/student/announcements`
- Headers:
  - `Authorization: Bearer <token>`
- Query Parameters:
  - `category`
  - `search`
- Response:
  - Paginated announcement list
- Where to use:
  - Student announcements page

### API: Student Profile

- Status: `PROPOSED`
- Purpose: Personal profile screen
- Description: Returns personal, academic, guardian, achievement, and account details
- Endpoint: `GET /api/student/profile`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Profile object
- Where to use:
  - Student profile page

## 5.18 Parent Portal APIs

These are not clearly shown in the screenshots but are implied by the backend comments and student admission flow.

### API: Parent Dashboard

- Status: `PROPOSED`
- Purpose: Parent overview
- Description: Returns child attendance, fees, homework, tests, announcements
- Endpoint: `GET /api/parent/dashboard`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Parent dashboard summary
- Where to use:
  - Parent app/web portal

### API: Parent Child Profile

- Status: `PROPOSED`
- Purpose: View child details
- Description: Returns child academic and attendance summary
- Endpoint: `GET /api/parent/children/{studentId}`
- Headers:
  - `Authorization: Bearer <token>`
- Response:
  - Student summary object
- Where to use:
  - Parent child overview

## 6. API Build Priority

Recommended implementation order:

1. `Auth alignment`
   - Support login by `identifier` instead of email-only
   - Make student and parent login compatible with Figma
2. `Master data`
   - Branches
   - Batches
   - Subjects
   - Fee structures
3. `Leads and admissions`
   - Leads
   - Follow-ups
   - Convert to admission
4. `Student and teacher management enhancement`
   - Edit profile
   - Full profile APIs
   - Subject assignment
5. `Attendance`
6. `Timetable`
7. `Homework and tests`
8. `Fees`
9. `Reports`
10. `Teacher and student portal APIs`
11. `Notifications composer and templates`
12. `Stationery and timesheet`

## 7. Important Backend Gaps to Fix Before Frontend Merge

### 7.1 Student Login Gap

Current issue:
- Student creation returns `loginId`
- Login still accepts only `email`

Impact:
- Student portal and parent portal login will not work as designed

Required fix:
- Change login request to accept `identifier`
- Support:
  - email for staff/admin
  - loginId for student/parent

### 7.2 Photo Upload Gap

Current issue:
- Student finalisation requires photo
- Photo upload endpoint is commented out

Impact:
- Admission flow remains incomplete

Required fix:
- Restore and implement `POST /api/users/students/{id}/photo`

### 7.3 Missing Database Migrations

Current issue:
- Flyway is enabled
- Migration folder is empty
- JPA `ddl-auto: update` is currently carrying schema changes

Impact:
- Unsafe for production
- Difficult for team synchronization

Required fix:
- Add proper Flyway migration scripts
- Seed:
  - roles
  - permissions
  - initial master data where required

### 7.4 Role and Parent Model Gap

Current issue:
- `PARENT` role exists in enum
- No parent entity or parent-facing APIs exist

Impact:
- Parent portal cannot be implemented cleanly

Required fix:
- Add parent account strategy
- Either:
  - separate parent user entity and auth
  - or explicit linked guardian login model

## 8. Suggested Frontend Usage Strategy

Frontend team should:

1. Integrate immediately with created APIs for:
   - tenant registration
   - login
   - auth refresh
   - current user
   - create/list/get students
   - create/list/get teachers
   - notifications

2. Keep these screens on mock or adapter mode until backend is ready:
   - dashboard
   - attendance
   - fees
   - timetable
   - homework
   - tests
   - reports
   - stationery
   - daily timesheet
   - teacher and student detailed portal flows

3. Use role-based routing based on `/api/auth/me`

4. Keep one shared API client that unwraps the standard `ApiResponse`

## 9. Final Summary

Current backend coverage is strongest in:
- tenant setup
- authentication
- basic student management
- basic teacher management
- notification inbox

Major Figma-aligned backend work still required:
- leads and admissions
- attendance
- timetable
- fees
- homework and assignments
- tests and marks
- reports
- stationery
- daily timesheet
- teacher operational workflows
- student portal
- parent portal

This document should be treated as the initial API contract and implementation roadmap for turning the existing backend into the full application shown in the Figma design.
