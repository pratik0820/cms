# Class Management System API Documentation

## 1. Purpose

This document defines the API contract for the application up to the `Super Admin` phase only.

It is written to support:
- backend implementation
- frontend integration
- QA test case design
- future role expansion to `ADMIN`, `TEACHER`, `PARENT`, and `STUDENT`

This document intentionally excludes deeper role-specific APIs for admin, teacher, parent, and student portals. It stays limited to what is needed for the super admin journey shown in the designs.

## 2. Scope

This API document is based on:
- the current backend code in this repository
- the non-tenant architecture now adopted in the project
- the super admin designs shared in the screenshots
- the current schema baseline in `src/main/resources/db/migration/V1__super_admin_foundation.sql`
- the admin management migration in `src/main/resources/db/migration/V2__admin_management.sql`
- the teacher management migration in `src/main/resources/db/migration/V3__teacher_management.sql`
- the student management migration in `src/main/resources/db/migration/V4__student_management.sql`
- the analytics support migration in `src/main/resources/db/migration/V5__analytics_support.sql`
- the reports management migration in `src/main/resources/db/migration/V6__reports_management.sql`
- the courses, batches, and enrolments migration in `src/main/resources/db/migration/V7__courses_batches_enrolments.sql`
- the lead management migration in `src/main/resources/db/migration/V8__lead_management.sql`
- the teacher/student catalogue alignment migration in `src/main/resources/db/migration/V9__teacher_student_catalog_alignment.sql`

Status labels used in this document:
- `IMPLEMENTED`: endpoint already exists in backend
- `PLANNED`: endpoint is part of the super admin phase contract but is not implemented yet

## 3. Application Flow Overview

### 3.1 High-Level Product Flow

The application flow up to the super admin phase is:

1. System starts with no tenant concept and no institute owner flow.
2. First-time setup creates exactly one independent `SUPER_ADMIN`.
3. Super admin logs in using email or future-compatible identifier.
4. Frontend loads current user context and access role.
5. Super admin lands on the dashboard.
6. Super admin navigates through management modules:
   - dashboard
   - branch overview
   - admin management
   - teacher management
   - student management
   - leads and admissions
   - analytics
   - reports
   - feedback
   - attendance overview
   - test and performance
   - syllabus completion
   - stationery overview
   - timesheet
   - teacher payout tracking
   - system settings
7. All later roles should plug into the same auth foundation using role-based authorization and optional branch scope.

### 3.2 Authentication and Authorization Model

The current backend is designed around:
- one `users` table for login accounts
- one `roles` table for role assignment
- JWT access token for short-lived authorization
- refresh token table for device/session rotation
- optional `branch_id` on user for branch-scoped roles later

Current roles:
- `SUPER_ADMIN`
- `ADMIN`
- `TEACHER`
- `STUDENT`
- `PARENT`

Super admin rules:
- only one bootstrap super admin is allowed in a fresh system
- super admin is not tenant-bound
- dashboard API is protected by `hasRole('SUPER_ADMIN')`

## 4. Base URL and Standards

### 4.1 Base URL

From `application.yaml`:

```text
http://localhost:8095/cms
```

Examples:

```text
POST http://localhost:8095/cms/api/auth/login
GET  http://localhost:8095/cms/api/super-admin/dashboard
```

### 4.2 Common Headers

Public endpoints:

```http
Content-Type: application/json
```

Authenticated endpoints:

```http
Content-Type: application/json
Authorization: Bearer <access_token>
```

### 4.3 Standard Response Envelope

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {},
  "errorCode": null,
  "timestamp": "2026-05-08T18:30:00"
}
```

### 4.4 Standard Error Envelope

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Please provide a valid email address"
  },
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-05-08T18:30:00"
}
```

### 4.5 Common Error Codes

- `VALIDATION_ERROR`
- `AUTH_IDENTIFIER_REQUIRED`
- `AUTH_INVALID_CREDENTIALS`
- `AUTH_ACCOUNT_DISABLED`
- `AUTH_ACCOUNT_LOCKED`
- `AUTH_INVALID_REFRESH_TOKEN`
- `AUTH_REFRESH_TOKEN_EXPIRED`
- `AUTH_TOKEN_REUSE`
- `FORBIDDEN`
- `INTERNAL_ERROR`

## 5. Implementation Order

Recommended backend delivery order for the super admin phase:

1. database migration and role seed
2. bootstrap super admin API
3. login, refresh, logout, me, change password
4. dashboard summary API
5. branch overview APIs
6. admin management APIs
7. teacher management APIs
8. student management APIs
9. leads and admissions APIs
10. analytics APIs
11. reports APIs
12. feedback APIs
13. attendance overview APIs
14. test and performance APIs
15. syllabus APIs
16. stationery APIs
17. timesheet APIs
18. teacher payout APIs
19. system settings APIs

## 6. Current Backend Surface

The current codebase has only these super-admin-phase APIs implemented:

- `POST /api/auth/bootstrap/super-admin`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `PUT /api/auth/change-password`
- `GET /api/auth/me`
- `GET /api/super-admin/dashboard`
- `GET /api/super-admin/branches/options`
- `GET /api/super-admin/branches`
- `GET /api/super-admin/branches/{branchId}`
- `POST /api/super-admin/branches`
- `PUT /api/super-admin/branches/{branchId}`
- `PATCH /api/super-admin/branches/{branchId}/status`
- `DELETE /api/super-admin/branches/{branchId}`
- `GET /api/super-admin/admins`
- `GET /api/super-admin/admins/{adminId}`
- `POST /api/super-admin/admins`
- `PUT /api/super-admin/admins/{adminId}`
- `PATCH /api/super-admin/admins/{adminId}/status`
- `DELETE /api/super-admin/admins/{adminId}`
- `GET /api/super-admin/teachers`
- `GET /api/super-admin/teachers/{teacherId}`
- `POST /api/super-admin/teachers`
- `PUT /api/super-admin/teachers/{teacherId}`
- `PATCH /api/super-admin/teachers/{teacherId}/status`
- `DELETE /api/super-admin/teachers/{teacherId}`
- `GET /api/super-admin/students`
- `GET /api/super-admin/students/{studentId}`
- `POST /api/super-admin/students`
- `PUT /api/super-admin/students/{studentId}`
- `PATCH /api/super-admin/students/{studentId}/status`
- `DELETE /api/super-admin/students/{studentId}`
- `GET /api/super-admin/analytics`
- `GET /api/super-admin/reports/summary`
- `GET /api/super-admin/reports/categories`
- `GET /api/super-admin/reports`
- `POST /api/super-admin/reports/export`
- `GET /api/super-admin/reports/{reportId}/download`
- `DELETE /api/super-admin/reports/{reportId}`
- `GET /api/courses/subjects`
- `GET /api/courses`
- `GET /api/courses/{courseId}`
- `POST /api/batches`
- `GET /api/batches`
- `GET /api/batches/by-branch/{branchId}`
- `GET /api/batches/{batchId}`
- `PUT /api/batches/{batchId}`
- `DELETE /api/batches/{batchId}`
- `POST /api/enrolments`
- `GET /api/enrolments/{enrolmentId}`
- `GET /api/enrolments/student/{studentId}`
- `GET /api/enrolments/batch/{batchId}`
- `GET /api/enrolments/branch/{branchId}`
- `PUT /api/enrolments/{enrolmentId}`
- `DELETE /api/enrolments/{enrolmentId}`
- `GET /api/super-admin/leads`
- `GET /api/super-admin/leads/{leadId}`
- `POST /api/super-admin/leads`
- `PUT /api/super-admin/leads/{leadId}`
- `PATCH /api/super-admin/leads/{leadId}/status`
- `POST /api/super-admin/leads/{leadId}/convert-to-admission`
- `DELETE /api/super-admin/leads/{leadId}`

Everything else in this document is the approved contract for the super admin phase and should be implemented next.

## 7. Authentication APIs

## 7.1 Bootstrap Super Admin

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/auth/bootstrap/super-admin`
- Auth: Public
- Purpose: Creates the first and only initial super admin account when the system is fresh.

### Request Payload

```json
{
  "fullName": "Super Admin",
  "email": "superadmin@classmanager.com",
  "phone": "9876543210",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `fullName` | string | Yes | Display name used across profile, header, audit context, and future ownership records. |
| `email` | string | Yes | Primary login identity for the bootstrap account. Must be unique across all users. |
| `phone` | string | No | Contact number for future profile, recovery, and notification use. |
| `password` | string | Yes | Initial credential for super admin login. Stored only as password hash. |
| `confirmPassword` | string | Yes | Prevents accidental password mismatch during first system setup. |

### Success Response

```json
{
  "success": true,
  "message": "Super admin created successfully",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "opaque-refresh-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "user": {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "email": "superadmin@classmanager.com",
      "loginId": null,
      "fullName": "Super Admin",
      "roles": ["SUPER_ADMIN"],
      "branchId": null,
      "branchName": null
    }
  },
  "timestamp": "2026-05-08T18:30:00"
}
```

### Backend Implementation Steps

1. Validate request body and password confirmation.
2. Check whether any user already holds role `SUPER_ADMIN`.
3. Normalize email to lowercase.
4. Check email uniqueness in `users`.
5. Load role `SUPER_ADMIN` from `roles`.
6. Create `users` record with active status.
7. Hash password using BCrypt.
8. Save `user_roles` mapping.
9. Mark login metadata as successful first login.
10. Generate JWT access token.
11. Generate and persist refresh token.
12. Return `AuthResponse` inside `ApiResponse`.

## 7.2 Login

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/auth/login`
- Auth: Public
- Purpose: Authenticates a user and starts a session.

### Request Payload

```json
{
  "identifier": "superadmin@classmanager.com",
  "password": "Password@123",
  "fcmToken": "optional-device-token"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `identifier` | string | Recommended | Main login field. For super admin and future staff roles this will be email. For students or parents it can later be login ID. |
| `email` | string | Backward compatible | Legacy support field still accepted by backend. Frontend should now use `identifier`. |
| `password` | string | Yes | Secret credential used by `AuthenticationManager`. |
| `fcmToken` | string | No | Device token for push notifications and device-linked session intelligence later. |

### Success Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "opaque-refresh-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "user": {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "email": "superadmin@classmanager.com",
      "loginId": null,
      "fullName": "Super Admin",
      "roles": ["SUPER_ADMIN"],
      "branchId": null,
      "branchName": null
    }
  }
}
```

### Backend Implementation Steps

1. Accept `identifier`; if empty, fallback to `email`.
2. Normalize identifier to lowercase.
3. Find user by `email` or `login_id`.
4. Reject disabled account.
5. Reject temporarily locked account.
6. Authenticate through Spring Security `AuthenticationManager`.
7. On failure, increment failed count and set lock if threshold reached.
8. On success, reset failed count and lock fields.
9. Save optional `fcmToken`.
10. Generate new access token and refresh token.
11. Return user context needed by frontend shell.

## 7.3 Refresh Token

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/auth/refresh`
- Auth: Public
- Purpose: Issues a fresh access token and rotates refresh token.

### Request Payload

```json
{
  "refreshToken": "existing-refresh-token"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `refreshToken` | string | Yes | Long-lived opaque token used to continue the session without forcing user login again. |

### Success Response

Same response shape as login.

### Backend Implementation Steps

1. Hash the incoming refresh token.
2. Load the token from `refresh_tokens`.
3. Reject missing, expired, revoked, or reused token.
4. Revoke all sessions if token reuse is detected.
5. Load related user and verify account is active.
6. Mark current token as used.
7. Create a new access token.
8. Persist a new refresh token.
9. Return rotated auth payload.

## 7.4 Logout

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/auth/logout`
- Auth: Authenticated
- Purpose: Ends session on the current device.

### Request Payload

```json
{
  "refreshToken": "existing-refresh-token"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `refreshToken` | string | Yes | Identifies the specific device session that should be revoked. |

### Success Response

```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

### Backend Implementation Steps

1. Require valid access token.
2. Hash supplied refresh token.
3. Find matching row in `refresh_tokens`.
4. Mark token revoked with reason `USER_LOGOUT`.
5. Return success envelope.

