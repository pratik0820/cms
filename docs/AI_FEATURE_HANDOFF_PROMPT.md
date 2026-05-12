# AI Feature Handoff Prompt for `cms-backend`

Use this prompt when asking another AI assistant (such as Claude) to design or implement a new feature for this project. Replace the placeholder requirement section near the end before sending it.

---

## Prompt

You are helping on an existing Java backend project. Your job is to design and implement a new feature **without breaking or rewriting the existing architecture, conventions, security model, API response format, or database patterns**.

You do **not** have direct access to the repository, so you must work only from the context below and produce a solution that is aligned with the current codebase. Be conservative: extend the project cleanly, reuse existing patterns, and avoid introducing unrelated refactors.

If something is unclear, prefer the existing project conventions described below over inventing a new pattern.

## 1. Project Identity

- Project name: `cms-backend`
- Type: Spring Boot REST backend
- Package root: `com.classmanager.cms_backend`
- Build tool: Maven
- Java version: `21`
- Spring Boot version: `3.2.5`
- Database: PostgreSQL
- Migration tool: Flyway
- Auth model: JWT access token + DB-backed refresh tokens
- Current architecture focus: non-tenant Class Management System backend
- Current implemented phase: mainly `SUPER_ADMIN` phase plus auth foundation

## 2. Tech Stack and Libraries

Main dependencies already used:

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-websocket`
- `spring-boot-starter-actuator`
- `spring-boot-starter-cache`
- `spring-aop`
- `postgresql`
- `flyway-core`
- `jjwt`
- `lombok`
- `mapstruct` is present as a dependency, but current code mostly uses manual mapping/builders
- `caffeine`
- `bucket4j`
- `springdoc-openapi`
- `cloudinary-http44`
- `firebase-admin`
- `aws ses sdk`
- `log4j2`

Do not switch the stack. Extend what is already there.

## 3. Runtime and Config Context

Current key config:

- Server port: `8095`
- Context path: `/cms`
- Base URL locally: `http://localhost:8095/cms`
- Swagger path: `/swagger-ui.html`
- OpenAPI docs path: `/api-docs`
- Default CORS origins include `http://localhost:3000` and `http://localhost:5173`
- Multipart upload enabled with max file size `10MB`
- Cache type: `caffeine`
- JWT config comes from `jwt.*`

Important config conventions:

- `application.yaml` is the main config file
- `.env` values are imported via `spring.config.import`
- Production-sensitive values are parameterized with env variables

## 4. Current Domain Model

The codebase already includes these main entities/tables:

- `users`
- `roles`
- `permissions`
- `user_roles`
- `branches`
- `refresh_tokens`
- `admin_profiles`
- `teachers`
- `teacher_subjects`
- `students`
- `operational_records`

### Core entity patterns

Most entities extend `BaseEntity`, which provides:

- `id : UUID`
- `createdAt`
- `updatedAt`
- `isDeleted`
- `softDelete()`

Soft delete is already a project convention. Do not hard delete unless there is a very strong reason and it matches the existing design.

### Important existing entities

#### `User`

Represents login identity and shared account state:

- `email`
- `loginId`
- `passwordHash`
- `fullName`
- `phone`
- `profilePhotoUrl`
- `branch`
- `isActive`
- `lastLoginAt`
- `fcmToken`
- password reset fields
- failed login / lock fields
- `roles`

Important behavior:

- emails and login IDs are normalized
- login failure count and temporary lock are supported
- role mapping is stored in `user_roles`

#### `Branch`

Simple branch entity with:

- `name`
- `address`
- `city`
- `phone`
- `email`
- `isActive`

#### `AdminProfile`

Admin-specific extension of `User`:

- `dateOfBirth`
- `gender`
- `roleTitle`
- `joiningDate`
- `accessLevel`
- `address`
- `allBranchesAccess`
- `createdByUser`

#### `Teacher`

Teacher-specific extension of `User`:

- linked `user`
- linked `branch`
- `name`, `phone`, `email`
- `dateOfBirth`
- `gender`
- `qualification`
- `experienceYears`
- `subjects` via `teacher_subjects`
- `specialization`
- `joiningDate`
- `employmentType`
- `salaryType`
- `hourlyRate`
- `address`
- `isActive`
- `createdByUser`

#### `Student`

Student-specific extension of `User`:

- linked `user`
- linked `branch`
- `name`
- `studentId`
- `dob`
- `gender`
- `photoUrl`
- `mobile`
- `parentName`
- `parentPhone`
- `email`
- `address`
- `schoolName`
- `standard`
- `batch`
- `board` as enum
- `admissionDate`
- `isAdmissionFinal`
- `isActive`
- `createdByUser`

#### `OperationalRecord`

This is an important lightweight event/log/metrics table used across modules. It stores:

