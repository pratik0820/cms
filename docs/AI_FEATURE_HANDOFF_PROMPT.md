# AI Feature Handoff Prompt for `cms-backend`

Use this prompt when asking another AI assistant such as Claude to continue this backend project. Replace the requirement section near the end before sending it.

---

## Prompt

You are helping on an existing Java backend project. Your job is to design and implement a new feature without breaking the current package structure, existing APIs, security model, response wrapper, database conventions, or current business flow.

You do not have direct access to the repository, so work only from the context below. Be conservative. Extend the current codebase cleanly. Do not invent a parallel architecture, parallel package tree, or broad rewrite unless explicitly requested.

If something is unclear, prefer the project conventions described below over introducing a new pattern.

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
- Context path: `/cms`
- Local base URL: `http://localhost:8095/cms`

## 2. Important Rule Before You Start

Do not create a new top-level feature package tree such as `feature/...`.

This project already has established top-level packages like:

- `config`
- `controller`
- `dto`
- `entity`
- `enums`
- `exception`
- `logging`
- `notification`
- `properties`
- `repository`
- `schedular`
- `security`
- `service`
- `storage`
- `tenant`
- `util`

If you add or refactor code, keep it inside these existing packages.

## 3. Current Architecture Direction

The project is moving away from role-bucket controller naming like:

- `SuperAdminXController`
- `AcademicManagementController`
- `StudentEnrolmentController`

and toward feature-based controller/service naming inside the same existing packages, such as:

- `CourseController`
- `SubjectController`
- `StandardController`
- `LeadController`
- `EnrolmentController`
- `AdminController`
- `BranchController`
- `TeacherController`
- `StudentController`
- `DashboardController`
- `AnalyticsController`
- `ReportController`

The same applies to services:

- `CourseService`
- `SubjectService`
- `StandardService`
- `LeadService`
- `EnrolmentService`
- `AdminService`
- `BranchService`
- `TeacherService`
- `StudentService`
- `DashboardService`
- `AnalyticsService`
- `ReportService`

Important: some of these feature services currently delegate to older internal implementation services such as:

- `AcademicManagementService`
- `LeadManagementService`
- `StudentEnrolmentService`
- `StudentManagementService`
- `SuperAdmin*Service`

Preserve behavior first. If you refactor deeper, do it carefully and incrementally. Do not break existing flows just to remove legacy service names.

## 4. Tech Stack and Libraries

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
- `mapstruct` is present, but most code uses manual mapping/builders
- `caffeine`
- `bucket4j`
- `springdoc-openapi`
- `cloudinary-http44`
- `firebase-admin`
- `aws ses sdk`
- `log4j2`

Do not change the stack unless explicitly asked.

## 5. Runtime and Config Conventions

- Main config file: `src/main/resources/application.yaml`
- `.env` values are imported via `spring.config.import`
- Swagger path: `/swagger-ui.html`
- OpenAPI docs path: `/api-docs`
- Default local frontend origins include `http://localhost:3000` and `http://localhost:5173`
- Multipart upload is enabled
- Cache type is `caffeine`
- JWT settings come from `jwt.*`

Preserve existing configuration style.

## 6. Package and Implementation Pattern

Typical feature implementation in this codebase should stay in this shape:

1. request DTOs
2. response DTOs
3. repository methods
4. service methods
5. controller endpoints
6. Flyway migration only if schema change is truly needed

Important implementation preference:

- separate service methods per API/use case
- avoid one overly generic method reused for many unrelated APIs
- keep controllers thin
- keep business logic in services
- keep JPA access in repositories

## 7. Core Data Model

The codebase includes these major entities/tables:

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
- `courses`
- `batches`
- `subjects`
- `course_subjects`
- `subject_groups`
- `subject_group_subjects`
- `subject_group_extra_subjects`
- `lead_inquiries`
- `lead_follow_ups`
- `student_enrolments`
- `enrolment_subjects`
- `enrolment_instalments`
- `operational_records`

Most entities extend `BaseEntity` with:

- `id : UUID`
- `createdAt`
- `updatedAt`
- `isDeleted`
- soft delete behavior

Do not replace soft delete conventions unless explicitly required.

## 8. Why There Are Multiple Academic Tables

The academic model is intentionally normalized:

- `courses` = master academic offering
- `batches` = running instance of a course
- `subjects` = global subject catalog
- `course_subjects` = course-to-subject mapping
- `subject_groups` = predefined subject combinations
- `subject_group_subjects` = default subjects inside a group
- `subject_group_extra_subjects` = optional extra subjects for a group
- `teacher_subjects` = teacher-to-subject mapping

The problem in this project is not that these tables exist. The real problem has been confusing API flow and indirect lookup logic. New work should keep flow straightforward.

## 9. Current Course/Batch Simplification Rule

This is very important for future work:

- `batches` should remain in the database for now
- but `batch` should not be treated as a separate user-facing module unless truly needed
- `course` is the main user-facing concept
- batch is an internal child of course

Current intended behavior:

- creating a course also creates one batch internally
- if batch name is omitted, default it to `"Morning Batch"`
- fetching a course should load the course first, then resolve its linked active batch internally
- updating a course should also update its linked batch as needed