## 7.5 Logout All Devices

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/auth/logout-all`
- Auth: Authenticated
- Purpose: Revokes every active session for the logged-in user.

### Request Payload

No payload.

### Success Response

```json
{
  "success": true,
  "message": "Logged out from all devices",
  "data": null
}
```

### Backend Implementation Steps

1. Resolve current user from JWT.
2. Revoke all refresh tokens for `user_id`.
3. Return success envelope.

## 7.6 Change Password

- Status: `IMPLEMENTED`
- Endpoint: `PUT /api/auth/change-password`
- Auth: Authenticated
- Purpose: Changes password for the current user.

### Request Payload

```json
{
  "currentPassword": "OldPassword@123",
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `currentPassword` | string | Yes | Prevents unauthorized password change from an already open session. |
| `newPassword` | string | Yes | New credential to be stored as secure hash. |
| `confirmPassword` | string | Yes | Prevents accidental mismatch before password replacement. |

### Success Response

```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

### Backend Implementation Steps

1. Resolve current user.
2. Load user from database.
3. Validate current password against stored hash.
4. Validate new password confirmation.
5. Hash and replace password.
6. Revoke all refresh tokens for security.
7. Return success envelope.

## 7.7 Current User

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/auth/me`
- Auth: Authenticated
- Purpose: Returns user context required to initialize frontend shell.

### Request Payload

No payload.

### Success Response

```json
{
  "success": true,
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "superadmin@classmanager.com",
    "loginId": null,
    "fullName": "Super Admin",
    "roles": ["SUPER_ADMIN"],
    "branchId": null,
    "branchName": null
  }
}
```

### Backend Implementation Steps

1. Resolve current user from JWT.
2. Map `User` entity to `AuthResponse.UserInfo`.
3. Return shell-safe context only.

## 8. Super Admin Dashboard APIs

## 8.1 Dashboard Summary

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/dashboard`
- Auth: `SUPER_ADMIN`
- Purpose: Fills the super admin dashboard shown in the design.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `branchId` | UUID | No | Filters dashboard metrics for a single branch. If missing, return all-branch aggregate. |
| `fromDate` | date | No | Lower date bound for fee, attendance, lead, activity, class, test, and feedback records. |
| `toDate` | date | No | Upper date bound for dashboard data. |

### Default Behavior

- if `toDate` is missing, backend uses current date
- if `fromDate` is missing, backend uses first day of `toDate` month
- if `branchId` is missing, backend returns `All Branches`

### Success Response

```json
{
  "success": true,
  "data": {
    "fromDate": "2026-05-01",
    "toDate": "2026-05-08",
    "branchId": null,
    "branchName": "All Branches",
    "branches": [
      {
        "id": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
        "name": "Main Branch"
      }
    ],
    "overview": {
      "totalStudents": {
        "total": 2453,
        "changeThisMonth": 120
      },
      "totalTeachers": {
        "total": 87,
        "changeThisMonth": 5
      },
      "totalAdmins": {
        "total": 12,
        "changeThisMonth": 1
      },
      "totalFeesCollected": {
        "total": 4875000,
        "changeThisMonth": 766000
      },
      "pendingFees": {
        "total": 875600,
        "changeThisMonth": 875600
      }
    },
    "studentGrowth": [
      {
        "month": "Jun",
        "totalStudents": 1780
      }
    ],
    "feeCollection": [
      {
        "month": "May",
        "feesCollected": 766000,
        "pendingFees": 875600
      }
    ],
    "attendanceOverview": {
      "present": 208505,
      "leave": 19632,
      "absent": 17186,
      "averageAttendancePercentage": 85.00
    },
    "leadConversionOverview": {
      "stages": [
        {
          "label": "Total Leads",
          "count": 1250,
          "percentage": 100.00
        },
        {
          "label": "Interested",
          "count": 650,
          "percentage": 52.00
        },
        {
          "label": "Converted",
          "count": 320,
          "percentage": 25.60
        },
        {
          "label": "Admission",
          "count": 285,
          "percentage": 22.80
        }
      ]
    },
    "recentActivities": [
      {
        "type": "NEW_ADMISSION",
        "title": "New student admission in Main Branch",
        "description": "Rahul Sharma (10th CBSE)",
        "branchName": "Main Branch",
        "createdAt": "2026-05-08T10:20:00"
      }
    ],
    "footerMetrics": {
      "totalClassesToday": 96,
      "teachersIn": 42,
      "studentsPresent": 1985,
      "testsConducted": 8,
      "feedbacksReceived": 36
    }
  }
}
```

### Backend Implementation Steps

1. Validate optional `branchId`.
2. Resolve date defaults.
3. Load active branches for filter dropdown.
4. Query `students`, `teachers`, and `users` counts.
5. Query `operational_records` by module for:
   - `FEE`
   - `ATTENDANCE`
   - `LEAD`
   - `ACTIVITY`
   - `CLASS_SESSION`
   - `TEST`
   - `FEEDBACK`
6. Build overview metric cards.
7. Build 12-month student growth series.
8. Build fee collection monthly series.
9. Build attendance donut summary.
10. Build lead conversion funnel.
11. Build recent activities list.
12. Build footer metrics.
13. Return one consolidated response to reduce frontend round trips.

## 9. Branch Overview APIs

These APIs are required because the super admin UI has branch switching and branch-level visibility.

## 9.1 Branch Dropdown List

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/branches/options`
- Auth: `SUPER_ADMIN`
- Purpose: Returns lightweight branch list for header filters and forms.

### Request Payload

No payload.

### Success Response

```json
{
  "success": true,
  "data": [
    {
      "id": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
      "name": "Main Branch"
    }
  ]
}
```

### Backend Implementation Steps

1. Query active, non-deleted branches.
2. Sort by name.
3. Return only `id` and `name`.

## 9.2 Branch Overview Summary

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/branches`
- Auth: `SUPER_ADMIN`
- Purpose: Fills the branch overview screen with cards, table, and status data.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `search` | string | No | Filters branch table by branch name, city, phone, or email. |
| `isActive` | boolean | No | Filters active or inactive branches. |
| `page` | integer | No | Current page for branch table. |
| `size` | integer | No | Page size for branch table. |

### Success Response

```json
{
  "success": true,
  "data": {
    "summary": {
      "totalBranches": 2,
      "activeBranches": 2,
      "inactiveBranches": 0
    },
    "content": [
      {
        "id": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
        "name": "Main Branch",
        "address": "Baner, Pune",
        "city": "Pune",
        "phone": "9876543210",
        "email": "main@school.com",
        "isActive": true,
        "totalStudents": 1200,
        "totalTeachers": 45,
        "totalAdmins": 5,
        "createdAt": "2026-05-09T10:00:00",
        "updatedAt": "2026-05-09T10:00:00"
      }
    ],
    "page": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 2,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  }
}
```

### Backend Implementation Steps

1. Accept filter and pagination inputs.
2. Query `branches`.
3. Join or aggregate counts from `students`, `teachers`, and `users`.
4. Build summary cards.
5. Build paginated branch table.
6. Return one page response.

## 9.3 Branch Detail

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/branches/{branchId}`
- Auth: `SUPER_ADMIN`
- Purpose: Returns a single branch with its counts and profile data.

## 9.4 Create Branch

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/super-admin/branches`
- Auth: `SUPER_ADMIN`
- Purpose: Creates a new branch.

### Request Payload

```json
{
  "name": "Baner Branch",
  "address": "Baner Road, Pune",
  "city": "Pune",
  "phone": "9876543210",
  "email": "baner@school.com"
}
```

## 9.5 Update Branch

- Status: `IMPLEMENTED`
- Endpoint: `PUT /api/super-admin/branches/{branchId}`
- Auth: `SUPER_ADMIN`
- Purpose: Updates branch profile data.

### Request Payload

Same shape as create branch.

## 9.6 Change Branch Status

- Status: `IMPLEMENTED`
- Endpoint: `PATCH /api/super-admin/branches/{branchId}/status`
- Auth: `SUPER_ADMIN`
- Purpose: Activates or deactivates a branch.

### Request Payload

```json
{
  "isActive": false,
  "reason": "Temporarily closed"
}
```

## 9.7 Delete Branch

- Status: `IMPLEMENTED`
- Endpoint: `DELETE /api/super-admin/branches/{branchId}`
- Auth: `SUPER_ADMIN`
- Purpose: Soft deletes a branch when it has no linked admins, teachers, or students.

## 10. Admin Management APIs

## 10.1 Admin List

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/admins`
- Auth: `SUPER_ADMIN`
- Purpose: Fills the admin management screen table and top cards.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `branchId` | UUID | No | Filters admins for a selected branch. |
| `isActive` | boolean | No | Filters active or inactive admins. |
| `search` | string | No | Searches by full name, email, or phone. |
| `page` | integer | No | Table page index. |
| `size` | integer | No | Table page size. |

### Success Response

```json
{
  "success": true,
  "data": {
    "summary": {
      "totalAdmins": 12,
      "activeAdmins": 10,
      "inactiveAdmins": 2,
      "allBranchAdmins": 7
    },
    "content": [
      {
        "id": "f5f4862c-7f49-49fe-ae54-078fd0bbf7e8",
        "userId": "2c421b28-4f72-41a5-8d87-c6f1d215db76",
        "fullName": "Rahul Sharma",
        "email": "rahul.sharma@institute.com",
        "phone": "9876543210",
        "loginId": "ADM00001",
        "dateOfBirth": "1991-04-10",
        "gender": "MALE",
        "profilePhotoUrl": "https://cdn.example.com/admins/rahul.jpg",
        "role": "Admin",
        "joiningDate": "2023-01-10",
        "accessLevel": "FULL",
        "address": "Pune",
        "allBranchesAccess": false,
        "branchId": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
        "branchName": "Main Branch",
        "isActive": true,
        "lastLoginAt": "2026-05-08T10:30:00",
        "createdAt": "2026-05-08T09:00:00",
        "updatedAt": "2026-05-08T09:00:00"
      }
    ],
    "page": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 12,
      "totalPages": 2,
      "first": true,
      "last": false
    }
  }
}
```

### Backend Implementation Steps

1. Require super admin access.
2. Accept optional `search`, `isActive`, `branchId`, `page`, and `size`.
3. Query `admin_profiles` joined with `users`.
4. Apply branch rule:
   - include all-branches admins
   - include branch-mapped admins when `branchId` matches
5. Build summary cards from the filtered scope.
6. Return admin list plus pagination metadata.

## 10.2 Create Admin

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/super-admin/admins`
- Auth: `SUPER_ADMIN`
- Purpose: Creates an admin user from the `Add New Admin` screen.

### Request Payload

```json
{
  "fullName": "Branch Admin",
  "email": "branch.admin@school.com",
  "phone": "9876543210",
  "dateOfBirth": "1992-02-10",
  "gender": "MALE",
  "profilePhotoUrl": "https://cdn.example.com/admin.jpg",
  "loginId": "ADM00013",
  "password": "Password@123",
  "confirmPassword": "Password@123",
  "role": "ADMIN",
  "joiningDate": "2026-05-08",
  "accessLevel": "FULL",
  "branchId": null,
  "address": "Baner, Pune",
  "allBranchesAccess": true
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `fullName` | string | Yes | Display name shown in list, header, and audit logs. |
| `email` | string | Yes | Primary admin login email and communication address. |
| `phone` | string | Yes | Contact number shown in table and for future recovery. |
| `dateOfBirth` | date | No | Personal profile field shown in create form. |
| `gender` | enum | No | Profile metadata shown in create form. |
| `profilePhotoUrl` | string | No | Stores uploaded image reference after media upload flow is added. |
| `loginId` | string | Yes | Business-friendly admin identifier from UI design. |
| `password` | string | Yes | Initial admin password. |
| `confirmPassword` | string | Yes | Prevents password mismatch during creation. |
| `role` | string | Yes | Keeps payload explicit and future-safe if more admin subtypes are added. |
| `joiningDate` | date | Yes | Used in admin profile and table display. |
| `accessLevel` | string | Yes | Future permission template selector for admin privilege depth. |
| `branchId` | UUID | No | Links admin to a specific branch when the admin is not global. |
| `address` | string | No | Profile and contact detail for the created admin. |
| `allBranchesAccess` | boolean | No | Controls whether the admin can operate across all branches or one branch only. |

### Success Response

```json
{
  "success": true,
  "message": "Admin created successfully",
  "data": {
    "id": "f5f4862c-7f49-49fe-ae54-078fd0bbf7e8",
    "userId": "2c421b28-4f72-41a5-8d87-c6f1d215db76",
    "fullName": "Branch Admin",
    "email": "branch.admin@school.com",
    "loginId": "ADM00013",
    "role": "ADMIN",
    "joiningDate": "2026-05-08",
    "accessLevel": "FULL",
    "allBranchesAccess": true,
    "branchId": null,
    "branchName": "All Branches",
    "isActive": true
  }
}
```

### Backend Implementation Steps

1. Validate required form fields.
2. Validate branch existence.
3. Ensure email uniqueness.
4. Ensure login ID uniqueness.
5. Ensure password confirmation.
6. Load role `ADMIN`.
7. Create `users` row.
8. Assign branch only if admin is branch-scoped.
9. Create `admin_profiles` row.
10. Write activity record in `operational_records`.
11. Return created admin response.

## 10.3 Admin Detail

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/admins/{adminId}`
- Auth: `SUPER_ADMIN`
- Purpose: Returns details for view/edit drawer or page.

### Success Response

The response shape matches `AdminResponse` from the list and returns the full admin profile.

## 10.4 Update Admin

- Status: `IMPLEMENTED`
- Endpoint: `PUT /api/super-admin/admins/{adminId}`
- Auth: `SUPER_ADMIN`
- Purpose: Updates editable admin profile fields.

### Request Payload

Same shape as the create admin payload, except `password` and `confirmPassword` are optional during update.

### Update Rules

- if both password fields are omitted, password remains unchanged
- if one password field is provided, both must match
- if `allBranchesAccess=false`, `branchId` is required
- if `allBranchesAccess=true`, the backend clears the branch mapping

### Backend Implementation Steps

1. Load admin profile and linked user.
2. Validate role remains `ADMIN`.
3. Validate unique email and login ID excluding current user.
4. Apply optional password update and revoke sessions if password changes.
5. Update user and `admin_profiles`.
6. Write update activity record.
7. Return updated admin response.

## 10.5 Change Admin Status

- Status: `IMPLEMENTED`
- Endpoint: `PATCH /api/super-admin/admins/{adminId}/status`
- Auth: `SUPER_ADMIN`
- Purpose: Activates or deactivates an admin.

### Request Payload

```json
{
  "isActive": false,
  "reason": "Left organization"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `isActive` | boolean | Yes | Target account state for login control. |
| `reason` | string | No | Optional audit note for admin state change. |

### Backend Implementation Steps

1. Load admin user.
2. Verify user has role `ADMIN`.
3. Update `is_active`.
4. If disabling, revoke refresh tokens.
5. Write activity record.
6. Return updated state.

## 10.6 Delete Admin

- Status: `IMPLEMENTED`
- Endpoint: `DELETE /api/super-admin/admins/{adminId}`
- Auth: `SUPER_ADMIN`
- Purpose: Soft deletes an admin account without hard-deleting rows.

### Request Payload

No payload.

### Success Response

```json
{
  "success": true,
  "message": "Admin deleted successfully",
  "data": null
}
```

### Backend Implementation Steps

1. Load admin profile and linked user.
2. Soft delete the admin profile.
3. Soft delete and deactivate the linked user.
4. Revoke all refresh tokens for that admin.
5. Write delete activity record.
6. Return success envelope.

## 11. Teacher Management APIs

## 11.1 Teacher List

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/teachers`
- Auth: `SUPER_ADMIN`
- Purpose: Fills teacher management table and cards.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `branchId` | UUID | No | Filters teachers by branch. |
| `subject` | string | No | Filters by legacy subject label, catalogue display name, or subject code. |
| `subjectId` | UUID | No | Filters by canonical subject catalogue ID from `GET /api/courses/subjects`. |
| `isActive` | boolean | No | Filters active or inactive teacher accounts. |
| `search` | string | No | Searches by name, email, or phone. |
| `page` | integer | No | Table page index. |
| `size` | integer | No | Table page size. |

### Success Response

```json
{
  "success": true,
  "data": {
    "summary": {
      "totalTeachers": 87,
      "activeTeachers": 78,
      "inactiveTeachers": 9
    },
    "content": [
      {
        "id": "f41c35fa-c842-4f9c-a4f4-98fb915ed6d2",
        "userId": "2c421b28-4f72-41a5-8d87-c6f1d215db76",
        "fullName": "Neha Patil",
        "email": "neha.patil@institute.com",
        "phone": "9876543210",
        "loginId": "TEA00087",
        "dateOfBirth": "1990-04-15",
        "gender": "FEMALE",
        "profilePhotoUrl": "https://cdn.example.com/teacher.jpg",
        "qualification": "M.Sc. Mathematics",
        "experienceYears": 6,
        "subjects": ["Mathematics", "Physics"],
        "subjectIds": [
          "11111111-1111-1111-1111-111111111111",
          "22222222-2222-2222-2222-222222222222"
        ],
        "specialization": "Algebra",
        "joiningDate": "2026-05-08",
        "employmentType": "FULL_TIME",
        "salaryType": "MONTHLY",
        "hourlyRate": 0,
        "address": "Pune",
        "branchId": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
        "branchName": "Main Branch",
        "isActive": true,
        "lastLoginAt": null,
        "createdAt": "2026-05-11T10:30:00",
        "updatedAt": "2026-05-11T10:30:00"
      }
    ],
    "page": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 87,
      "totalPages": 9,
      "first": true,
      "last": false
    }
  }
}
```

## 11.2 Create Teacher

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/super-admin/teachers`
- Auth: `SUPER_ADMIN`
- Purpose: Creates a teacher account and linked teacher profile.

### Request Payload

```json
{
  "fullName": "Neha Patil",
  "email": "neha.patil@institute.com",
  "phone": "9876543210",
  "dateOfBirth": "1990-04-15",
  "gender": "FEMALE",
  "profilePhotoUrl": "https://cdn.example.com/teacher.jpg",
  "qualification": "M.Sc. Mathematics",
  "experienceYears": 6,
  "subjectIds": [
    "11111111-1111-1111-1111-111111111111",
    "22222222-2222-2222-2222-222222222222"
  ],
  "subjects": ["Mathematics", "Physics"],
  "specialization": "Algebra",
  "joiningDate": "2026-05-08",
  "employmentType": "FULL_TIME",
  "salaryType": "MONTHLY",
  "hourlyRate": 0,
  "loginId": "TEA00087",
  "password": "Password@123",
  "confirmPassword": "Password@123",
  "branchId": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
  "address": "Pune"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `fullName` | string | Yes | Teacher display name used in lists and assignment flows. |
| `email` | string | Yes | Login email and official contact address. |
| `phone` | string | Yes | Contact field for management views. |
| `dateOfBirth` | date | No | Personal profile field shown in UI. |
| `gender` | enum | No | Personal profile field shown in UI. |
| `profilePhotoUrl` | string | No | Teacher avatar for table and profile views. |
| `qualification` | string | Yes | Academic qualification shown in teacher records. |
| `experienceYears` | integer | No | Used in teacher listing and profile overview. |
| `subjectIds` | array[UUID] | Preferred | Canonical subject catalogue links from `GET /api/courses/subjects`. Use this for new screens. |
| `subjects` | array[string] | Backward compatible | Legacy free-text subject labels. Required only when `subjectIds` is empty or omitted. |
| `specialization` | string | No | Optional academic specialization detail. |
| `joiningDate` | date | Yes | Used for teacher profile and payout timelines. |
| `employmentType` | string | Yes | Distinguishes full-time, part-time, or contractual teacher. |
| `salaryType` | string | Yes | Needed because payout screens may support monthly or hourly logic. |
| `hourlyRate` | number | Conditional | Required for payout tracking when teacher is hourly-based. |
| `loginId` | string | Yes | Business-friendly teacher ID for system tracking. |
| `password` | string | Yes | Initial teacher credential. |
| `confirmPassword` | string | Yes | Prevents password mismatch at creation time. |
| `branchId` | UUID | Yes | Branch mapping for teacher scope. |
| `address` | string | No | Contact detail shown in teacher profile. |

### Backend Implementation Steps

1. Validate branch and unique identifiers.
2. Create `users` row with role `TEACHER`.
3. Create `teachers` row linked to `user_id`.
4. Store branch link and hourly rate.
5. Persist catalogue subjects in `teacher_subject_assignments`.
6. Keep legacy labels in `teacher_subjects` for older screens and easy display.
7. Return teacher summary payload with both `subjects` and `subjectIds`.

## 11.3 Teacher Detail

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/teachers/{teacherId}`
- Auth: `SUPER_ADMIN`
- Purpose: Returns details for view/edit drawer or page.

### Success Response

The response shape matches `TeacherResponse` from the list and returns the full teacher profile.

## 11.4 Update Teacher

- Status: `IMPLEMENTED`
- Endpoint: `PUT /api/super-admin/teachers/{teacherId}`
- Auth: `SUPER_ADMIN`
- Purpose: Updates editable teacher profile fields.

### Request Payload

Same shape as the create teacher payload, except `password` and `confirmPassword` are optional during update.

### Update Rules

- `branchId` is required and must point to an existing branch.
- if both password fields are omitted, password remains unchanged.
- if one password field is provided, both must match.
- email and login ID must remain unique across non-deleted users.
- use `subjectIds` for catalogue-aligned updates; `subjects` remains supported for old payloads.

## 11.5 Change Teacher Status

- Status: `IMPLEMENTED`
- Endpoint: `PATCH /api/super-admin/teachers/{teacherId}/status`
- Auth: `SUPER_ADMIN`
- Purpose: Activates or deactivates a teacher account.

### Request Payload

```json
{
  "isActive": false,
  "reason": "Left organization"
}
```

## 11.6 Delete Teacher

- Status: `IMPLEMENTED`
- Endpoint: `DELETE /api/super-admin/teachers/{teacherId}`
- Auth: `SUPER_ADMIN`
- Purpose: Soft deletes a teacher account and linked user account.

### Success Response

```json
{
  "success": true,
  "message": "Teacher deleted successfully",
  "data": null
}
```

## 12. Student Management APIs

## 12.1 Student List

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/students`
- Auth: `SUPER_ADMIN`
- Purpose: Fills student management table and top cards.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `branchId` | UUID | No | Filters by branch. |
| `standard` | string | No | Filters by class or standard. |
| `batch` | string | No | Filters by batch or section once mapped. |
| `isActive` | boolean | No | Filters active or inactive students. |
| `search` | string | No | Searches by student name, student ID, mobile, email, parent name, parent phone, or login ID. |
| `page` | integer | No | Table page index. |
| `size` | integer | No | Table page size. |

### Success Response

```json
{
  "success": true,
  "data": {
    "summary": {
      "totalStudents": 2453,
      "activeStudents": 2286,
      "inactiveStudents": 167,
      "totalBranches": 2,
      "activeBranches": 2
    },
    "content": [
      {
        "id": "6f75e8c9-c492-442a-8f06-84de5e040c07",
        "userId": "2c421b28-4f72-41a5-8d87-c6f1d215db76",
        "fullName": "Aarav Sharma",
        "studentId": "STU240001",
        "branchId": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
        "branchName": "Main Branch",
        "standard": "9th",
        "batch": "9th CBSE A",
        "gender": "MALE",
        "dateOfBirth": "2011-03-27",
        "mobile": "9876543210",
        "parentName": "Rajesh Sharma",
        "parentPhone": "9876543211",
        "email": "aarav@example.com",
        "address": "Pune",
        "schoolName": "Greenfield Public School",
        "board": "CBSE",
        "admissionDate": "2026-05-08",
        "loginId": "stu240001",
        "profilePhotoUrl": "https://cdn.example.com/student.jpg",
        "isAdmissionFinal": true,
        "isActive": true,
        "lastLoginAt": null,
        "createdAt": "2026-05-11T10:45:00",
        "updatedAt": "2026-05-11T10:45:00"
      }
    ],
    "page": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 2453,
      "totalPages": 246,
      "first": true,
      "last": false
    }
  }
}
```

## 12.2 Create Student

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/super-admin/students`
- Auth: `SUPER_ADMIN`
- Purpose: Creates student record and future student login.

### Request Payload

```json
{
  "fullName": "Aarav Sharma",
  "studentId": "STU240001",
  "branchId": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
  "standard": "9th",
  "batch": "9th CBSE A",
  "courseId": "a0000001-0000-0000-0000-000000000004",
  "batchId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "subjectGroupId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "subjectIds": [
    "11111111-1111-1111-1111-111111111111",
    "22222222-2222-2222-2222-222222222222"
  ],
  "agreedTotalFee": 45000,
  "paymentPlan": "INSTALMENT_3",
  "instalments": [
    { "instalmentNumber": 1, "label": "At Admission", "amount": 25000, "dueDate": "2026-05-08", "isPostDatedCheque": false },
    { "instalmentNumber": 2, "label": "Second Instalment", "amount": 10000, "dueDate": "2026-07-15", "isPostDatedCheque": true },
    { "instalmentNumber": 3, "label": "Third Instalment", "amount": 10000, "dueDate": "2026-09-15", "isPostDatedCheque": true }
  ],
  "enrolmentNotes": "Manual fee entered by admin",
  "gender": "MALE",
  "dateOfBirth": "2011-03-27",
  "mobile": "9876543210",
  "parentName": "Rajesh Sharma",
  "parentPhone": "9876543211",
  "email": "aarav@example.com",
  "address": "Pune",
  "schoolName": "Greenfield Public School",
  "board": "CBSE",
  "admissionDate": "2026-05-08",
  "loginId": "STU240001",
  "password": "Password@123",
  "confirmPassword": "Password@123",
  "profilePhotoUrl": "https://cdn.example.com/student.jpg"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `fullName` | string | Yes | Student display name shown throughout the system. |
| `studentId` | string | Yes | Business-visible student identifier shown in list and reports. |
| `branchId` | UUID | Yes | Student branch mapping for all branch filters. |
| `standard` | string | Yes | Class or grade used in filters and academic grouping. |
| `batch` | string | Conditional | Section or batch label shown in table. Required only when `batchId` is omitted. If `batchId` is sent, backend stores the canonical batch name. |
| `courseId` | UUID | Optional | Optional frontend consistency check. If sent with `batchId`, the batch must belong to this course. |
| `batchId` | UUID | Optional | Canonical batch selection. When sent with `subjectIds` and `agreedTotalFee`, the API also creates a `student_enrolments` row. |
| `subjectGroupId` | UUID | Optional | The subject group used as a starting point in the UI. Informational only. |
| `subjectIds` | array[UUID] | Conditional | Actual selected subjects. Required when creating enrolment from this student API. |
| `agreedTotalFee` | decimal | Conditional | Manually entered fee. Required when creating enrolment from this student API. No backend fee calculation. |
| `paymentPlan` | enum | Optional | `REGULAR`, `LUMPSUM`, `INSTALMENT_2`, or `INSTALMENT_3`. Defaults to `REGULAR` when omitted with enrolment data. |
| `instalments` | array | Optional | Manually entered payment schedule for the enrolment. |
| `enrolmentNotes` | string | No | Internal admission/enrolment note. |
| `gender` | enum | No | Demographic field for profile and analytics. |
| `dateOfBirth` | date | No | Personal profile field. |
| `mobile` | string | Yes | Student or primary contact number. |
| `parentName` | string | Yes | Guardian name shown in list and communication flows. |
| `parentPhone` | string | Yes | Guardian contact for outreach and future parent portal. |
| `email` | string | No | Optional student or guardian email contact. Student login can use `loginId` when email is absent. |
| `address` | string | No | Contact detail shown in detail views. |
| `schoolName` | string | No | Needed because admission designs capture prior or current school. |
| `board` | string | Yes | Academic board, aligned with schema check values. |
| `admissionDate` | date | Yes | Needed for reports, admissions, and audit timeline. |
| `loginId` | string | Yes | Future student login identity. |
| `password` | string | Yes | Initial student credential. |
| `confirmPassword` | string | Yes | Prevents password mismatch. |
| `profilePhotoUrl` | string | No | Avatar shown in student list. |

### Backend Implementation Steps

1. Validate branch and board value.
2. Create `users` account with role `STUDENT`. Email is optional for future parent/student login flows; `loginId` is required.
3. Create `students` row linked by `user_id`.
4. If `batchId`, `subjectIds`, and `agreedTotalFee` are present, create a linked `student_enrolments` row.
5. Store only the manually entered amount and instalments; never calculate fees from course, batch, or subjects.
6. Mark active and current admission state.
7. Return student summary response.

## 12.3 Student Detail

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/students/{studentId}`
- Auth: `SUPER_ADMIN`
- Purpose: Returns details for view/edit drawer or page.

### Success Response

The response shape matches `StudentResponse` from the list and returns the full student profile.

## 12.4 Update Student

- Status: `IMPLEMENTED`
- Endpoint: `PUT /api/super-admin/students/{studentId}`
- Auth: `SUPER_ADMIN`
- Purpose: Updates editable student profile fields.

### Request Payload

Same shape as the create student payload, except `password` and `confirmPassword` are optional during update.

### Update Rules

- `branchId` is required and must point to an existing branch.
- `studentId` must be unique across non-deleted students.
- `loginId` must be unique across non-deleted users.
- `email` is optional, but if present it must be unique across non-deleted users.
- if both password fields are omitted, password remains unchanged.
- if one password field is provided, both must match.
- course, batch, subject, and fee changes after admission should use `PUT /api/enrolments/{enrolmentId}` rather than student profile update.

## 12.5 Change Student Status

- Status: `IMPLEMENTED`
- Endpoint: `PATCH /api/super-admin/students/{studentId}/status`
- Auth: `SUPER_ADMIN`
- Purpose: Activates or deactivates a student account.

### Request Payload

```json
{
  "isActive": false,
  "reason": "Transferred to another institute"
}
```

## 12.6 Bulk Import Students

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/students/import`
- Auth: `SUPER_ADMIN`
- Purpose: Supports `Import Students` action visible in design.

## 13. Leads and Admissions APIs

## 13.1 Lead List

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/leads`
- Auth: `SUPER_ADMIN`
- Purpose: Lists inquiry leads for the lead management screen.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `search` | string | No | Searches lead code, student name, parent/guardian name, phone, or email. |
| `branchId` | UUID | No | Filters by preferred branch. |
| `status` | string | No | Filters `NEW`, `CONTACTED`, `IN_FOLLOW_UP`, `CONVERTED`, or `NOT_INTERESTED`. |
| `leadSource` | string | No | Filters by source such as referral, social media, website, banner, or pamphlet. |
| `courseId` | UUID | No | Filters by selected/recommended course. |
| `batchId` | UUID | No | Filters by selected/recommended batch. |
| `page` | integer | No | Table page index. |
| `size` | integer | No | Table page size. |

## 13.2 Create Lead

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/super-admin/leads`
- Auth: `SUPER_ADMIN`
- Purpose: Saves the long lead inquiry form shown in design.

### Request Payload

The payload should be split into:
- student information
- parent or guardian information
- lead source and inquiry details
- counselor recommendation
- documents

### Required Core Fields

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `studentName` | string | Yes | Lead identity for inquiry tracking. |
| `gender` | enum | No | Student demographic detail from form. |
| `dateOfBirth` | date | No | Student profile field from form. |
| `classInterestedIn` | string | No | Helps route lead to the correct academic offering. |
| `board` | string | No | Board-sensitive inquiry detail. |
| `medium` | string | No | Academic medium filter from form. |
| `stream` | string | No | Needed for higher-class admission routing. |
| `address` | string | No | Contact and locality context. |
| `mobileNumber` | string | Yes | Primary lead contact number. |
| `alternateMobileNumber` | string | No | Backup communication channel. |
| `email` | string | No | Email communication for reminders. |
| `fatherName` | string | No | Parent identity. |
| `motherName` | string | No | Parent identity. |
| `relation` | string | No | Guardian relationship to the student. |
| `leadSource` | string | Yes | Funnel tracking for analytics and conversion. |
| `referredBy` | string | No | Source attribution when referral exists. |
| `preferredBranchId` | UUID | No | Branch routing for admission ownership. Required when converting if no branch is already set. |
| `courseId` | UUID | No | Optional catalogue course selected during inquiry/counseling. |
| `batchId` | UUID | No | Optional target batch selected during inquiry/counseling. |
| `subjectIds` | array[UUID] | No | Optional subjects selected or suggested from the catalogue. |
| `expectedAdmissionYear` | string | No | Academic cycle target for follow-up. |
| `nextFollowUpAt` | datetime | No | Follow-up scheduling aid. |
| `preferredContactTime` | string | No | Contact timing preference. |
| `modeOfContact` | string | No | Call, WhatsApp, email, or SMS preference. |
| `assignedToUserId` | UUID | No | Counselor or staff user assigned to the lead. |
| `courseRecommended` | string | No | Counselor recommendation detail. |
| `batchSuggested` | string | No | Recommended batch or section. |
| `admissionLikelihood` | string | No | Funnel confidence used in counseling. |
| `remarks` | string | No | Free-text counselor or staff notes. |

### Sample Payload

```json
{
  "studentName": "Aarav Singh",
  "gender": "MALE",
  "dateOfBirth": "2011-03-27",
  "classInterestedIn": "8th",
  "board": "CBSE",
  "medium": "English",
  "currentSchool": "Greenfield Public School",
  "mobileNumber": "9876543210",
  "email": "aarav@example.com",
  "fatherName": "Rahul Singh",
  "fatherMobileNumber": "9876543210",
  "leadSource": "Parent Referral",
  "preferredBranchId": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
  "courseId": "a0000001-0000-0000-0000-000000000004",
  "batchId": "batch-uuid",
  "subjectIds": ["subject-uuid-1", "subject-uuid-2"],
  "expectedAdmissionYear": "2026-27",
  "nextFollowUpAt": "2026-05-15T10:30:00",
  "modeOfContact": "CALL",
  "courseRecommended": "Std. 8th CBSE Batch",
  "batchSuggested": "Chanakya",
  "admissionLikelihood": "HIGH",
  "remarks": "Interested in Maths and Science."
}
```

### Backend Implementation Steps

1. Validate optional branch, course, batch, subject, and assignee references.
2. Save lead master row in `lead_inquiries`.
3. Save selected subjects in `lead_subjects`.
4. Store source, status, recommendation, and follow-up preferences.
5. Write operational record with module `LEAD` for dashboard and analytics funnel.

## 13.3 Lead Detail

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/leads/{leadId}`
- Auth: `SUPER_ADMIN`
- Purpose: Returns full lead details plus follow-up history.

## 13.4 Update Lead

- Status: `IMPLEMENTED`
- Endpoint: `PUT /api/super-admin/leads/{leadId}`
- Auth: `SUPER_ADMIN`
- Purpose: Updates inquiry details, optional course/batch/subject choices, and counselor recommendation fields.

## 13.5 Update Lead Status

- Status: `IMPLEMENTED`
- Endpoint: `PATCH /api/super-admin/leads/{leadId}/status`
- Auth: `SUPER_ADMIN`
- Purpose: Updates lead status and adds a follow-up history row.

### Request Payload

```json
{
  "status": "IN_FOLLOW_UP",
  "notes": "Parent asked for fee details and batch timing.",
  "modeOfContact": "CALL",
  "nextFollowUpAt": "2026-05-16T11:00:00"
}
```

## 13.6 Convert Lead to Admission

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/super-admin/leads/{leadId}/convert-to-admission`
- Auth: `SUPER_ADMIN`
- Purpose: Converts a lead to a student admission and optionally creates a batch enrolment.

### Request Payload

```json
{
  "studentId": "STU240001",
  "branchId": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
  "standard": "8th",
  "batch": "Chanakya",
  "board": "CBSE",
  "admissionDate": "2026-05-16",
  "batchId": "batch-uuid",
  "subjectGroupId": "subject-group-uuid",
  "subjectIds": ["subject-uuid-1", "subject-uuid-2"],
  "agreedTotalFee": 45000,
  "paymentPlan": "INSTALMENTS",
  "instalments": [
    {
      "instalmentNumber": 1,
      "label": "At admission",
      "amount": 30000,
      "dueDate": "2026-05-16",
      "isPostDatedCheque": false
    }
  ],
  "notes": "Manual fee entered by admin."
}
```

### Conversion Rules

- The backend does not calculate fees from course, batch, subject group, or subject selections.
- `agreedTotalFee` and instalments are manually entered by super admin/admin.
- If `batchId`, `subjectIds`, and `agreedTotalFee` are present, the API creates a `student_enrolments` row.
- If enrolment fields are omitted, the API only creates the student admission and marks the lead converted.
- `School-Level Competitive Examination` data from the brochure is intentionally excluded from the catalogue.

### Success Response

```json
{
  "success": true,
  "message": "Lead converted successfully",
  "data": {
    "leadId": "6f75e8c9-c492-442a-8f06-84de5e040c07",
    "leadCode": "INQ/2026/240001",
    "studentId": "2c421b28-4f72-41a5-8d87-c6f1d215db76",
    "visibleStudentId": "STU240001",
    "convertedAt": "2026-05-16T16:00:00",
    "enrolment": {
      "id": "enrolment-uuid",
      "batchId": "batch-uuid",
      "agreedTotalFee": 45000,
      "paymentPlan": "INSTALMENTS"
    }
  }
}
```

## 13.7 Delete Lead

- Status: `IMPLEMENTED`
- Endpoint: `DELETE /api/super-admin/leads/{leadId}`
- Auth: `SUPER_ADMIN`
- Purpose: Soft deletes a lead inquiry.

## 13.8 Admission List

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/admissions`
- Auth: `SUPER_ADMIN`
- Purpose: Dedicated admission list can be added later. Current conversion creates/updates `students` and optional `student_enrolments`.

## 13.9 Create Admission

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/admissions`
- Auth: `SUPER_ADMIN`
- Purpose: Dedicated direct-admission form can be added later. Current student management and lead conversion cover the admission creation path.

## 14. Analytics APIs

## 14.1 Analytics Overview

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/analytics`
- Auth: `SUPER_ADMIN`
- Purpose: Fills the Analytics Overview and Analytics Students screens with cards, tabs, and chart-ready data.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `tab` | enum | No | Controls current analytics tab. Currently implemented values are `overview` and `students`. Other tab labels are returned for UI navigation but their detailed datasets are planned. |
| `branchId` | UUID | No | Optional branch filter. |
| `fromDate` | date | No | Lower reporting bound. Defaults to the first day of the `toDate` year. |
| `toDate` | date | No | Upper reporting bound. Defaults to current date. |

### Overview Tab Request

```http
GET /api/super-admin/analytics?tab=overview&fromDate=2026-05-01&toDate=2026-05-14
```

### Overview Tab Success Response

```json
{
  "success": true,
  "data": {
    "fromDate": "2026-05-01",
    "toDate": "2026-05-14",
    "branchId": null,
    "branchName": "All Branches",
    "activeTab": "overview",
    "branches": [
      {
        "id": "72ca236b-b9d7-4c10-b5b6-28ddf4c9d432",
        "name": "Main Branch"
      }
    ],
    "tabs": [
      { "key": "overview", "label": "Overview" },
      { "key": "students", "label": "Students" },
      { "key": "teachers", "label": "Teachers" },
      { "key": "attendance", "label": "Attendance" },
      { "key": "academics", "label": "Academics" },
      { "key": "finance", "label": "Finance" },
      { "key": "admissions", "label": "Admissions" },
      { "key": "performance", "label": "Performance" },
      { "key": "feedback", "label": "Feedback" }
    ],
    "overview": {
      "cards": [
        {
          "key": "totalStudents",
          "label": "Total Students",
          "totalCount": 2453,
          "changePercentage": 12.50,
          "comparisonLabel": "vs last month",
          "trendDirection": "up"
        },
        {
          "key": "feesCollected",
          "label": "Fees Collected",
          "totalAmount": 4875000,
          "changePercentage": 18.60,
          "comparisonLabel": "vs previous period",
          "trendDirection": "up"
        }
      ],
      "studentGrowth": [
        {
          "month": "Jan",
          "currentValue": 700,
          "previousValue": 420
        }
      ],
      "feeCollectionOverview": [
        {
          "month": "May",
          "feesCollected": 4875000,
          "pendingFees": 875600
        }
      ],
      "leadConversionFunnel": {
        "stages": [
          {
            "label": "Total Leads",
            "count": 1250,
            "percentage": 100.00
          },
          {
            "label": "Interested",
            "count": 650,
            "percentage": 52.00
          },
          {
            "label": "Converted",
            "count": 320,
            "percentage": 25.60
          },
          {
            "label": "Admissions",
            "count": 285,
            "percentage": 22.80
          }
        ]
      },
      "admissionsOverview": {
        "totalAdmissions": 285,
        "branches": [
          {
            "label": "Main Branch",
            "count": 162,
            "percentage": 56.84
          }
        ],
        "conversionRate": 22.80,
        "inquiryToAdmissionRate": 22.80
      }
    }
  }
}
```

### Students Tab Request

```http
GET /api/super-admin/analytics?tab=students&fromDate=2026-05-01&toDate=2026-05-14
```

### Students Tab Success Response

```json
{
  "success": true,
  "data": {
    "fromDate": "2026-05-01",
    "toDate": "2026-05-14",
    "branchId": null,
    "branchName": "All Branches",
    "activeTab": "students",
    "students": {
      "cards": [
        {
          "key": "totalStudents",
          "label": "Total Students",
          "totalCount": 2453,
          "changePercentage": 12.50,
          "comparisonLabel": "vs last month",
          "trendDirection": "up"
        },
        {
          "key": "activeStudents",
          "label": "Active Students",
          "totalCount": 2286,
          "sharePercentage": 93.19,
          "shareLabel": "of total",
          "trendDirection": "neutral"
        }
      ],
      "studentGrowth": [
        {
          "month": "Jan",
          "currentValue": 700,
          "previousValue": 420
        }
      ],
      "studentsByClass": {
        "total": 2453,
        "segments": [
          {
            "label": "9th",
            "count": 360,
            "percentage": 14.68
          }
        ]
      },
      "studentsByGender": {
        "total": 2453,
        "segments": [
          {
            "label": "MALE",
            "count": 1296,
            "percentage": 52.83
          },
          {
            "label": "FEMALE",
            "count": 1157,
            "percentage": 47.17
          }
        ]
      },
      "admissionsVsDropped": [
        {
          "month": "May",
          "newAdmissions": 245,
          "droppedStudents": 36
        }
      ]
    }
  }
}
```

### Backend Implementation Steps

1. Resolve branch and date filters.
2. Return active branch options for header filters.
3. Return tab metadata for the visible analytics navigation.
4. For `overview`, aggregate student, teacher, admin, fee, lead conversion, and admission data.
5. For `students`, aggregate active/inactive students, new admissions, dropped students, class distribution, gender distribution, and admission/drop monthly series.
6. Use `students`, `teachers`, `users`, and `operational_records`.
7. Use `admission_date` for admission analytics instead of row creation time.
8. Return one consolidated response for the active tab.

## 15. Reports APIs

## 15.1 Reports Summary

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/reports/summary`
- Auth: `SUPER_ADMIN`
- Purpose: Fills report dashboard cards and category tiles.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `branchId` | UUID | No | Optional branch filter. |
| `fromDate` | date | No | Lower reporting bound. Defaults to first day of current month. |
| `toDate` | date | No | Upper reporting bound. Defaults to current date. |

### Success Response

```json
{
  "success": true,
  "data": {
    "fromDate": "2026-05-01",
    "toDate": "2026-05-14",
    "summary": {
      "totalStudents": 2453,
      "totalTeachers": 87,
      "totalAdmissions": 245,
      "feesCollected": 4875000,
      "pendingFees": 875600
    },
    "categories": [
      {
        "key": "student_reports",
        "label": "Student Reports",
        "description": "View and download student related reports",
        "reportTypes": [
          {
            "key": "student_admission_report",
            "label": "Student Admission Report",
            "defaultFormat": "PDF"
          }
        ]
      }
    ],
    "recentReports": []
  }
}
```

## 15.2 Report Categories

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/reports/categories`
- Auth: `SUPER_ADMIN`
- Purpose: Returns category cards and report type options for filters/export dropdowns.

## 15.3 Reports List

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/reports`
- Auth: `SUPER_ADMIN`
- Purpose: Returns generated report rows for the reports table.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `category` | string | No | Filters student, teacher, attendance, academic, admission, or exam reports. |
| `reportType` | string | No | Narrows to specific report type. |
| `format` | string | No | Filters generated report format. |
| `branchId` | UUID | No | Filters generated reports by branch. |
| `search` | string | No | Searches report name or description. |
| `fromDate` | date | No | Start date for report filter. |
| `toDate` | date | No | End date for report filter. |
| `page` | integer | No | Table page index. |
| `size` | integer | No | Table page size. |

### Success Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "6f75e8c9-c492-442a-8f06-84de5e040c07",
        "reportName": "Student Admission Report",
        "category": "student_reports",
        "categoryLabel": "Student Reports",
        "reportType": "student_admission_report",
        "reportTypeLabel": "Student Admission Report",
        "description": "View and download student related reports",
        "generatedBy": "Super Admin",
        "generatedOn": "2026-05-14T10:30:00",
        "format": "PDF",
        "status": "READY",
        "branchId": null,
        "branchName": "All Branches",
        "fromDate": "2026-05-01",
        "toDate": "2026-05-14",
        "downloadUrl": "/api/super-admin/reports/6f75e8c9-c492-442a-8f06-84de5e040c07/download"
      }
    ],
    "page": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 1,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  }
}
```

## 15.4 Export Report

- Status: `IMPLEMENTED`
- Endpoint: `POST /api/super-admin/reports/export`
- Auth: `SUPER_ADMIN`
- Purpose: Generates a report metadata record and returns a download URL.

### Request Payload

```json
{
  "category": "student_reports",
  "reportType": "student_admission_report",
  "format": "PDF",
  "branchId": null,
  "fromDate": "2026-05-01",
  "toDate": "2026-05-14",
  "standard": "9th",
  "batch": "9th CBSE A"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `category` | string | Yes | Report category, such as `student_reports`, `teacher_reports`, `attendance_reports`, `academic_reports`, `admission_reports`, `exam_reports`, or `financial_reports`. |
| `reportType` | string | Yes | Identifies which dataset should be exported. |
| `format` | string | No | Specifies output format. Supported values are `PDF`, `CSV`, and `EXCEL`; `EXCEL` currently streams CSV-compatible bytes. |
| `branchId` | UUID | No | Optional branch-scope export. |
| `fromDate` | date | No | Lower date filter for report generation. |
| `toDate` | date | No | Upper date filter for report generation. |
| `standard` | string | No | Future-compatible student filter captured in `filters_json`. |
| `batch` | string | No | Future-compatible student filter captured in `filters_json`. |

### Success Response

```json
{
  "success": true,
  "message": "Report generated successfully",
  "data": {
    "id": "6f75e8c9-c492-442a-8f06-84de5e040c07",
    "reportName": "Student Admission Report",
    "category": "student_reports",
    "reportType": "student_admission_report",
    "format": "PDF",
    "status": "READY",
    "downloadUrl": "/api/super-admin/reports/6f75e8c9-c492-442a-8f06-84de5e040c07/download"
  }
}
```

## 15.5 Download Report

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/super-admin/reports/{reportId}/download`
- Auth: `SUPER_ADMIN`
- Purpose: Streams the generated report file to the browser as an attachment.

### Response

Returns binary file bytes with:

```http
Content-Type: application/pdf
Content-Disposition: attachment; filename="student_admission_report-2026-05-14.pdf"
```

For `CSV` or `EXCEL`, the API returns `text/csv` content. This is intentional for the first backend version because CSV opens directly in Excel and does not require adding a spreadsheet-generation dependency.

## 15.6 Delete Report

- Status: `IMPLEMENTED`
- Endpoint: `DELETE /api/super-admin/reports/{reportId}`
- Auth: `SUPER_ADMIN`
- Purpose: Soft deletes a generated report metadata record.

## 15.7 Report Storage Strategy

Recommended production strategy:

- Generate small interactive reports synchronously and stream them directly from `GET /download`.
- Store metadata in `generated_reports` for audit/history, filters, generated-by user, format, branch, and future retry/download behavior.
- Store large or long-lived generated files in S3 and keep only `storage_provider`, `storage_key`, `download_url`, and expiry metadata in PostgreSQL.
- Avoid storing large report binaries in PostgreSQL unless the files are tiny and retention requirements are short.
- Use async/background generation later for large reports, changing `status` from `QUEUED` to `PROCESSING` to `READY` or `FAILED`.

Current implementation:

- Uses `generated_reports` metadata records.
- Uses `storage_provider=ON_DEMAND`.
- Regenerates and streams the file from source data when `/download` is called.
- Supports PDF and CSV-compatible downloads without adding new third-party file-generation libraries.

## 16. Feedback APIs

## 16.1 Feedback Summary

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/feedback/summary`
- Auth: `SUPER_ADMIN`
- Purpose: Fills feedback cards and charts.

## 16.2 Feedback List

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/feedback`
- Auth: `SUPER_ADMIN`
- Purpose: Returns feedback table with filters visible in design.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `feedbackFrom` | string | No | Filters by source audience such as student, teacher, or parent. |
| `feedbackFor` | string | No | Filters target team or person. |
| `feedbackType` | string | No | Filters category such as teaching, infrastructure, or administration. |
| `rating` | integer | No | Filters star rating. |
| `fromDate` | date | No | Lower date bound. |
| `toDate` | date | No | Upper date bound. |
| `page` | integer | No | Table page index. |
| `size` | integer | No | Table page size. |

## 17. Attendance Overview APIs

## 17.1 Attendance Overview

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/attendance/overview`
- Auth: `SUPER_ADMIN`
- Purpose: Fills the attendance overview screen.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `classOrGrade` | string | No | Filters attendance by class. |
| `section` | string | No | Filters by section. |
| `branchId` | UUID | No | Optional branch filter. |
| `fromDate` | date | No | Start date filter. |
| `toDate` | date | No | End date filter. |

### Backend Implementation Steps

1. Query attendance operational records.
2. Aggregate student cards.
3. Compute present, absent, late, and percentage metrics.
4. Return class-wise breakdown and day-of-week table.

## 18. Test and Performance APIs

## 18.1 Test Performance Summary

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/tests/performance`
- Auth: `SUPER_ADMIN`
- Purpose: Fills test and performance dashboard page.

## 18.2 Create Test

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/tests`
- Auth: `SUPER_ADMIN`
- Purpose: Saves the `Add New Test` form.

### Request Payload

```json
{
  "title": "Science Weekly Test",
  "testType": "MCQ_TEST",
  "subject": "Science",
  "classOrGrade": "8th",
  "totalMarks": 100,
  "passingMarks": 40,
  "durationMinutes": 60,
  "negativeMarkingPolicy": "NONE",
  "instructions": "Answer all questions.",
  "startDateTime": "2026-05-16T10:00:00",
  "endDateTime": "2026-05-16T11:00:00",
  "resultDeclaration": "AFTER_TEST_END",
  "shuffleQuestions": true
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `title` | string | Yes | Test title shown in lists and reports. |
| `testType` | string | Yes | Supports the test type filter in design. |
| `subject` | string | Yes | Links the test to subject analytics. |
| `classOrGrade` | string | Yes | Controls student audience. |
| `totalMarks` | integer | Yes | Needed for score calculations. |
| `passingMarks` | integer | Yes | Needed for pass percentage analytics. |
| `durationMinutes` | integer | Yes | Required for scheduling and exam timing. |
| `negativeMarkingPolicy` | string | Yes | Mirrors design option for negative marking. |
| `instructions` | string | No | Student-facing test instructions. |
| `startDateTime` | datetime | Yes | Test availability start. |
| `endDateTime` | datetime | Yes | Test availability end. |
| `resultDeclaration` | string | Yes | Controls when results become visible. |
| `shuffleQuestions` | boolean | Yes | Mirrors exam configuration from UI. |

## 18.3 Add Test Questions

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/tests/{testId}/questions`
- Auth: `SUPER_ADMIN`
- Purpose: Saves questions from the `Add Questions` screen.

### Request Payload

```json
{
  "questionType": "MCQ",
  "questionText": "What is the capital of France?",
  "options": [
    {"label": "A", "text": "London"},
    {"label": "B", "text": "Paris"},
    {"label": "C", "text": "Rome"},
    {"label": "D", "text": "Berlin"}
  ],
  "correctOption": "B",
  "marks": 5,
  "negativeMarks": 0
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `questionType` | string | Yes | Supports current MCQ design and future question types. |
| `questionText` | string | Yes | Actual content shown in exam. |
| `options` | array | Yes | MCQ answer choices. |
| `correctOption` | string | Yes | Required for evaluation. |
| `marks` | integer | Yes | Per-question scoring value. |
| `negativeMarks` | number | No | Only relevant if negative marking is enabled. |

## 19. Syllabus APIs

## 19.1 Syllabus Summary

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/syllabus/summary`
- Auth: `SUPER_ADMIN`
- Purpose: Fills syllabus completion page.

## 19.2 Create Syllabus

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/syllabus`
- Auth: `SUPER_ADMIN`
- Purpose: Saves the `Add New Syllabus` structure.

### Request Payload

```json
{
  "subject": "Mathematics",
  "classOrGrade": "5th Standard",
  "academicYear": "2024-2025",
  "description": "Core maths syllabus",
  "chapters": [
    {
      "chapterName": "Algebra",
      "subTopics": [
        {"name": "Linear Equations", "questions": 15},
        {"name": "Variables and Expressions", "questions": 12}
      ]
    }
  ]
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `subject` | string | Yes | Identifies the subject this syllabus belongs to. |
| `classOrGrade` | string | Yes | Maps syllabus to student level. |
| `academicYear` | string | Yes | Keeps syllabus versioned by session. |
| `description` | string | No | Short summary shown in syllabus details. |
| `chapters` | array | Yes | Top-level syllabus structure. |
| `chapterName` | string | Yes | Chapter title visible in UI. |
| `subTopics` | array | Yes | Smaller learning units tracked for completion. |
| `questions` | integer | No | Planned question count used in assessment planning. |

## 20. Stationery APIs

## 20.1 Stationery Summary

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/stationery/summary`
- Auth: `SUPER_ADMIN`
- Purpose: Fills stationery overview cards, donut chart, and alerts.

## 20.2 Stationery Items List

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/stationery/items`
- Auth: `SUPER_ADMIN`
- Purpose: Returns inventory table records.

## 20.3 Create Stationery Item

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/stationery/items`
- Auth: `SUPER_ADMIN`
- Purpose: Adds a new inventory item.

### Request Payload

```json
{
  "itemName": "Blue Ball Pen",
  "category": "Writing",
  "unit": "Pcs",
  "stockQuantity": 1250,
  "unitPrice": 10,
  "supplier": "ABC Stationers",
  "minimumStock": 200
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `itemName` | string | Yes | Name shown in inventory table. |
| `category` | string | Yes | Used in category filters and summary grouping. |
| `unit` | string | Yes | Measurement label such as pieces or packs. |
| `stockQuantity` | integer | Yes | Opening or current stock count. |
| `unitPrice` | number | Yes | Used to compute stock value. |
| `supplier` | string | No | Supplier filter and purchase context. |
| `minimumStock` | integer | Yes | Used to generate low stock alerts. |

## 20.4 Stock Movement

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/stationery/transactions`
- Auth: `SUPER_ADMIN`
- Purpose: Records stock in and stock out events.

## 21. Timesheet APIs

## 21.1 Timesheet Summary

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/timesheets`
- Auth: `SUPER_ADMIN`
- Purpose: Fills the admin timesheet summary and table view.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `status` | string | No | Filters approved, pending, or rejected timesheets. |
| `search` | string | No | Searches by admin name or admin ID. |
| `fromDate` | date | No | Week or date range start. |
| `toDate` | date | No | Week or date range end. |
| `page` | integer | No | Table page index. |
| `size` | integer | No | Table page size. |

## 21.2 Timesheet Detail

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/timesheets/{timesheetId}`
- Auth: `SUPER_ADMIN`
- Purpose: Fills the timesheet details page.

## 21.3 Review Timesheet

- Status: `PLANNED`
- Endpoint: `PATCH /api/super-admin/timesheets/{timesheetId}/review`
- Auth: `SUPER_ADMIN`
- Purpose: Approves or rejects an admin timesheet entry set.

### Request Payload

```json
{
  "status": "APPROVED",
  "comment": "All entries have been reviewed."
}
```

## 22. Teacher Payout APIs

## 22.1 Teacher Payout Summary

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/teacher-payouts`
- Auth: `SUPER_ADMIN`
- Purpose: Fills teacher payout tracking screen.

## 22.2 Process Teacher Payout

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/teacher-payouts/{teacherId}/process`
- Auth: `SUPER_ADMIN`
- Purpose: Marks pending payout as paid for a teacher and date range.

### Request Payload

```json
{
  "fromDate": "2026-05-01",
  "toDate": "2026-05-31",
  "paidAmount": 93900,
  "paymentReference": "UTR12345",
  "remarks": "Processed for May payroll"
}
```

## 23. System Settings APIs

The design shows system settings split into tabs, so the API should also be modular rather than one oversized settings payload.

## 23.1 Get General Settings

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/settings/general`
- Auth: `SUPER_ADMIN`
- Purpose: Returns school information, preferences, grading, email, SMS, and backup settings for the general settings screen.

## 23.2 Update General Settings

- Status: `PLANNED`
- Endpoint: `PUT /api/super-admin/settings/general`
- Auth: `SUPER_ADMIN`
- Purpose: Updates general settings form.

## 23.3 Get Academic Settings

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/settings/academic`

## 23.4 Update Academic Settings

- Status: `PLANNED`
- Endpoint: `PUT /api/super-admin/settings/academic`

## 23.5 Get Attendance Settings

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/settings/attendance`

## 23.6 Update Attendance Settings

- Status: `PLANNED`
- Endpoint: `PUT /api/super-admin/settings/attendance`

## 23.7 Get Exam and Test Settings

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/settings/exam-test`

## 23.8 Update Exam and Test Settings

- Status: `PLANNED`
- Endpoint: `PUT /api/super-admin/settings/exam-test`

## 23.9 Get Notification Settings

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/settings/notifications`

## 23.10 Update Notification Settings

- Status: `PLANNED`
- Endpoint: `PUT /api/super-admin/settings/notifications`

## 23.11 Get Security Settings

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/settings/security`

## 23.12 Update Security Settings

- Status: `PLANNED`
- Endpoint: `PUT /api/super-admin/settings/security`

### Example Update Payload for Settings Tabs

The exact payload differs by tab, but the design suggests the backend should:
- keep each tab in a dedicated DTO
- validate only tab-specific fields
- persist only what the current tab owns
- avoid sending one giant cross-tab settings payload

## 24. Authorization Matrix for Super Admin Phase

| API Group | Public | Authenticated | `SUPER_ADMIN` |
|---|---:|---:|---:|
| Bootstrap super admin | Yes | No | No |
| Login and refresh | Yes | No | No |
| Logout, me, change password | No | Yes | No |
| Super admin dashboard | No | No | Yes |
| Branch overview | No | No | Yes |
| Admin management | No | No | Yes |
| Teacher management | No | No | Yes |
| Student management | No | No | Yes |
| Leads and admissions | No | No | Yes |
| Analytics | No | No | Yes |
| Reports | No | No | Yes |
| Feedback | No | No | Yes |
| Attendance overview | No | No | Yes |
| Tests and syllabus | No | No | Yes |
| Stationery, timesheet, payouts | No | No | Yes |
| System settings | No | No | Yes |

## 25. Database Notes for API Builders

Current schema already provides these foundations:
- `users`
- `roles`
- `user_roles`
- `branches`
- `admin_profiles`
- `teachers`
- `students`
- `subjects`
- `courses`
- `subject_groups`
- `batches`
- `student_enrolments`
- `lead_inquiries`
- `lead_follow_ups`
- `generated_reports`
- `refresh_tokens`
- `operational_records`

Admin management is now implemented using:
- `users` for login identity, status, login tracking, and branch link
- `admin_profiles` for admin-only profile fields like date of birth, gender, joining date, access level, address, and all-branches access
- `operational_records` for create, update, activate, deactivate, and delete activity entries

Teacher management is now implemented using:
- `users` for login identity, status, login tracking, profile photo, and branch link
- `teachers` for teacher-only profile fields like date of birth, gender, qualification, experience, specialization, joining date, employment type, salary type, hourly rate, and address
- `teacher_subjects` for multi-subject storage and subject filtering
- `operational_records` for create, update, activate, deactivate, and delete activity entries

Student management is now implemented using:
- `users` for login identity, status, login tracking, profile photo, and branch link
- nullable `users.email` so future parent/student login flows can use `loginId` without forcing an email address
- `students` for student-only profile fields like student ID, standard, batch, parent contact, school, board, admission date, and admission status
- `StudentManagementService` as shared business logic so future admin and teacher controllers can reuse the same student management rules with role-specific branch and permission checks
- `operational_records` for create, update, activate, deactivate, and delete activity entries

Analytics is now implemented using:
- `students` for total, active, inactive, class, gender, admission-date, and growth metrics
- `teachers` and role-scoped `users` for overview card totals
- `operational_records` for fee, pending fee, lead funnel, and dropped-student event analytics
- `V5__analytics_support.sql` indexes for operational-record module/status/type lookups and branch/date analytics queries

Reports are now implemented using:
- `generated_reports` for generated report metadata, filters, generated-by user, date range, branch scope, format, status, and future storage pointers
- source tables such as `students`, `teachers`, `users`, and `operational_records` to regenerate report bytes on demand
- `storage_provider=ON_DEMAND` for current synchronous downloads
- `storage_provider=S3` and `storage_key` later for large or persistent report files

Courses, batches, and enrolments are now implemented using:
- `subjects` as a global master catalogue
- `courses` for academic offerings such as SSC/CBSE/ICSE/HSC standards
- `subject_groups` as UI helper bundles only; they pre-fill subjects but do not lock the final subject selection
- `batches` as branch-specific running instances of a course
- `student_enrolments` and `enrolment_subjects` for the actual student batch and subject selection
- `agreed_total_fee` and `enrolment_instalments` for manually entered fee amounts and payment schedules
- no backend fee-structure reference table and no automatic fee calculation from course, batch, or subject group
- no seed/model for the brochure's `School-Level Competitive Examination` section

Lead management is now implemented using:
- `lead_inquiries` for student, parent/guardian, source, follow-up, and counselor recommendation fields
- optional `course_id`, `batch_id`, and `lead_subjects` links so super admin/admin can select catalogue items during inquiry
- manual admission/enrolment amount entry during conversion through `agreedTotalFee`
- `lead_follow_ups` for follow-up history and status transitions
- `operational_records` with module `LEAD` for dashboard and analytics funnel integration

Branch management is now implemented using:
- the existing `branches` table from the foundation migration
- branch-level aggregates from `students`, `teachers`, and branch-scoped `ADMIN` users
- `operational_records` for create, update, activate, deactivate, and delete branch activity entries

Recommended use of `operational_records` for the super admin phase:
- quick dashboard metrics
- fee collection events
- attendance events
- lead and admission funnel tracking
- test activities
- feedback counts
- recent activity feed

Recommended future dedicated tables for cleaner module APIs:
- leads
- admissions
- admin_profiles
- classes or batches
- tests
- test_questions
- syllabus
- stationery_items
- stationery_transactions
- timesheets
- teacher_payouts
- settings tables by module

## 26. Frontend Integration Sequence

Frontend should integrate the super admin phase in this order:

1. bootstrap super admin
2. login
3. `/api/auth/me`
4. dashboard
5. shared branch filter endpoint
6. module list pages
7. create forms
8. detail drawers
9. status update actions
10. exports and settings tabs

## 27. Final Summary

The project is now centered on a non-tenant architecture with one independent super admin bootstrap flow.

The currently implemented APIs are:
- bootstrap super admin
- login
- refresh
- logout
- logout all
- change password
- current user
- dashboard summary
- branch options
- branch list and summary
- branch detail
- create branch
- update branch
- change branch status
- delete branch
- admin list
- admin detail
- create admin
- update admin
- change admin status
- delete admin

The remaining APIs in this document are the approved super-admin-phase contract and should be implemented next, strictly following the designs and without expanding into non-visible features yet.


---

## 4.6 Courses, Batches and Student Enrolment APIs

> Status: `IMPLEMENTED`
> These APIs implement the full academic catalogue â€” subjects, courses, subject groups, batches, and student enrolments with manually entered fees.
> **There is no backend fee calculation.** The admin enters the total fee and instalment amounts manually at enrolment time.

---

### Data Model Overview

```
Subject (master catalogue â€” global list of all subjects)
  â””â”€â”€ SubjectGroup (named bundle of subjects per course, used as a UI helper)
        â””â”€â”€ Course (academic offering: 8th CBSE, 11-12 HSC, etc.)

Branch
  â””â”€â”€ Batch (running instance of a Course at a Branch for a given academic year)
        â””â”€â”€ StudentEnrolment (student enrolled in a batch)
              â”œâ”€â”€ subjects[]       (actual subjects the student takes â€” admin picks freely)
              â”œâ”€â”€ agreedTotalFee   (entered manually by admin â€” no calculation)
              â””â”€â”€ EnrolmentInstalment[] (payment schedule â€” entered manually by admin)
```

### Design Principles

- **No backend fee calculation.** The admin discusses the fee with the student/parent and enters the agreed amount directly.
- **Subject groups are UI helpers only.** When the admin selects a group (e.g. "Vyasa"), the frontend pre-fills the subject checkboxes. The admin can then freely add or remove subjects before saving. The `subjectGroupId` on the enrolment just records which group was used as a starting point.
- **Instalment schedule is fully manual.** The admin enters each instalment label, amount, due date, and whether it is a post-dated cheque.
- **School-level competitive exam brochure content is excluded.** Do not seed or expose the separate "School-Level Competitive Examination" section unless a future screen explicitly requires it.

---

### 4.6.1 Subject APIs

#### API: List All Subjects

- Status: `IMPLEMENTED`
- Purpose: Populate subject pickers in the enrolment form and other screens
- Endpoint: `GET /api/courses/subjects`
- Auth: Required (`Authorization: Bearer <token>`)
- Roles: Any authenticated user

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "code": "MATHS",
      "displayName": "Mathematics",
      "shortName": "Maths",
      "description": null,
      "sortOrder": 1,
      "isActive": true
    },
    {
      "id": "...",
      "code": "SCIENCE",
      "displayName": "Science",
      "shortName": "Science",
      "description": null,
      "sortOrder": 2,
      "isActive": true
    }
  ]
}
```

**Response field descriptions:**

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Subject's unique identifier â€” use this as `subjectIds[]` in the enrolment request |
| `code` | String (enum) | Canonical code â€” one of: `MATHS`, `SCIENCE`, `ENGLISH`, `LANGUAGE`, `SST`, `HINDI`, `MARATHI`, `SANSKRIT`, `GERMAN`, `HISTORY`, `GEOGRAPHY`, `CIVICS`, `PHYSICS`, `CHEMISTRY`, `BIOLOGY`, `MATHS_ADVANCED`, `ECONOMICS`, `ACCOUNTS`, `BUSINESS_STUDIES`, `LITERATURE`, `COMP_APP`, `OTHER` |
| `displayName` | String | Human-readable label shown in the UI |
| `shortName` | String | Abbreviated label for compact displays |
| `description` | String or null | Optional description |
| `sortOrder` | Integer | Display order (ascending) |
| `isActive` | Boolean | Only active subjects are returned by this endpoint |

- Where to use: Subject selection checkboxes in the enrolment form, timetable, homework, tests

---

### 4.6.2 Course APIs

#### API: List All Courses

- Status: `IMPLEMENTED`
- Purpose: Populate the course selector when creating a batch or enrolling a student
- Endpoint: `GET /api/courses`
- Auth: Required
- Roles: Any authenticated user

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `board` | String (enum) | No | Filter by board: `SSC`, `CBSE`, `ICSE` |
| `category` | String (enum) | No | Filter by category: `BOARD_REGULAR`, `FOUNDATION`, `BOARD_SENIOR`, `COMPETITIVE`, `COMBINED` |

If neither `board` nor `category` is provided, all active courses are returned.

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": "a0000001-0000-0000-0000-000000000004",
      "name": "Std. 8th CBSE Batch",
      "code": "CBSE-8",
      "category": "BOARD_REGULAR",
      "board": "CBSE",
      "standard": "8",
      "academicYear": null,
      "description": null,
      "isActive": true,
      "sortOrder": 40,
      "subjectGroups": null
    }
  ]
}
```

