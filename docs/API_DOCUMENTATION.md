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
| `subject` | string | No | Filters by primary subject. |
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
| `subjects` | array[string] | Yes | Indicates teaching subjects and drives subject filters later. |
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
5. Persist subjects in `teacher_subjects`.
6. Return teacher summary payload.

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
| `batch` | string | Yes | Section or batch label shown in table. |
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
4. Mark active and current admission state.
5. Return student summary response.

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

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/leads`
- Auth: `SUPER_ADMIN`
- Purpose: Lists inquiry leads for the lead management screen.

## 13.2 Create Lead

- Status: `PLANNED`
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
| `classInterestedIn` | string | Yes | Needed to route lead to the correct academic offering. |
| `board` | string | Yes | Needed because leads are board-sensitive in design. |
| `medium` | string | Yes | Academic medium filter from form. |
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
| `preferredBranchId` | UUID | Yes | Branch routing for admission ownership. |
| `inquiryFor` | string | Yes | Captures whether inquiry is for admission, counseling, or another case. |
| `expectedAdmissionYear` | string | Yes | Academic cycle target for follow-up. |
| `preferredFollowupDate` | date | No | Follow-up scheduling aid. |
| `preferredContactTime` | string | No | Contact timing preference. |
| `preferredContactModes` | array[string] | No | Call, WhatsApp, email, or SMS preference. |
| `counselorRecommended` | string | No | Counselor assignment detail. |
| `subjectsSuggested` | array[string] | No | Counseling outcome detail from form. |
| `batchSuggested` | string | No | Recommended batch or section. |
| `admissionLikelihood` | string | No | Funnel confidence used in counseling. |
| `remarks` | string | No | Free-text counselor or staff notes. |

### Backend Implementation Steps

1. Validate branch and academic fields.
2. Save lead master row.
3. Save counselor metadata and follow-up preferences.
4. Store source for funnel analytics.
5. Save uploaded file references.
6. Write operational record with module `LEAD`.

## 13.3 Lead Detail

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/leads/{leadId}`

## 13.4 Convert Lead to Admission

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/leads/{leadId}/convert-to-admission`
- Auth: `SUPER_ADMIN`
- Purpose: Starts admission record using an approved lead.

## 13.5 Admission List

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/admissions`
- Auth: `SUPER_ADMIN`
- Purpose: Lists admissions records and filters.

## 13.6 Create Admission

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/admissions`
- Auth: `SUPER_ADMIN`
- Purpose: Saves admission form shown in design.

### Request Payload

The admission payload should reuse student and guardian fields from lead capture, plus:

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `academicSession` | string | Yes | Admission belongs to one academic year. |
| `admissionDate` | date | Yes | Required for admission timeline and reports. |
| `admissionNumber` | string | No | Auto-generated or manually preserved admission identifier. |
| `branchId` | UUID | Yes | Branch mapping for all downstream data. |
| `classOrStandard` | string | Yes | Required academic placement. |
| `board` | string | Yes | Board-specific admission grouping. |
| `medium` | string | Yes | Medium-specific academic placement. |
| `stream` | string | No | Needed where stream applies. |
| `schoolLastAttended` | string | No | Previous school information from form. |
| `lastClassPassed` | string | No | Admission eligibility context. |
| `lastBoard` | string | No | Previous academic board. |
| `passingYear` | integer | No | Prior academic timeline. |
| `subjects` | array[string] | Yes | Selected study subjects from form. |
| `transportRequired` | boolean | No | Operational requirement captured in form. |
| `hostelRequired` | boolean | No | Operational requirement captured in form. |
| `previousTransferCertificateUrl` | string | No | Admission document reference. |

### Backend Implementation Steps

1. Validate academic session and branch.
2. Create or link student record.
3. Mark `is_admission_final = true`.
4. Write admission operational record.
5. Return created admission summary.

## 14. Analytics APIs

## 14.1 Analytics Overview

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/analytics`
- Auth: `SUPER_ADMIN`
- Purpose: Fills analytics screen with cards, tabs, and charts.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `tab` | enum | No | Controls current analytics tab such as overview, students, teachers, attendance, academics, finance, admissions, performance, or feedback. |
| `branchId` | UUID | No | Optional branch filter. |
| `fromDate` | date | No | Lower reporting bound. |
| `toDate` | date | No | Upper reporting bound. |

### Backend Implementation Steps

1. Reuse dashboard aggregations where possible.
2. Add tab-specific metrics only for visible design cards and charts.
3. Return data sectioned by active tab.

## 15. Reports APIs

## 15.1 Reports Summary

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/reports/summary`
- Auth: `SUPER_ADMIN`
- Purpose: Fills report dashboard cards and category tiles.

## 15.2 Reports List

- Status: `PLANNED`
- Endpoint: `GET /api/super-admin/reports`
- Auth: `SUPER_ADMIN`
- Purpose: Returns report rows for the reports table.

### Query Parameters

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `category` | string | No | Filters student, teacher, attendance, academic, admission, or exam reports. |
| `reportType` | string | No | Narrows to specific report type. |
| `fromDate` | date | No | Start date for report filter. |
| `toDate` | date | No | End date for report filter. |
| `page` | integer | No | Table page index. |
| `size` | integer | No | Table page size. |

## 15.3 Export Report

- Status: `PLANNED`
- Endpoint: `POST /api/super-admin/reports/export`
- Auth: `SUPER_ADMIN`
- Purpose: Generates export file for selected report.

### Request Payload

```json
{
  "reportType": "STUDENT_ATTENDANCE_REPORT",
  "format": "PDF",
  "branchId": null,
  "fromDate": "2026-05-01",
  "toDate": "2026-05-31"
}
```

### Payload Field Purpose

| Field | Type | Required | Purpose |
|---|---|---:|---|
| `reportType` | string | Yes | Identifies which dataset should be exported. |
| `format` | string | Yes | Specifies output format such as PDF or Excel. |
| `branchId` | UUID | No | Optional branch-scope export. |
| `fromDate` | date | No | Lower date filter for report generation. |
| `toDate` | date | No | Upper date filter for report generation. |

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