Do not reintroduce a confusing design where a course API primarily resolves from batch identity first.

## 10. Existing Roles and Authorization

Current roles seeded by migration:

- `SUPER_ADMIN`
- `ADMIN`
- `TEACHER`
- `STUDENT`
- `PARENT`

Current security conventions:

- public auth endpoints are explicitly permitted in `SecurityConfig`
- most management controllers use `@PreAuthorize(...)`
- authenticated user info is available via `CmsUserDetails`
- `BaseController` provides helpers like `currentUserId()` and `currentBranchId()`

Do not weaken security. New endpoints must follow existing authorization style.

## 11. Authentication Model

Already implemented:

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
- refresh token reuse detection revokes all sessions
- login supports `email` or `loginId`
- repeated failures can lock the account temporarily

Do not replace this auth system.

## 12. API Response Style

All APIs use a standard wrapper:

```json
{
  "success": true,
  "message": "Optional message",
  "data": {},
  "errorCode": null,
  "timestamp": "2026-05-28T18:30:00"
}
```

Use `ApiResponse<T>` for controller responses.

Exception handling is centralized in `GlobalExceptionHandler`.

Common exception types already used:

- `BadRequestException`
- `UnauthorizedException`
- `ResourceNotFoundException`
- `ResourceAlreadyExistsException`
- `BusinessRuleException`

Do not invent a new response envelope.

## 13. Existing Feature Modules and Active Controller Surface

These modules exist and should be preserved:

### Auth

- `POST /api/auth/bootstrap/super-admin`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `PUT /api/auth/change-password`
- `GET /api/auth/me`

### Academic

- `GET /api/academic/boards`
- `GET /api/academic/standards`
- `GET /api/academic/standards/options`
- `POST /api/academic/standards`
- `PUT /api/academic/standards/{standardId}`
- `DELETE /api/academic/standards/{standardId}`

- `GET /api/academic/courses`
- `POST /api/academic/courses`
- `GET /api/academic/courses/{courseId}`
- `PUT /api/academic/courses/{courseId}`
- `DELETE /api/academic/courses/{courseId}`

- `GET /api/academic/subjects`
- `POST /api/academic/courses/{courseId}/subjects`
- `PUT /api/academic/courses/{courseId}/subjects/{subjectId}`
- `DELETE /api/academic/courses/{courseId}/subjects/{subjectId}`

### Branch

- `GET /api/super-admin/branches/options`
- `GET /api/super-admin/branches`
- `GET /api/super-admin/branches/{branchId}`
- `POST /api/super-admin/branches`
- `PUT /api/super-admin/branches/{branchId}`
- `PATCH /api/super-admin/branches/{branchId}/status`
- `DELETE /api/super-admin/branches/{branchId}`

### Admin

- `GET /api/super-admin/admins`
- `GET /api/super-admin/admins/{adminId}`
- `POST /api/super-admin/admins`
- `PUT /api/super-admin/admins/{adminId}`
- `PATCH /api/super-admin/admins/{adminId}/status`
- `DELETE /api/super-admin/admins/{adminId}`

### Teacher

- `GET /api/super-admin/teachers`
- `GET /api/super-admin/teachers/{teacherId}`
- `POST /api/super-admin/teachers`
- `PUT /api/super-admin/teachers/{teacherId}`
- `PATCH /api/super-admin/teachers/{teacherId}/status`
- `DELETE /api/super-admin/teachers/{teacherId}`

### Student

- `GET /api/super-admin/students`
- `GET /api/super-admin/students/{studentId}`
- `POST /api/super-admin/students`
- `PUT /api/super-admin/students/{studentId}`
- `PATCH /api/super-admin/students/{studentId}/status`
- `POST /api/super-admin/students/{studentId}/generate-password`
- `DELETE /api/super-admin/students/{studentId}`

### Enrolment

- `POST /api/enrolments`
- `GET /api/enrolments/{enrolmentId}`
- `GET /api/enrolments/student/{studentId}`
- `GET /api/enrolments/batch/{batchId}`
- `GET /api/enrolments/branch/{branchId}`
- `PUT /api/enrolments/{enrolmentId}`
- `DELETE /api/enrolments/{enrolmentId}`

### Lead Management

- `GET /api/super-admin/leads`
- `GET /api/super-admin/leads/{leadId}`
- `POST /api/super-admin/leads`
- `PUT /api/super-admin/leads/{leadId}`
- `PATCH /api/super-admin/leads/{leadId}/status`
- `POST /api/super-admin/leads/{leadId}/convert-to-admission`
- `DELETE /api/super-admin/leads/{leadId}`
- `GET /api/super-admin/leads/follow-ups`
- `POST /api/super-admin/leads/{leadId}/follow-ups`
- `GET /api/super-admin/leads/{leadId}/follow-ups`
- `PUT /api/super-admin/leads/follow-ups/{followUpId}`

### Dashboard / Analytics / Reports