**Response field descriptions:**

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Course identifier â€” use this as `courseId` when creating a batch |
| `name` | String | Display name, e.g. "Std. 8th CBSE Batch" |
| `code` | String | Short code, e.g. `CBSE-8`, `SSC-10`, `HSC-11-12` |
| `category` | String (enum) | `BOARD_REGULAR` = SSC/CBSE/ICSE 8-10, `BOARD_SENIOR` = HSC/CBSE 11-12, `FOUNDATION` = junior programme, `COMPETITIVE` = JEE/NEET only, `COMBINED` = board + competitive |
| `board` | String or null | `SSC`, `CBSE`, `ICSE`, or null for combined/competitive courses |
| `standard` | String | Grade level: `"8"`, `"9"`, `"10"`, `"11-12"`, etc. |
| `academicYear` | String or null | If set, this course is specific to one academic year; null means it repeats every year |
| `sortOrder` | Integer | Display order |
| `subjectGroups` | null | Not included in list view â€” use the detail endpoint to get subject groups |

- Where to use: Batch creation form (step 1: pick a course), student enrolment form

---

#### API: Get Course Detail (with Subject Groups)

- Status: `IMPLEMENTED`
- Purpose: Load the subject groups and their subjects for a selected course â€” used to pre-fill the subject picker in the enrolment form
- Endpoint: `GET /api/courses/{courseId}`
- Auth: Required
- Roles: Any authenticated user

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `courseId` | UUID | The course ID from the list endpoint |