- `module`
- `type`
- `title`
- `description`
- `status`
- `branch`
- `studentId`
- `teacherId`
- `createdByUserId`
- `source`
- `eventDate`
- `startTime`
- `endTime`
- `durationMinutes`
- `amount`
- `paidAmount`
- `pendingAmount`
- `detailsJson`

This table is already being used for:

- dashboard metrics
- activity feeds
- branch/admin/teacher/student lifecycle activities

If the new feature needs auditable activity or lightweight event tracking, prefer reusing `operational_records` unless the feature clearly needs a dedicated table.

## 5. Existing Roles and Authorization

Current roles seeded by migration:

- `SUPER_ADMIN`
- `ADMIN`
- `TEACHER`
- `STUDENT`
- `PARENT`

Current implemented controller security pattern:

- public auth endpoints are explicitly permitted in `SecurityConfig`
- most management controllers use class-level `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- authenticated user info is available through `CmsUserDetails`
- `BaseController` exposes helpers like `currentUserId()` and `currentBranchId()`

Do not weaken security. New endpoints must follow the existing authorization style.

## 6. Authentication Model

Current auth flow already implemented:

- bootstrap super admin
- login
- refresh token rotation
- logout current device
- logout all devices
- change password
- get current user

Implementation details:

- access token is JWT
- refresh tokens are opaque random tokens
- refresh token hashes are stored in DB
- refresh token reuse detection revokes all user sessions
- login can use `email` or `loginId`
- account locking exists after repeated failed login attempts

Do not replace this auth system. Any new secured feature must plug into it.

## 7. Existing API Style

All APIs use a standard wrapper:

```json
{
  "success": true,
  "message": "Optional message",
  "data": {},
  "errorCode": null,
  "timestamp": "2026-05-08T18:30:00"
}
```

Use `ApiResponse<T>` for all controller responses.

Validation and exceptions are centralized in `GlobalExceptionHandler`.

Common exception types already used:

- `BadRequestException`
- `UnauthorizedException`
- `ResourceNotFoundException`
- `ResourceAlreadyExistsException`
- `BusinessRuleException`

Error handling conventions:

- validation errors return `VALIDATION_ERROR`
- auth failures return project-specific auth codes
- forbidden actions return `FORBIDDEN`
- unknown issues return `INTERNAL_ERROR`

Do not invent a new response format.

## 8. Existing Code Organization Pattern

Current package structure is broadly:

- `config`
- `controller`
- `dto.request`
- `dto.response`
- `entity`
- `enums`
- `exception`
- `logging`
- `properties`
- `repository`
- `security`
- `service`
- `util`

Typical feature implementation pattern in this project:

1. request DTO(s)
2. response DTO(s)
3. repository queries
4. service with business logic
5. controller with REST endpoints
6. optional Flyway migration if schema change is needed

Follow this structure for any new feature.

## 9. Existing Implemented Modules

These modules are already implemented and should be treated as existing behavior to preserve:

### Authentication

- `POST /api/auth/bootstrap/super-admin`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `PUT /api/auth/change-password`
- `GET /api/auth/me`

### Super Admin Dashboard

- `GET /api/super-admin/dashboard`

Dashboard currently aggregates:

- branch options
- overview cards
- student growth
- fee collection
- attendance overview
- lead conversion overview
- recent activities
- footer metrics

### Branch Management

- `GET /api/super-admin/branches/options`
- `GET /api/super-admin/branches`
- `GET /api/super-admin/branches/{branchId}`
- `POST /api/super-admin/branches`
- `PUT /api/super-admin/branches/{branchId}`
- `PATCH /api/super-admin/branches/{branchId}/status`
- `DELETE /api/super-admin/branches/{branchId}`

### Admin Management

- `GET /api/super-admin/admins`
- `GET /api/super-admin/admins/{adminId}`
- `POST /api/super-admin/admins`
- `PUT /api/super-admin/admins/{adminId}`
- `PATCH /api/super-admin/admins/{adminId}/status`
- `DELETE /api/super-admin/admins/{adminId}`

### Teacher Management

- `GET /api/super-admin/teachers`
- `GET /api/super-admin/teachers/{teacherId}`
- `POST /api/super-admin/teachers`
- `PUT /api/super-admin/teachers/{teacherId}`
- `PATCH /api/super-admin/teachers/{teacherId}/status`
- `DELETE /api/super-admin/teachers/{teacherId}`

### Student Management

- `GET /api/super-admin/students`
- `GET /api/super-admin/students/{studentId}`
- `POST /api/super-admin/students`
- `PUT /api/super-admin/students/{studentId}`
- `PATCH /api/super-admin/students/{studentId}/status`
- `DELETE /api/super-admin/students/{studentId}`

## 10. Existing Service Conventions You Must Preserve

These conventions are visible in current services and should be followed:

- normalize emails and login IDs before persisting/searching
- validate uniqueness before create/update
- use soft delete for delete operations
- deactivate users when deleting/deactivating related profiles
- revoke refresh tokens when disabling or deleting a user, or when password changes
- record activities in `operational_records`
- use transactional service methods
- keep controllers thin and business logic in services
- use paginated list endpoints with summary + page metadata where applicable
- use `createdByUserId` or `createdByUser` when tracking actor context

### Example list response pattern

Existing management modules typically return:

- a `summary` object
- `content` list
- `page` metadata

If your new module is a management/listing module, match that pattern.

## 11. Existing Database and Migration Conventions

Important current DB conventions:

- PostgreSQL UUID primary keys
- Flyway migrations named like `V1__...sql`, `V2__...sql`
- partial unique indexes are used for soft-delete-safe uniqueness
- role/identity tables are already foundational and should not be reworked

When adding new tables:

- include `id`, `created_at`, `updated_at`, `is_deleted` if it is a normal domain entity
- use indexes thoughtfully
- respect soft delete strategy
- avoid destructive schema changes unless absolutely required

When adding uniqueness:

- prefer uniqueness that respects `is_deleted = false` if soft delete applies

## 12. Existing Validation and Data Handling Conventions

Observed conventions:

- request DTOs use Bean Validation
- services still perform business validations beyond DTO annotations
- strings are trimmed
- optional strings are often converted to `null`
- enums are validated explicitly when needed
- response DTOs are built manually using Lombok builders

Preserve this style.

## 13. Existing Logging / Audit Conventions

Project already includes:

- request logging filter
- flow logging aspect
- log4j2 configuration
- activity/event records in DB

Do not add noisy logging everywhere. Use the same practical style as the current project.

## 14. Current Architectural Direction

This backend is currently centered on:

- a non-tenant architecture
- a shared auth/user foundation
- role-based APIs
- super-admin-first delivery
- CRUD-style management modules
- operational records for lightweight analytics/activity

Future modules were already planned in project docs, including:

- leads and admissions
- analytics
- reports
- feedback
- attendance overview
- tests and performance
- syllabus
- stationery
- timesheets
- teacher payouts
- settings

If the new feature belongs to one of these modules, align with that roadmap rather than inventing a parallel structure.

## 15. Constraints for Your Solution

Follow these rules strictly:

1. Do not rewrite existing modules unless necessary for the new feature.
2. Do not change the API response envelope.
3. Do not replace JWT/refresh token auth.
4. Do not remove soft delete conventions.
5. Do not introduce a new architectural style inconsistent with the current code.
6. Do not add frontend code unless explicitly requested.
7. Prefer minimal, targeted changes.
8. If a new table is needed, justify why existing tables are not enough.
9. If a new endpoint is added, specify auth rule and request/response contract clearly.
10. Reuse naming and package conventions already present in this project.

## 16. What I Want From You

Please implement the new requirement using the project context above.

Your answer should be structured like this:

1. `Understanding`
   - Summarize the requirement and where it fits in the existing backend.

2. `Design Decisions`
   - Explain whether this should use existing tables/entities or new ones.
   - Explain endpoint design, security, DTOs, service changes, repository changes, and migration changes.

3. `Implementation`
   - Provide the exact Java classes, DTOs, repository methods, controller methods, entity changes, and Flyway SQL needed.
   - Keep code aligned with current project style.
   - If modifying existing files, show the updated code sections clearly.

4. `Files To Create Or Update`
   - Provide a concise file-by-file list.

5. `Safety Checks`
   - Call out anything that could break existing behavior and how you avoided it.

6. `Testing`
   - Suggest unit/integration test coverage relevant to this feature.

If the requirement is large, you may split implementation into phases, but still provide concrete code for phase 1.

## 17. New Requirement

Replace this section with the actual feature request.

Example placeholder:

`Implement a new super-admin Leads Management module with list, detail, create, update status, convert-to-admission flow, necessary database migration, DTOs, repositories, services, controllers, and operational record integration. Follow the existing branch/admin/teacher/student management style and keep all responses inside ApiResponse.`

## 18. Final Reminder

The most important thing is to **extend this backend safely and consistently**. Match the current codebase style:

- Spring Boot controller/service/repository layering
- Lombok builders
- Flyway migrations
- `ApiResponse`
- `@PreAuthorize`
- soft deletes
- role-aware business rules
- activity tracking in `operational_records`

When there is uncertainty, choose the option that is least disruptive to the existing project.

---

## Notes for the human using this prompt

- Replace the `New Requirement` section before sending.
- If needed, also attach specific existing DTOs/controllers/services from this repo to give the other AI even more precision.
- For best results, ask the other AI to return code in file-by-file blocks instead of one giant answer.