- `GET /api/super-admin/dashboard`
- `GET /api/super-admin/analytics`
- `GET /api/super-admin/reports/summary`
- `GET /api/super-admin/reports/categories`
- `GET /api/super-admin/reports`
- `POST /api/super-admin/reports/export`
- `GET /api/super-admin/reports/{reportId}/download`
- `DELETE /api/super-admin/reports/{reportId}`

## 14. Lead Module Details You Must Preserve

The latest pulled code includes a more detailed lead workflow. Treat this as current behavior:

- lead listing supports filtering by:
  - `search`
  - `branchId`
  - `status`
  - `leadSource`
  - `courseId`
  - `batchId`
  - `createdByUserId`
- lead records include:
  - counsellor assignment
  - assigned-to user
  - fee fields
  - scholarship/concession fields
  - next follow-up information
- lead follow-ups are stored separately in `lead_follow_ups`
- converting a lead to admission can also create downstream admission/enrolment data

Important lead tables and flow:

1. `lead_inquiries`
2. `lead_follow_ups`
3. `students`
4. optional `student_enrolments`
5. optional `enrolment_subjects`
6. optional `enrolment_instalments`
7. `operational_records`

Do not simplify the lead module by removing this structure unless explicitly told to do so.

## 15. Service and Data Handling Conventions

Preserve these conventions:

- normalize emails and login IDs before searching or persisting
- validate uniqueness before create/update
- use soft delete for delete operations
- deactivate linked users when deactivating/deleting related profiles where applicable
- revoke refresh tokens when disabling or deleting a user, or after password-sensitive changes
- record activities in `operational_records`
- use transactional service methods
- trim strings
- convert optional blank strings to `null` where current style does so
- use Bean Validation on request DTOs
- use Lombok builders for response DTOs

For management list APIs, preserve the current response style with:

- `summary`
- `content`
- `page`

## 16. Database and Migration Conventions

Important current DB conventions:

- PostgreSQL UUID primary keys
- Flyway migrations named like `V1__...sql`, `V2__...sql`
- soft-delete-safe uniqueness via partial indexes where appropriate
- role and auth tables are foundational and should not be reworked casually

When adding new tables:

- include standard base entity fields if it is a normal domain entity
- add indexes thoughtfully
- follow soft delete strategy
- avoid destructive schema changes unless unavoidable

If an existing table can safely support the feature, prefer reusing it.

## 17. Existing Documentation You Should Respect

There are project documents that explain current flow and should be treated as source context:

- `docs/FEATURE_API_FLOW_MAP.md`
- `src/main/resources/docs/SIMPLE_DATA_FLOW.md`

Important interpretation:

- the project wants straightforward feature flow
- APIs should clearly show what table they read/write
- indirect lookups that confuse course vs batch vs enrolment should be reduced over time

## 18. What You Must Not Do

Do not do the following unless explicitly requested:

1. Do not create a parallel top-level package tree.
2. Do not change the response wrapper format.
3. Do not replace the auth system.
4. Do not remove soft delete conventions.
5. Do not broadly rename everything without preserving API behavior.
6. Do not merge unrelated APIs into one generic service method.
7. Do not expose batch again as a separate primary user-facing module if the work is about course-first simplification.
8. Do not rewrite large parts of the project just because the code looks inconsistent.

## 19. What I Want From You

Please implement the requested change using the project context above.

Structure your answer like this:

1. `Understanding`
   - summarize the requirement and where it fits

2. `Design Decisions`
   - explain table usage, endpoint changes, security, DTOs, service methods, repository methods, and migration impact

3. `Implementation`
   - provide exact Java classes or exact code changes
   - if modifying existing files, show the updated sections clearly

4. `Files To Create Or Update`
   - concise file-by-file list

5. `Flow Impact`
   - explain which APIs read/write which tables
   - explain any behavior change very clearly

6. `Safety Checks`
   - call out what could break and how you avoided it

7. `Testing`
   - suggest unit/integration/startup checks

If the requirement is large, you may split it into phases, but phase 1 must still be concrete and code-ready.

## 20. New Requirement

Replace this section with the actual feature request.

Example placeholder:

`Implement a new attendance workflow that fits the current controller/service/repository structure, preserves ApiResponse, records operational activity when needed, and clearly documents which tables are read and written by each new endpoint.`

## 21. Final Reminder

The most important thing is to extend this backend safely and consistently.

Match the current project style:

- existing top-level package layout
- controller/service/repository layering
- separate service methods per API/use case
- Lombok builders
- Flyway migrations
- `ApiResponse`
- `@PreAuthorize`
- soft deletes
- role-aware business rules
- operational activity tracking
- straightforward feature flow

When there is uncertainty, choose the least disruptive option that keeps behavior understandable and easy to continue.

---

## Notes for the Human Using This Prompt

- Replace the `New Requirement` section before sending.
- If you want very accurate continuation, also paste the relevant current controller, service, DTO, and entity snippets with this prompt.
- If the requested work touches course, batch, lead, or enrolment flow, also attach:
  - `docs/FEATURE_API_FLOW_MAP.md`
  - `src/main/resources/docs/SIMPLE_DATA_FLOW.md`
- Ask the other AI to return code in file-by-file blocks, not one giant mixed answer.