**Response:**

```json
{
  "success": true,
  "data": {
    "id": "a0000001-0000-0000-0000-000000000004",
    "name": "Std. 8th CBSE Batch",
    "code": "CBSE-8",
    "category": "BOARD_REGULAR",
    "board": "CBSE",
    "standard": "8",
    "academicYear": null,
    "description": null,
    "isActive": true,
    "sortOrder": 40,
    "subjectGroups": [
      {
        "id": "b0000001-0000-0000-0000-000000000001",
        "name": "Chanakya - All Subjects",
        "shortName": "Chanakya",
        "subjectCount": 5,
        "isExtraSubjectAllowed": false,
        "maxExtraSubjects": 0,
        "subjects": [
          { "id": "uuid", "code": "MATHS",    "displayName": "Mathematics",              "shortName": "Maths",    "sortOrder": 1,  "isActive": true },
          { "id": "uuid", "code": "SCIENCE",  "displayName": "Science",                  "shortName": "Science",  "sortOrder": 2,  "isActive": true },
          { "id": "uuid", "code": "ENGLISH",  "displayName": "English",                  "shortName": "English",  "sortOrder": 3,  "isActive": true },
          { "id": "uuid", "code": "LANGUAGE", "displayName": "Language (Marathi/Hindi)", "shortName": "Language", "sortOrder": 4,  "isActive": true },
          { "id": "uuid", "code": "SST",      "displayName": "Social Studies",           "shortName": "SST",      "sortOrder": 5,  "isActive": true }
        ],
        "allowedExtraSubjects": [],
        "sortOrder": 1,
        "isActive": true
      },
      {
        "id": "b0000001-0000-0000-0000-000000000002",
        "name": "Drona",
        "shortName": "Drona",
        "subjectCount": 4,
        "isExtraSubjectAllowed": false,
        "maxExtraSubjects": 0,
        "subjects": [
          { "id": "uuid", "code": "MATHS",   "displayName": "Mathematics", "shortName": "Maths",   "sortOrder": 1, "isActive": true },
          { "id": "uuid", "code": "SCIENCE", "displayName": "Science",     "shortName": "Science", "sortOrder": 2, "isActive": true },
          { "id": "uuid", "code": "ENGLISH", "displayName": "English",     "shortName": "English", "sortOrder": 3, "isActive": true },
          { "id": "uuid", "code": "SST",     "displayName": "Social Studies", "shortName": "SST",  "sortOrder": 5, "isActive": true }
        ],
        "allowedExtraSubjects": [],
        "sortOrder": 2,
        "isActive": true
      },
      {
        "id": "b0000001-0000-0000-0000-000000000003",
        "name": "Vyasa",
        "shortName": "Vyasa",
        "subjectCount": 3,
        "isExtraSubjectAllowed": true,
        "maxExtraSubjects": 2,
        "subjects": [
          { "id": "uuid", "code": "MATHS",   "displayName": "Mathematics", "shortName": "Maths",   "sortOrder": 1, "isActive": true },
          { "id": "uuid", "code": "SCIENCE", "displayName": "Science",     "shortName": "Science", "sortOrder": 2, "isActive": true },
          { "id": "uuid", "code": "ENGLISH", "displayName": "English",     "shortName": "English", "sortOrder": 3, "isActive": true }
        ],
        "allowedExtraSubjects": [
          { "id": "uuid", "code": "LANGUAGE", "displayName": "Language (Marathi/Hindi)", "shortName": "Language", "sortOrder": 4, "isActive": true },
          { "id": "uuid", "code": "SST",      "displayName": "Social Studies",           "shortName": "SST",      "sortOrder": 5, "isActive": true }
        ],
        "sortOrder": 3,
        "isActive": true
      },
      {
        "id": "b0000001-0000-0000-0000-000000000004",
        "name": "Arjuna",
        "shortName": "Arjuna",
        "subjectCount": 2,
        "isExtraSubjectAllowed": true,
        "maxExtraSubjects": 3,
        "subjects": [
          { "id": "uuid", "code": "MATHS",   "displayName": "Mathematics", "shortName": "Maths",   "sortOrder": 1, "isActive": true },
          { "id": "uuid", "code": "SCIENCE", "displayName": "Science",     "shortName": "Science", "sortOrder": 2, "isActive": true }
        ],
        "allowedExtraSubjects": [
          { "id": "uuid", "code": "ENGLISH",  "displayName": "English",                  "shortName": "English",  "sortOrder": 3, "isActive": true },
          { "id": "uuid", "code": "LANGUAGE", "displayName": "Language (Marathi/Hindi)", "shortName": "Language", "sortOrder": 4, "isActive": true },
          { "id": "uuid", "code": "SST",      "displayName": "Social Studies",           "shortName": "SST",      "sortOrder": 5, "isActive": true }
        ],
        "sortOrder": 4,
        "isActive": true
      }
    ]
  }
}
```

**SubjectGroup field descriptions:**

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Subject group identifier â€” pass as `subjectGroupId` in the enrolment request (optional) |
| `name` | String | Display name of the group, e.g. "Chanakya - All Subjects", "Vyasa" |
| `shortName` | String | Short label for compact display |
| `subjectCount` | Integer | Canonical number of subjects in this group (informational) |
| `isExtraSubjectAllowed` | Boolean | If `true`, the admin can add extra subjects beyond the group's default list |
| `maxExtraSubjects` | Integer | Maximum number of extra subjects allowed (0 = unlimited). Only relevant when `isExtraSubjectAllowed = true` |
| `subjects` | Array | Default subjects included in this group â€” pre-fill the subject checkboxes with these |
| `allowedExtraSubjects` | Array | Subjects that can be added as extras. If empty, any active subject can be added |
| `sortOrder` | Integer | Display order within the course |

**How to use subject groups in the enrolment form:**

1. Show the groups as radio buttons or a dropdown (e.g. "Chanakya", "Drona", "Vyasa", "Arjuna")
2. When the admin selects a group, pre-fill the subject checkboxes with `subjects[]`
3. If `isExtraSubjectAllowed = true`, show an "Add subject" picker populated from `allowedExtraSubjects[]` (or all subjects if empty)
4. The admin can freely check/uncheck any subject before saving
5. The final checked subjects become `subjectIds[]` in the enrolment request

- Where to use: Student enrolment form (step: select subjects), batch creation context

---

### 4.6.3 Batch APIs

A batch is a running instance of a course at a specific branch for a given academic year and timing slot. Students are enrolled into batches, not directly into courses.

#### API: Create Batch

- Status: `IMPLEMENTED`
- Purpose: Create a new batch for a branch and course
- Endpoint: `POST /api/batches`
- Auth: Required
- Roles: `SUPER_ADMIN`, `ADMIN`

**Request Body:**

```json
{
  "branchId": "branch-uuid",
  "courseId": "a0000001-0000-0000-0000-000000000004",
  "name": "8th CBSE Evening 2026-27",
  "academicYear": "2026-27",
  "batchType": "CHANAKYA",
  "timing": "EVENING",
  "timingLabel": null,
  "startTime": "16:30:00",
  "endTime": "19:30:00",
  "daysOfWeek": "MON,TUE,WED,THU,FRI,SAT",
  "startDate": "2026-03-25",
  "endDate": null,
  "maxStudents": 40,
  "classTeacherId": "teacher-uuid",
  "room": "Room 101"
}
```

**Request field descriptions:**

| Field | Type | Required | Description |
|---|---|---|---|
| `branchId` | UUID | Yes | The branch this batch belongs to |
| `courseId` | UUID | Yes | The course this batch runs (from `GET /api/courses`) |
| `name` | String | No | Custom display name. If blank, auto-generated as `{courseName} â€“ {timing} â€“ {academicYear} ({branchName})` |
| `academicYear` | String | Yes | Academic year in format `YYYY-YY`, e.g. `"2026-27"` |
| `batchType` | String (enum) | No | Tier label: `CHANAKYA`, `DRONA`, `VYASA`, `ARJUNA`, `TIER_E`. Optional â€” can be set later |
| `timing` | String (enum) | Yes | `MORNING`, `EVENING`, or `CUSTOM` |
| `timingLabel` | String | Conditional | Required when `timing = CUSTOM`. Free-text label, e.g. `"04:30 PM â€“ 07:30 PM"` |
| `startTime` | Time (`HH:mm:ss`) | No | Batch start time |
| `endTime` | Time (`HH:mm:ss`) | No | Batch end time |
| `daysOfWeek` | String | No | Comma-separated days: `"MON,TUE,WED,THU,FRI,SAT"` |
| `startDate` | Date (`YYYY-MM-DD`) | No | Date the batch starts |
| `endDate` | Date (`YYYY-MM-DD`) | No | Date the batch ends (null = ongoing) |
| `maxStudents` | Integer | No | Maximum students allowed. `0` = unlimited. Defaults to `0` |
| `classTeacherId` | UUID | No | Teacher assigned as class teacher for this batch |
| `room` | String | No | Room or classroom label |

**Response:**

```json
{
  "success": true,
  "message": "Batch created successfully",
  "data": {
    "id": "batch-uuid",
    "branchId": "branch-uuid",
    "branchName": "Main Branch",
    "courseId": "a0000001-0000-0000-0000-000000000004",
    "courseName": "Std. 8th CBSE Batch",
    "courseCode": "CBSE-8",
    "name": "8th CBSE Evening 2026-27",
    "academicYear": "2026-27",
    "batchType": "CHANAKYA",
    "timing": "EVENING",
    "timingLabel": null,
    "startTime": "16:30:00",
    "endTime": "19:30:00",
    "daysOfWeek": "MON,TUE,WED,THU,FRI,SAT",
    "startDate": "2026-03-25",
    "endDate": null,
    "maxStudents": 40,
    "enrolledCount": 0,
    "classTeacherId": "teacher-uuid",
    "classTeacherName": "Rahul Sharma",
    "room": "Room 101",
    "isActive": true
  }
}
```

**Response field descriptions:**

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Batch identifier â€” use as `batchId` in enrolment requests |
| `branchId` / `branchName` | UUID / String | Branch this batch belongs to |
| `courseId` / `courseName` / `courseCode` | UUID / String / String | Course details |
| `name` | String | Display name of the batch |
| `academicYear` | String | e.g. `"2026-27"` |
| `batchType` | String or null | Tier label if set |
| `timing` | String | `MORNING`, `EVENING`, or `CUSTOM` |
| `timingLabel` | String or null | Custom timing description (only when `timing = CUSTOM`) |
| `startTime` / `endTime` | Time or null | Batch time slot |
| `daysOfWeek` | String or null | Comma-separated days |
| `startDate` / `endDate` | Date or null | Batch date range |
| `maxStudents` | Integer | Capacity limit (`0` = unlimited) |
| `enrolledCount` | Integer | Current number of active enrolments â€” updated automatically when students are enrolled or withdrawn |
| `classTeacherId` / `classTeacherName` | UUID / String or null | Assigned class teacher |
| `room` | String or null | Room label |
| `isActive` | Boolean | Whether the batch is active |

---

#### API: List Batches (Paginated)

- Status: `IMPLEMENTED`
- Purpose: Admin batch management list with pagination
- Endpoint: `GET /api/batches`
- Auth: Required
- Roles: Any authenticated user

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `branchId` | UUID | No | Filter by branch. If omitted, returns batches across all branches |
| `page` | Integer | No | Page number, 0-indexed. Default: `0` |
| `size` | Integer | No | Page size. Default: `20` |

**Response:** Standard paginated envelope with `data.content[]` containing batch objects (same shape as create response).

- Where to use: Batch management screen

---

#### API: List Active Batches by Branch (for Dropdowns)

- Status: `IMPLEMENTED`
- Purpose: Populate batch dropdowns in enrolment, attendance, timetable, homework, and test screens
- Endpoint: `GET /api/batches/by-branch/{branchId}`
- Auth: Required
- Roles: Any authenticated user

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `branchId` | UUID | The branch to list batches for |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `academicYear` | String | No | Filter by academic year, e.g. `"2026-27"`. If omitted, returns all active batches for the branch |

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": "batch-uuid",
      "branchId": "branch-uuid",
      "branchName": "Main Branch",
      "courseId": "course-uuid",
      "courseName": "Std. 8th CBSE Batch",
      "courseCode": "CBSE-8",
      "name": "8th CBSE Evening 2026-27",
      "academicYear": "2026-27",
      "batchType": "CHANAKYA",
      "timing": "EVENING",
      "timingLabel": null,
      "startTime": "16:30:00",
      "endTime": "19:30:00",
      "daysOfWeek": "MON,TUE,WED,THU,FRI,SAT",
      "startDate": "2026-03-25",
      "endDate": null,
      "maxStudents": 40,
      "enrolledCount": 12,
      "classTeacherId": "teacher-uuid",
      "classTeacherName": "Rahul Sharma",
      "room": "Room 101",
      "isActive": true
    }
  ]
}
```

- Where to use: Any dropdown that needs a list of batches â€” student enrolment form, attendance marking, timetable, homework, tests

---

#### API: Get Batch Details

- Status: `IMPLEMENTED`
- Purpose: Fetch full details of a single batch
- Endpoint: `GET /api/batches/{batchId}`
- Auth: Required
- Roles: Any authenticated user

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `batchId` | UUID | Batch identifier |

**Response:** Single batch object (same shape as create response).

---

#### API: Update Batch

- Status: `IMPLEMENTED`
- Purpose: Edit batch details â€” all fields are optional, only non-null fields are applied
- Endpoint: `PUT /api/batches/{batchId}`
- Auth: Required
- Roles: `SUPER_ADMIN`, `ADMIN`

**Request Body (all fields optional):**

```json
{
  "name": "8th CBSE Evening 2026-27 (Updated)",
  "batchType": "DRONA",
  "timing": "CUSTOM",
  "timingLabel": "04:30 PM â€“ 07:30 PM",
  "startTime": "16:30:00",
  "endTime": "19:30:00",
  "daysOfWeek": "MON,WED,FRI,SAT",
  "startDate": "2026-04-01",
  "endDate": "2027-03-31",
  "maxStudents": 35,
  "classTeacherId": "another-teacher-uuid",
  "room": "Room 202",
  "isActive": true
}
```

**Response:** Updated batch object.

---

#### API: Delete Batch

- Status: `IMPLEMENTED`
- Purpose: Soft-delete a batch (marks as deleted, does not remove from database)
- Endpoint: `DELETE /api/batches/{batchId}`
- Auth: Required
- Roles: `SUPER_ADMIN`, `ADMIN`

**Response:**

```json
{
  "success": true,
  "message": "Batch deleted",
  "data": null
}
```

---

### 4.6.4 Student Enrolment APIs

An enrolment records a student's participation in a batch for a specific academic year, including which subjects they are taking and the fee agreed with the admin. All fee amounts are entered manually â€” there is no backend calculation.

#### API: Create Enrolment

- Status: `IMPLEMENTED`
- Purpose: Enrol a student into a batch with manually entered fee and instalment schedule
- Endpoint: `POST /api/enrolments`
- Auth: Required
- Roles: `SUPER_ADMIN`, `ADMIN`

**Request Body:**

```json
{
  "studentId": "student-uuid",
  "batchId": "batch-uuid",
  "subjectGroupId": "b0000001-0000-0000-0000-000000000003",
  "subjectIds": [
    "subject-uuid-maths",
    "subject-uuid-science",
    "subject-uuid-english"
  ],
  "agreedTotalFee": 38000,
  "paymentPlan": "INSTALMENT_3",
  "enrolmentDate": "2026-03-25",
  "notes": "Sibling discount applied",
  "instalments": [
    {
      "instalmentNumber": 1,
      "label": "1st Instalment at time of Admission",
      "amount": 21000,
      "dueDate": null,
      "isPostDatedCheque": false
    },
    {
      "instalmentNumber": 2,
      "label": "2nd Instalment on or before 15th July",
      "amount": 8500,
      "dueDate": "2026-07-15",
      "isPostDatedCheque": true
    },
    {
      "instalmentNumber": 3,
      "label": "3rd Instalment on or before 15th September",
      "amount": 8500,
      "dueDate": "2026-09-15",
      "isPostDatedCheque": true
    }
  ]
}
```

**Request field descriptions:**

| Field | Type | Required | Description |
|---|---|---|---|
| `studentId` | UUID | Yes | The student to enrol |
| `batchId` | UUID | Yes | The batch to enrol the student into |
| `subjectGroupId` | UUID | No | Optional â€” the subject group used as a starting point. Purely informational. Pass the `id` from `GET /api/courses/{courseId}` subject groups |
| `subjectIds` | UUID[] | Yes (min 1) | The actual subjects the student is enrolled for. Use IDs from `GET /api/courses/subjects` or from the subject group's `subjects[]` array |
| `agreedTotalFee` | Decimal | Yes | Total fee agreed with the student/parent. Entered manually. Must be >= 0 |
| `paymentPlan` | String (enum) | Yes | `REGULAR` = single payment, `LUMPSUM` = upfront lump sum, `INSTALMENT_2` = two instalments, `INSTALMENT_3` = three instalments |
| `enrolmentDate` | Date (`YYYY-MM-DD`) | Yes | Date the student was enrolled |
| `notes` | String | No | Internal notes, e.g. "Sibling discount applied" |
| `instalments` | Array | No | Admin-entered payment schedule. Can be empty for `REGULAR` plan |
| `instalments[].instalmentNumber` | Integer | Yes (per row) | Sequential number: 1, 2, 3 |
| `instalments[].label` | String | Yes (per row) | Description shown on receipts |
| `instalments[].amount` | Decimal | Yes (per row) | Amount for this instalment. Must be >= 0 |
| `instalments[].dueDate` | Date or null | No | Due date. Null = due immediately (e.g. at admission) |
| `instalments[].isPostDatedCheque` | Boolean | No | `true` if a post-dated cheque was collected. Default: `false` |

**Error responses:**

| HTTP Status | Error Code | When |
|---|---|---|
| `422` | `DUPLICATE_ENROLMENT` | Student already has an active enrolment in the same batch |
| `404` | â€” | Student, batch, subject group, or any subject ID not found |
| `400` | â€” | Validation error (missing required fields, negative fee, etc.) |

**Response:**

```json
{
  "success": true,
  "message": "Student enrolled successfully",
  "data": {
    "id": "enrolment-uuid",
    "studentId": "student-uuid",
    "studentName": "Aarav Singh",
    "studentLoginId": "STU-A3X9KL",
    "batchId": "batch-uuid",
    "batchName": "8th CBSE Evening 2026-27",
    "academicYear": "2026-27",
    "courseId": "course-uuid",
    "courseName": "Std. 8th CBSE Batch",
    "courseCode": "CBSE-8",
    "subjectGroupId": "b0000001-0000-0000-0000-000000000003",
    "subjectGroupName": "Vyasa",
    "subjects": [
      { "id": "uuid", "code": "MATHS",   "displayName": "Mathematics", "shortName": "Maths",   "sortOrder": 1, "isActive": true },
      { "id": "uuid", "code": "SCIENCE", "displayName": "Science",     "shortName": "Science", "sortOrder": 2, "isActive": true },
      { "id": "uuid", "code": "ENGLISH", "displayName": "English",     "shortName": "English", "sortOrder": 3, "isActive": true }
    ],
    "agreedTotalFee": 38000,
    "paymentPlan": "INSTALMENT_3",
    "totalPaid": 0.00,
    "totalPending": 38000.00,
    "instalments": [
      {
        "id": "inst-uuid-1",
        "instalmentNumber": 1,
        "label": "1st Instalment at time of Admission",
        "amount": 21000,
        "dueDate": null,
        "isPostDatedCheque": false,
        "isPaid": false,
        "paidDate": null
      },
      {
        "id": "inst-uuid-2",
        "instalmentNumber": 2,
        "label": "2nd Instalment on or before 15th July",
        "amount": 8500,
        "dueDate": "2026-07-15",
        "isPostDatedCheque": true,
        "isPaid": false,
        "paidDate": null
      },
      {
        "id": "inst-uuid-3",
        "instalmentNumber": 3,
        "label": "3rd Instalment on or before 15th September",
        "amount": 8500,
        "dueDate": "2026-09-15",
        "isPostDatedCheque": true,
        "isPaid": false,
        "paidDate": null
      }
    ],
    "enrolmentDate": "2026-03-25",
    "status": "ACTIVE",
    "notes": "Sibling discount applied"
  }
}
```

**Response field descriptions:**

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Enrolment identifier |
| `studentId` / `studentName` / `studentLoginId` | â€” | Student details |
| `batchId` / `batchName` / `academicYear` | â€” | Batch details |
| `courseId` / `courseName` / `courseCode` | â€” | Course details (derived from the batch) |
| `subjectGroupId` / `subjectGroupName` | UUID / String or null | The group used as a starting point, if any |
| `subjects` | Array | The actual subjects the student is enrolled for |
| `agreedTotalFee` | Decimal | Total fee as entered by the admin |
| `paymentPlan` | String | Payment plan chosen |
| `totalPaid` | Decimal | Sum of all instalments where `isPaid = true` |
| `totalPending` | Decimal | `agreedTotalFee - totalPaid` |
| `instalments[].isPaid` | Boolean | Updated by the fee collection module when payment is recorded |
| `instalments[].paidDate` | Date or null | Date payment was recorded |
| `status` | String | `ACTIVE`, `COMPLETED`, `WITHDRAWN`, `TRANSFERRED`, `SUSPENDED` |

---

#### API: Get Enrolment

- Status: `IMPLEMENTED`
- Endpoint: `GET /api/enrolments/{enrolmentId}`
- Auth: Required
- Roles: Any authenticated user
- Response: Full enrolment object (same shape as create response)

---

#### API: Get Enrolments by Student

- Status: `IMPLEMENTED`
- Purpose: Show all batches a student is or was enrolled in
- Endpoint: `GET /api/enrolments/student/{studentId}`
- Auth: Required
- Roles: Any authenticated user
- Response: Array of enrolment objects ordered by `enrolmentDate` descending
- Where to use: Student profile page (fee tab, academic history), parent portal

---

#### API: Get Enrolments by Batch

- Status: `IMPLEMENTED`
- Purpose: List all students enrolled in a batch with their fee status
- Endpoint: `GET /api/enrolments/batch/{batchId}?page=0&size=20`
- Auth: Required
- Roles: Any authenticated user
- Response: Paginated list of enrolment objects ordered by student name
- Where to use: Batch detail page, attendance marking, fee collection

---

#### API: Get Enrolments by Branch and Year

- Status: `IMPLEMENTED`
- Purpose: Admin overview of all enrolments for a branch in an academic year
- Endpoint: `GET /api/enrolments/branch/{branchId}?academicYear=2026-27&page=0&size=20`
- Auth: Required
- Roles: `SUPER_ADMIN`, `ADMIN`
- Response: Paginated list of enrolment objects ordered by student name

---

#### API: Update Enrolment

- Status: `IMPLEMENTED`
- Purpose: Edit subjects, fee, payment plan, instalment schedule, or status
- Endpoint: `PUT /api/enrolments/{enrolmentId}`
- Auth: Required
- Roles: `SUPER_ADMIN`, `ADMIN`

**Request Body (all fields optional â€” only non-null fields are applied):**

```json
{
  "subjectGroupId": "b0000001-0000-0000-0000-000000000001",
  "subjectIds": ["subject-uuid-maths", "subject-uuid-science", "subject-uuid-english", "subject-uuid-sst"],
  "agreedTotalFee": 42000,
  "paymentPlan": "INSTALMENT_3",
  "enrolmentDate": "2026-03-25",
  "instalments": [
    { "instalmentNumber": 1, "label": "At Admission", "amount": 25000, "dueDate": null,         "isPostDatedCheque": false },
    { "instalmentNumber": 2, "label": "By 15th July", "amount": 8500,  "dueDate": "2026-07-15", "isPostDatedCheque": true  },
    { "instalmentNumber": 3, "label": "By 15th Sep",  "amount": 8500,  "dueDate": "2026-09-15", "isPostDatedCheque": true  }
  ],
  "status": "ACTIVE",
  "notes": "Extra subject SST added"
}
```

**Update notes:**

| Field | Behaviour |
|---|---|
| `subjectIds` | Replaces the entire subject list |
| `instalments` | Replaces the entire instalment schedule |
| `status` | Changing status automatically updates `batch.enrolledCount` |
| All other fields | Applied only if non-null in the request |

**Response:** Updated enrolment object.

---

#### API: Delete Enrolment

- Status: `IMPLEMENTED`
- Purpose: Soft-delete an enrolment (updates batch enrolled count automatically)
- Endpoint: `DELETE /api/enrolments/{enrolmentId}`
- Auth: Required
- Roles: `SUPER_ADMIN`, `ADMIN`
- Response: `{ "success": true, "message": "Enrolment deleted", "data": null }`

---

### 4.6.5 Subject Selection Rules

The subject selection is fully flexible. The admin can use a subject group as a starting point or ignore groups entirely.

| Scenario | What to send |
|---|---|
| Pick a named group (e.g. Chanakya â€” all 5 subjects) | `subjectGroupId` = group ID, `subjectIds` = all 5 subject IDs from the group |
| Pick a group and add an extra subject | `subjectGroupId` = group ID, `subjectIds` = group subjects + extra subject ID |
| Pick subjects freely without any group | `subjectGroupId` = null, `subjectIds` = any subject IDs |
| Single subject only (e.g. Maths only) | `subjectGroupId` = null, `subjectIds` = [maths-uuid] |
| Change subjects later | `PUT /api/enrolments/{id}` with new `subjectIds` |

The `subjectGroupId` is purely informational â€” it records which group the admin started from. The `subjectIds` array is the authoritative record of what the student is enrolled for.

---

### 4.6.6 Seeded Master Data

The following data is pre-seeded in `V7__courses_batches_enrolments.sql` and is available immediately after the first application startup.

**Courses:**

| Code | Name | Board | Standard | Category |
|---|---|---|---|---|
| `SSC-8` | Std. 8th SSC Batch | SSC | 8 | BOARD_REGULAR |
| `SSC-9` | Std. 9th SSC Batch | SSC | 9 | BOARD_REGULAR |
| `SSC-10` | Std. 10th SSC Batch | SSC | 10 | BOARD_REGULAR |
| `CBSE-8` | Std. 8th CBSE Batch | CBSE | 8 | BOARD_REGULAR |
| `CBSE-9` | Std. 9th CBSE Batch | CBSE | 9 | BOARD_REGULAR |
| `CBSE-10` | Std. 10th CBSE Batch | CBSE | 10 | BOARD_REGULAR |
| `ICSE-8` | Std. 8th ICSE Batch | ICSE | 8 | BOARD_REGULAR |
| `ICSE-9` | Std. 9th ICSE Batch | ICSE | 9 | BOARD_REGULAR |
| `ICSE-10` | Std. 10th ICSE Batch | ICSE | 10 | BOARD_REGULAR |
| `HSC-11-12` | Std. 11th-12th HSC | â€” | 11-12 | BOARD_SENIOR |

**Subject Groups (seeded for SSC-8 as an example â€” same pattern applies to other courses):**

| Group | Default Subjects | Extra Subjects Allowed | Max Extras |
|---|---|---|---|
| Chanakya | Maths, Science, English, Language, SST | No | 0 |
| Drona | Maths, Science, English, SST | No | 0 |
| Vyasa | Maths, Science, English | Yes (Language, SST) | 2 |
| Arjuna | Maths, Science | Yes (English, Language, SST) | 3 |

**Subjects (22 subjects in master catalogue):**

`MATHS`, `SCIENCE`, `ENGLISH`, `LANGUAGE`, `SST`, `HINDI`, `MARATHI`, `SANSKRIT`, `GERMAN`, `HISTORY`, `GEOGRAPHY`, `CIVICS`, `PHYSICS`, `CHEMISTRY`, `BIOLOGY`, `MATHS_ADVANCED`, `ECONOMICS`, `ACCOUNTS`, `BUSINESS_STUDIES`, `LITERATURE`, `COMP_APP`, `OTHER`

---

### 4.6.7 Frontend Integration Guide

#### Batch Creation Flow

```
1. GET /api/courses                              â†’ populate course dropdown
2. POST /api/batches                             â†’ create batch (branch + course + timing + year)
3. GET /api/batches/by-branch/{branchId}         â†’ refresh batch dropdowns in other modules
```

#### Student Enrolment Flow

```
1. GET /api/batches/by-branch/{branchId}?academicYear=2026-27
   â†’ populate batch dropdown

2. GET /api/courses/{courseId}
   â†’ load subject groups for the course linked to the selected batch
   â†’ show groups as radio buttons or a dropdown

3. Admin selects a subject group (optional)
   â†’ pre-fill subject checkboxes from subjectGroup.subjects[]
   â†’ if isExtraSubjectAllowed = true, show "Add subject" picker
     from subjectGroup.allowedExtraSubjects[] (or all subjects if empty)

4. Admin freely checks/unchecks subjects
   â†’ collect final checked subject IDs as subjectIds[]

5. Admin enters:
   â†’ agreedTotalFee  (total fee agreed â€” no calculation)
   â†’ paymentPlan     (REGULAR / LUMPSUM / INSTALMENT_2 / INSTALMENT_3)
   â†’ instalments[]   (each row: label, amount, dueDate, isPostDatedCheque)

6. POST /api/enrolments
   â†’ save the enrolment
```

#### Batch Student List (for Attendance / Fee Collection)

```
GET /api/enrolments/batch/{batchId}?page=0&size=50
â†’ returns all students in the batch with subjects, fee status (totalPaid, totalPending),
  and instalment schedule
```

#### Student Fee Overview (Student Profile Page)

```
GET /api/enrolments/student/{studentId}
â†’ returns all enrolments for the student across all batches and years
â†’ each enrolment includes agreedTotalFee, totalPaid, totalPending, and instalments[]
```

#### End-to-End API Test Flow

Use this sequence in Postman/Thunder Client after logging in as `SUPER_ADMIN`. Replace every placeholder UUID with the ID returned by the previous API.

1. Login and store token.

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "loginId": "superadmin",
  "password": "Password@123"
}
```

Use `Authorization: Bearer {{accessToken}}` for every request below.

2. Load subject catalogue.

```http
GET /api/courses/subjects
Authorization: Bearer {{accessToken}}
```

Save subject IDs as `{{mathSubjectId}}` and `{{scienceSubjectId}}`.

3. Load courses and choose one course.

```http
GET /api/courses?board=CBSE&standard=8
Authorization: Bearer {{accessToken}}
```

Save the selected course ID as `{{courseId}}`.

4. Load course detail and subject groups.

```http
GET /api/courses/{{courseId}}
Authorization: Bearer {{accessToken}}
```

Save a subject group ID as `{{subjectGroupId}}`.

5. Create a batch.

```http
POST /api/batches
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "branchId": "{{branchId}}",
  "courseId": "{{courseId}}",
  "name": "8th CBSE Evening 2026-27",
  "academicYear": "2026-27",
  "batchType": "CHANAKYA",
  "timing": "EVENING",
  "startTime": "16:30:00",
  "endTime": "19:30:00",
  "startDate": "2026-03-25",
  "endDate": "2027-03-31",
  "maxStudents": 60,
  "room": "Room 101"
}
```

Save `data.id` as `{{batchId}}`.

6. Verify batches by branch.

```http
GET /api/batches/by-branch/{{branchId}}?academicYear=2026-27
Authorization: Bearer {{accessToken}}
```

7. Create a teacher aligned with the subject catalogue.

```http
POST /api/super-admin/teachers
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "fullName": "Neha Patil",
  "email": "neha.patil+test@institute.com",
  "phone": "9876543210",
  "qualification": "M.Sc. Mathematics",
  "experienceYears": 6,
  "subjectIds": ["{{mathSubjectId}}", "{{scienceSubjectId}}"],
  "specialization": "Algebra",
  "joiningDate": "2026-05-08",
  "employmentType": "FULL_TIME",
  "salaryType": "MONTHLY",
  "hourlyRate": 0,
  "loginId": "TEA_TEST_001",
  "password": "Password@123",
  "confirmPassword": "Password@123",
  "branchId": "{{branchId}}",
  "address": "Pune"
}
```

8. Filter teachers by subject.

```http
GET /api/super-admin/teachers?subjectId={{mathSubjectId}}&page=0&size=10
Authorization: Bearer {{accessToken}}
```

9. Create a direct student admission with batch, subjects, and manual fee.

```http
POST /api/super-admin/students
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "fullName": "Aarav Sharma",
  "studentId": "STU_TEST_001",
  "branchId": "{{branchId}}",
  "standard": "8th",
  "board": "CBSE",
  "batchId": "{{batchId}}",
  "courseId": "{{courseId}}",
  "subjectGroupId": "{{subjectGroupId}}",
  "subjectIds": ["{{mathSubjectId}}", "{{scienceSubjectId}}"],
  "agreedTotalFee": 45000,
  "paymentPlan": "INSTALMENT_3",
  "instalments": [
    { "instalmentNumber": 1, "label": "At Admission", "amount": 25000, "dueDate": "2026-05-08", "isPostDatedCheque": false },
    { "instalmentNumber": 2, "label": "Second Instalment", "amount": 10000, "dueDate": "2026-07-15", "isPostDatedCheque": true },
    { "instalmentNumber": 3, "label": "Third Instalment", "amount": 10000, "dueDate": "2026-09-15", "isPostDatedCheque": true }
  ],
  "gender": "MALE",
  "dateOfBirth": "2012-03-27",
  "mobile": "9876543210",
  "parentName": "Rajesh Sharma",
  "parentPhone": "9876543211",
  "email": "aarav.test@example.com",
  "address": "Pune",
  "schoolName": "Greenfield Public School",
  "admissionDate": "2026-05-08",
  "loginId": "STU_TEST_001",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```

Save `data.id` as `{{studentId}}`.

10. Verify student enrolments.

```http
GET /api/enrolments/student/{{studentId}}
Authorization: Bearer {{accessToken}}
```

11. Create a lead with course, batch, and subjects.

```http
POST /api/super-admin/leads
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "studentName": "Vivaan Patel",
  "gender": "MALE",
  "classInterestedIn": "8th",
  "board": "CBSE",
  "mobileNumber": "9876543220",
  "fatherName": "Amit Patel",
  "fatherMobileNumber": "9876543221",
  "leadSource": "Parent Referral",
  "preferredBranchId": "{{branchId}}",
  "courseId": "{{courseId}}",
  "batchId": "{{batchId}}",
  "subjectIds": ["{{mathSubjectId}}", "{{scienceSubjectId}}"],
  "preferredContactTime": "10:00 AM - 12:00 PM",
  "modeOfContact": "CALL",
  "remarks": "Interested in Maths and Science batch."
}
```

Save `data.id` as `{{leadId}}`.

12. Add follow-up/status on the lead.

```http
PATCH /api/super-admin/leads/{{leadId}}/status
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "status": "IN_FOLLOW_UP",
  "followUpAt": "2026-05-20T10:30:00",
  "remarks": "Parent asked for fee details.",
  "contactedBy": "Admin"
}
```

13. Convert lead to admission with manual fee and optional enrolment.

```http
POST /api/super-admin/leads/{{leadId}}/convert-to-admission
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

```json
{
  "studentId": "STU_TEST_002",
  "loginId": "STU_TEST_002",
  "password": "Password@123",
  "confirmPassword": "Password@123",
  "branchId": "{{branchId}}",
  "admissionDate": "2026-05-16",
  "standard": "8th",
  "batch": "8th CBSE Evening 2026-27",
  "board": "CBSE",
  "batchId": "{{batchId}}",
  "subjectGroupId": "{{subjectGroupId}}",
  "subjectIds": ["{{mathSubjectId}}", "{{scienceSubjectId}}"],
  "agreedTotalFee": 45000,
  "paymentPlan": "INSTALMENT_3",
  "instalments": [
    { "instalmentNumber": 1, "label": "At Admission", "amount": 25000, "dueDate": "2026-05-16", "isPostDatedCheque": false },
    { "instalmentNumber": 2, "label": "Second Instalment", "amount": 10000, "dueDate": "2026-07-15", "isPostDatedCheque": true },
    { "instalmentNumber": 3, "label": "Third Instalment", "amount": 10000, "dueDate": "2026-09-15", "isPostDatedCheque": true }
  ]
}
```

14. Verify batch enrolment list.

```http
GET /api/enrolments/batch/{{batchId}}?page=0&size=20
Authorization: Bearer {{accessToken}}
```

Important rule: course, subject group, and batch selections are catalogue/navigation data. The fee is always manually entered through `agreedTotalFee` and `instalments`; the backend does not configure or calculate fee structures from the brochure.

#### Enrolment Status Values

| Status | Meaning |
|---|---|
| `ACTIVE` | Student is currently enrolled and attending |
| `COMPLETED` | Academic year or course completed |
| `WITHDRAWN` | Student left mid-course |
| `TRANSFERRED` | Moved to another batch or branch |
| `SUSPENDED` | Temporarily suspended |

Changing status via `PUT /api/enrolments/{id}` automatically updates `batch.enrolledCount`.

#### Payment Plan Values

| Value | Meaning |
|---|---|
| `REGULAR` | Single full payment (standard) |
| `LUMPSUM` | Single upfront lump-sum (e.g. with discount) |
| `INSTALMENT_2` | Two-instalment schedule |
| `INSTALMENT_3` | Three-instalment schedule |

