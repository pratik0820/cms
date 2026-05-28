# AI Project Rebuild Prompt for `cms-backend`

Use this document when you want another AI, without access to this repository, to recreate this backend project from scratch as closely as possible to its current state.

This is not a feature-extension prompt. This is a full reconstruction prompt.

Do not send the old `AI_FEATURE_HANDOFF_PROMPT.md` for this purpose. Send this document instead.

---

## Prompt To Give Another AI

You are recreating an existing Java backend project from scratch. You do not have the original repository. You must rebuild the project so that its structure, stack, configuration style, package names, endpoint surface, database shape, security model, docs, and supporting files match the specification below as closely as possible.

Your goal is not to create a similar project. Your goal is to recreate this project.

If a detail is specified below, do not improvise.

If a detail is not fully specified below, infer the least disruptive version that fits the surrounding conventions.

Do not redesign the architecture.
Do not rename packages.
Do not swap frameworks.
Do not remove legacy support classes that are part of the current tree.
Do not add extra modules unless required to make the project run.

When finished, the rebuilt project should be a Spring Boot Maven project named `cms-backend` with the same package root, same controller/service layering, same current endpoint surface, same migration sequence, same resource files, same docs, and the same overall behavior.

---

## 1. Project Identity

- Project name: `cms-backend`
- Group ID: `com.classmanager`
- Artifact ID: `cms-backend`
- Java version: `21`
- Build tool: `Maven`
- Packaging: `jar`
- Spring Boot version: `3.2.5`
- Root package: `com.classmanager.cms_backend`
- Main class: `com.classmanager.cms_backend.CmsBackendApplication`
- Application type: REST backend for a Class Management System

Main app behavior:

- `@SpringBootApplication`
- `@EnableCaching`
- `@EnableAsync`
- `@EnableScheduling`
- startup log message: `Cms Application Started`

---

## 2. Exact Top-Level Repo Shape

Recreate these root-level files and directories:

- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- `.gitattributes`
- `.gitignore`
- `.env`
- `HELP.md`
- `generateDocs.js`
- `log4j2-config.xml`
- `swagger.json`
- `docs/`
- `src/main/java/com/classmanager/cms_backend/...`
- `src/main/resources/...`
- `src/test/java/com/classmanager/cms_backend/...`
- `src/test/resources/application.yaml`

Do not include IDE junk or generated output as source-of-truth files. The live repo may contain `.idea`, `target`, and `logs`, but those are not part of the source reconstruction target.

---

## 3. Current Package Tree To Recreate

Recreate these current package groups under `src/main/java/com/classmanager/cms_backend`:

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
- `schedular`
- `security`
- `security.jwt`
- `service`
- `service.jpa`
- `util`

Important:

- Do not create a new top-level `feature`, `module`, or `domain` package tree.
- Do not recreate old removed controllers such as `SuperAdminBranchController` or `AcademicManagementController` as controllers.
- The current controller layer is feature-based, but some legacy internal services still exist and must remain.

---

## 4. Exact Java File Manifest

Recreate these Java source files.

### 4.1 Root

- `CmsBackendApplication.java`

### 4.2 Config

- `AsyncConfig.java`
- `CacheConfig.java`
- `JpaAuditingConfig.java`
- `OpenApiConfig.java`
- `SecurityConfig.java`
- `SesConfig.java`
- `WebSocketConfig.java`

### 4.3 Controllers

- `AdminController.java`
- `AnalyticsController.java`
- `AuthController.java`
- `BaseController.java`
- `BranchController.java`
- `CourseController.java`
- `DashboardController.java`
- `EnrolmentController.java`
- `LeadController.java`
- `ReportController.java`
- `StandardController.java`
- `StudentController.java`
- `SubjectController.java`
- `TeacherController.java`

### 4.4 DTO Request

- `BootstrapSuperAdminRequest.java`
- `ChangePasswordRequest.java`
- `ConvertLeadToAdmissionRequest.java`
- `CreateAcademicCourseRequest.java`
- `CreateAdminRequest.java`
- `CreateBranchRequest.java`
- `CreateCourseSubjectRequest.java`
- `CreateEnrolmentRequest.java`
- `CreateLeadFollowUpRequest.java`
- `CreateLeadRequest.java`
- `CreateStandardRequest.java`
- `CreateStudentRequest.java`
- `CreateTeacherRequest.java`
- `GenerateReportRequest.java`
- `GenerateStudentPasswordRequest.java`
- `LoginRequest.java`
- `RefreshTokenRequest.java`
- `UpdateAcademicCourseRequest.java`
- `UpdateAdminRequest.java`
- `UpdateAdminStatusRequest.java`
- `UpdateBranchRequest.java`
- `UpdateBranchStatusRequest.java`
- `UpdateCourseSubjectRequest.java`
- `UpdateEnrolmentRequest.java`
- `UpdateLeadFollowUpRequest.java`
- `UpdateLeadRequest.java`
- `UpdateLeadStatusRequest.java`
- `UpdateStandardRequest.java`
- `UpdateStudentRequest.java`
- `UpdateStudentStatusRequest.java`
- `UpdateTeacherRequest.java`
- `UpdateTeacherStatusRequest.java`

### 4.5 DTO Response

- `AcademicCourseDetailResponse.java`
- `AcademicCourseListItemResponse.java`
- `AdminManagementResponse.java`
- `AdminResponse.java`
- `ApiResponse.java`
- `AuthResponse.java`
- `BoardOptionResponse.java`
- `BranchManagementResponse.java`
- `BranchResponse.java`
- `CourseSubjectResponse.java`
- `EnrolmentInstalmentResponse.java`
- `LeadConversionResponse.java`
- `LeadFollowUpPageResponse.java`
- `LeadFollowUpResponse.java`
- `LeadManagementResponse.java`
- `LeadResponse.java`
- `PagedResponse.java`
- `ReportFile.java`
- `ReportManagementResponse.java`
- `ReportResponse.java`
- `StandardResponse.java`
- `StudentEnrolmentResponse.java`
- `StudentManagementResponse.java`
- `StudentResponse.java`
- `SubjectResponse.java`
- `SuperAdminAnalyticsResponse.java`
- `SuperAdminDashboardResponse.java`
- `TeacherManagementResponse.java`
- `TeacherResponse.java`

### 4.6 Entities

- `AdminProfile.java`
- `BaseEntity.java`
- `Batch.java`
- `Branch.java`
- `Course.java`
- `EnrolmentInstalment.java`
- `GeneratedReport.java`
- `LeadFollowUp.java`
- `LeadInquiry.java`
- `OperationalRecord.java`
- `Permission.java`
- `RefreshToken.java`
- `Role.java`
- `Standard.java`
- `Student.java`
- `StudentEnrolment.java`
- `Subject.java`
- `SubjectGroup.java`
- `Teacher.java`
- `User.java`

### 4.7 Enums

- `BatchTiming.java`
- `BatchType.java`
- `BoardType.java`
- `CourseCategory.java`
- `EnrolmentStatus.java`
- `FeePaymentPlan.java`
- `PlanType.java`
- `SubjectCode.java`
- `UserRole.java`

### 4.8 Exceptions

- `BadRequestException.java`
- `BusinessRuleException.java`
- `GlobalExceptionHandler.java`
- `ResourceAlreadyExistsException.java`
- `ResourceNotFoundException.java`
- `UnauthorizedException.java`

### 4.9 Logging

- `ApiRequestLoggingFilter.java`
- `ApplicationFlowLoggingAspect.java`

### 4.10 Properties

- `SesProperties.java`

### 4.11 Repositories

- `AdminProfileRepository.java`
- `BatchRepository.java`
- `BranchRepository.java`
- `CourseRepository.java`
- `GeneratedReportRepository.java`
- `LeadFollowUpRepository.java`
- `LeadInquiryRepository.java`
- `OperationalRecordRepository.java`
- `RefreshTokenRepository.java`
- `RoleRepository.java`
- `StandardRepository.java`
- `StudentEnrolmentRepository.java`
- `StudentRepository.java`
- `SubjectRepository.java`
- `TeacherRepository.java`
- `UserRepository.java`

### 4.12 Scheduler

- `CleanupScheduler.java`

### 4.13 Security

- `CmsUserDetails.java`
- `CmsUserDetailsService.java`
- `jwt/JwtAuthenticationFilter.java`
- `jwt/JwtProperties.java`
- `jwt/JwtService.java`

### 4.14 Services

- `AcademicManagementService.java`
- `AdminService.java`
- `AnalyticsService.java`
- `AuthService.java`
- `BranchService.java`
- `CourseService.java`
- `DashboardService.java`
- `EmailTemplateService.java`
- `EnrolmentService.java`
- `LeadManagementService.java`
- `LeadService.java`
- `NoOpEmailService.java`
- `ReportService.java`
- `SesEmailService.java`
- `StandardService.java`
- `StudentEnrolmentService.java`
- `StudentManagementService.java`
- `StudentService.java`
- `SubjectResponseMapper.java`
- `SubjectService.java`
- `SuperAdminAdminService.java`
- `SuperAdminAnalyticsService.java`
- `SuperAdminBranchService.java`
- `SuperAdminDashboardService.java`
- `SuperAdminReportService.java`
- `SuperAdminTeacherService.java`
- `TeacherService.java`

### 4.15 Service Interface Package

- `service/jpa/EmailService.java`

### 4.16 Utils

- `PasswordUtils.java`
- `ValidatorUtil.java`

---

## 5. Resource File Manifest

Recreate these non-Java files under `src/main/resources`:

- `application.yaml`
- `docs/SIMPLE_DATA_FLOW.md`
- `db/migration/V1__super_admin_foundation.sql`
- `db/migration/V2__admin_management.sql`
- `db/migration/V3__teacher_management.sql`
- `db/migration/V4__student_management.sql`
- `db/migration/V5__analytics_support.sql`
- `db/migration/V6__reports_management.sql`
- `db/migration/V7__courses_batches_enrolments.sql`
- `db/migration/V8__lead_management.sql`
- `db/migration/V9__teacher_student_catalog_alignment.sql`
- `db/migration/V10__academic_screen_alignment.sql`
- `db/migration/V11__teacher_course_batch_alignment.sql`
- `db/migration/V12__lead_missing_fields.sql`
- `db/migration/V13__lead_follow_up_fields.sql`
- `db/migration/V14__lead_fee_and_scholarship_fields.sql`
- `db/migration/V15__add_salary_columns_to_teachers.sql`
- `db/migration/V16__allow_null_hourly_rate.sql`
- `templates/admin/admin-welcome.html`
- `templates/admin/admin-welcome.subject.txt`
- `templates/admin/admin-welcome.txt`
- `templates/student/student-welcome.html`
- `templates/student/student-welcome.subject.txt`
- `templates/student/student-welcome.txt`
- `templates/super-admin/super-admin-welcome.html`
- `templates/super-admin/super-admin-welcome.subject.txt`
- `templates/super-admin/super-admin-welcome.txt`
- `templates/teacher/teacher-welcome.html`
- `templates/teacher/teacher-welcome.subject.txt`
- `templates/teacher/teacher-welcome.txt`
- `templates/emails/`

`templates/emails/` currently exists as a directory even if unused or empty.

---

## 6. Test File Manifest

Recreate these test files:

- `src/test/java/com/classmanager/cms_backend/CmsBackendApplicationTests.java`
- `src/test/java/com/classmanager/cms_backend/security/CmsUserDetailsServiceTest.java`
- `src/test/java/com/classmanager/cms_backend/security/CmsUserDetailsTest.java`
- `src/test/resources/application.yaml`

The test config uses H2 in PostgreSQL mode and disables Flyway.

---

## 7. Maven Dependencies and Versions

The `pom.xml` must match this stack:

- Parent: `org.springframework.boot:spring-boot-starter-parent:3.2.5`
- Java: `21`
- `jjwt.version`: `0.12.6`
- `mapstruct.version`: `1.6.3`
- `lombok.version`: `1.18.42`
- `springdoc.version`: `2.5.0`

Dependencies to include:

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
- `flyway-database-postgresql:12.1.1`
- `jjwt-api`
- `jjwt-impl`
- `jjwt-jackson`
- `lombok`
- `mapstruct`
- `caffeine`
- `bucket4j_jdk17-core:8.17.0`
- `springdoc-openapi-starter-webmvc-ui`
- `cloudinary-http44:1.38.0`
- `firebase-admin:9.8.0`
- `commons-lang3`
- `spring-boot-starter-test`
- `spring-security-test`
- `h2`
- `spring-boot-starter-log4j2`
- `software.amazon.awssdk:ses:2.25.26`
- `software.amazon.awssdk:url-connection-client:2.25.26`

Important:

- Exclude `spring-boot-starter-logging` from `spring-boot-starter-web`.
- Use `spring-boot-maven-plugin`.

---

## 8. Runtime Configuration

Recreate `src/main/resources/application.yaml` with this configuration style and these important values:

- server port: `8095`
- context path: `/cms`
- include error messages and binding errors
- enable response compression for JSON/XML/HTML/plain text
- import `.env` using `spring.config.import: optional:file:.env[.properties]`
- app name: `cms-backend`
- datasource:
  - `jdbc:postgresql://localhost:5432/cms`
  - username `postgres`
  - password `root`
  - driver `org.postgresql.Driver`
  - Hikari pool name `CMSHikariPool`
  - max pool `10`
  - min idle `2`
- JPA:
  - `ddl-auto: none`
  - `show-sql: false`
  - PostgreSQL dialect
  - format SQL
  - batch fetch size `20`
  - insert/update ordering
  - JDBC batch size `20`
- Flyway:
  - enabled
  - `classpath:db/migration`
  - `baseline-on-migrate: true`
  - `validate-on-migrate: true`
- cache:
  - type `caffeine`
  - spec `maximumSize=500,expireAfterWrite=300s`
- Jackson:
  - no timestamp dates
  - ignore unknown properties
  - include non-null only
- async executor:
  - core `4`
  - max `16`
  - queue `100`
  - thread prefix `cms-async`
- multipart:
  - enabled
  - max file size `10MB`
  - max request size `15MB`
- JWT:
  - `jwt.secret`
  - `jwt.access-token-expiry-ms`
  - `jwt.refresh-token-expiry-ms`
- Cloudinary:
  - `cloud-name`
  - `api-key`
  - `api-secret`
  - folder prefix `cms`
- AWS:
  - `region`
  - `s3.bucket-name`
  - `access-key`
  - `secret-key`
- SES:
  - `aws.ses.enabled`
  - `aws.ses.from-email`
- Firebase:
  - `credentials-path`
  - `enabled`
- Brevo:
  - `api-key`
  - `from-email`
  - `from-name`
  - `enabled`
- CORS:
  - default allowed origins `http://localhost:3000,http://localhost:5173`
  - methods `GET,POST,PUT,DELETE,PATCH,OPTIONS`
  - headers `*`
  - credentials `true`
  - max age `3600`
- Springdoc:
  - docs path `/api-docs`
  - swagger path `/swagger-ui.html`
- rate-limit:
  - enabled `true`
  - login attempts `5`
  - login window minutes `15`
  - API requests per minute `100`
- app:
  - name `Class Manager`
  - frontend URL default `http://localhost:3000`
  - default page size `20`
  - max page size `100`
- swagger:
  - local URL `http://localhost:8095/cms`
  - production URL `http://cms.com/cms`
- logging:
  - config `log4j2-config.xml`
  - Hikari config debug/trace
  - app package debug
  - root info
- email template base path:
  - `classpath:/templates`

Security note:

- Never include real secret values from someone else’s `.env`.
- Recreate `.env` as placeholder/sample values only.

Test config in `src/test/resources/application.yaml`:

- H2 in-memory DB in PostgreSQL mode
- `ddl-auto: create-drop`
- `flyway.enabled: false`
- JWT test secret present
- same swagger URLs as above
- CORS allowed origin `http://localhost:3000`

---

## 9. Security Configuration

Recreate these security behaviors:

- stateless Spring Security
- JWT auth filter before `UsernamePasswordAuthenticationFilter`
- `@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)`
- BCrypt password encoder strength `12`
- DAO auth provider using `CmsUserDetailsService`
- hide user-not-found exceptions
- CORS configured from `cors.allowed-origins`
- expose headers:
  - `X-Total-Count`
  - `X-Page-Number`
  - `X-Page-Size`

Public routes:

- `POST /api/auth/bootstrap/super-admin`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `/actuator/health`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/api-docs/**`

Everything else requires authentication.

---

## 10. Base Classes and Cross-Cutting Conventions

### 10.1 `BaseEntity`

All normal entities follow a shared base model:

- UUID primary key
- `createdAt`
- `updatedAt`
- `isDeleted`
- `softDelete()` helper
- `@MappedSuperclass`
- auditing timestamps

### 10.2 `BaseController`

Provide helper methods:

- `currentUser()`
- `currentUserId()`
- `currentBranchId()`

### 10.3 `ApiResponse<T>`

All controller responses use this wrapper:

```json
{
  "success": true,
  "message": "Optional message",
  "data": {},
  "errorCode": null,
  "timestamp": "2026-05-28T18:30:00"
}
```

Recreate:

- Lombok data/builder/noargs/allargs
- `@JsonInclude(NON_NULL)`
- static factory methods:
  - `success(data)`
  - `success(data, message)`
  - `error(message, errorCode)`
  - `error(message)`

### 10.4 Error Handling

Use centralized exception handling in `GlobalExceptionHandler`.

Custom exceptions present:

- `BadRequestException`
- `BusinessRuleException`
- `ResourceAlreadyExistsException`
- `ResourceNotFoundException`
- `UnauthorizedException`

---

## 11. Current Architecture Rules

The current codebase uses feature-based controllers and public services:

- `CourseController`, `CourseService`
- `SubjectController`, `SubjectService`
- `StandardController`, `StandardService`
- `LeadController`, `LeadService`
- `EnrolmentController`, `EnrolmentService`
- `AdminController`, `AdminService`
- `BranchController`, `BranchService`
- `TeacherController`, `TeacherService`
- `StudentController`, `StudentService`
- `DashboardController`, `DashboardService`
- `AnalyticsController`, `AnalyticsService`
- `ReportController`, `ReportService`

But legacy internal services still exist and should remain, such as:

- `AcademicManagementService`
- `LeadManagementService`
- `StudentEnrolmentService`
- `StudentManagementService`
- `SuperAdminAdminService`
- `SuperAdminAnalyticsService`
- `SuperAdminBranchService`
- `SuperAdminDashboardService`
- `SuperAdminReportService`
- `SuperAdminTeacherService`

Recreate the current hybrid state.

Do not “clean this up” by deleting the legacy services.

---

## 12. Entity and Table Model

The database is PostgreSQL with Flyway migrations.

Current major tables and concepts:

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
- `generated_reports`

Roles seeded in the system:

- `SUPER_ADMIN`
- `ADMIN`
- `TEACHER`
- `STUDENT`
- `PARENT`

Important domain model rules:

- soft delete is used broadly
- UUID keys are used
- users are the auth identity base
- refresh tokens are DB-backed
- lead conversion can produce student + enrolment records
- batch remains a real DB concept
- course is the main user-facing academic concept
- batch is increasingly treated as an internal child of course in the API flow

---

## 13. Academic Model Rules

The current academic data model is intentionally normalized:

- `courses` = master academic offering
- `batches` = running instance of a course
- `subjects` = global subject catalog
- `course_subjects` = course-to-subject mapping
- `subject_groups` = predefined subject combinations
- `subject_group_subjects` = default subjects in a group
- `subject_group_extra_subjects` = optional extra subjects
- `teacher_subjects` = teacher-to-subject mapping

Important behavioral rule to preserve:

- keep `batches` in the database
- do not treat batch as a fully separate user-facing module unless necessary
- course creation flow also creates or manages the linked batch internally
- course detail should be resolved course-first, not batch-first

---

## 14. Authentication Model

Recreate these auth features:

- bootstrap super admin
- login
- refresh token rotation
- logout current device
- logout all devices
- change password
- current user profile (`/me`)

Auth rules:

- access token is JWT
- refresh token is opaque random token
- refresh token hash is stored in DB
- refresh token reuse triggers session revocation
- login supports `identifier` and legacy `email`
- user can log in by `email` or `loginId`
- failed attempts can temporarily lock the account
- disabling/deleting users should revoke refresh tokens

`CmsUserDetails` should expose current auth identity including branch and roles.

---

## 15. API Surface To Recreate

All routes below are under the server context path `/cms`.

### 15.1 Auth

- `POST /api/auth/bootstrap/super-admin`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `PUT /api/auth/change-password`
- `GET /api/auth/me`

### 15.2 Branch

- `GET /api/super-admin/branches/options`
- `GET /api/super-admin/branches`
- `GET /api/super-admin/branches/{branchId}`
- `POST /api/super-admin/branches`
- `PUT /api/super-admin/branches/{branchId}`
- `PATCH /api/super-admin/branches/{branchId}/status`
- `DELETE /api/super-admin/branches/{branchId}`

### 15.3 Admin

- `GET /api/super-admin/admins`
- `GET /api/super-admin/admins/{adminId}`
- `POST /api/super-admin/admins`
- `PUT /api/super-admin/admins/{adminId}`
- `PATCH /api/super-admin/admins/{adminId}/status`
- `DELETE /api/super-admin/admins/{adminId}`

### 15.4 Teacher

- `GET /api/super-admin/teachers`
- `GET /api/super-admin/teachers/{teacherId}`
- `POST /api/super-admin/teachers`
- `PUT /api/super-admin/teachers/{teacherId}`
- `PATCH /api/super-admin/teachers/{teacherId}/status`
- `DELETE /api/super-admin/teachers/{teacherId}`

### 15.5 Student

- `GET /api/super-admin/students`
- `GET /api/super-admin/students/{studentId}`
- `POST /api/super-admin/students`
- `PUT /api/super-admin/students/{studentId}`
- `PATCH /api/super-admin/students/{studentId}/status`
- `POST /api/super-admin/students/{studentId}/generate-password`
- `DELETE /api/super-admin/students/{studentId}`

### 15.6 Standards and Boards

- `GET /api/academic/boards`
- `GET /api/academic/standards`
- `GET /api/academic/standards/options`
- `POST /api/academic/standards`
- `PUT /api/academic/standards/{standardId}`
- `DELETE /api/academic/standards/{standardId}`

### 15.7 Courses and Subjects

- `GET /api/academic/courses`
- `POST /api/academic/courses`
- `GET /api/academic/courses/{courseId}`
- `PUT /api/academic/courses/{courseId}`
- `DELETE /api/academic/courses/{courseId}`
- `GET /api/academic/subjects`
- `POST /api/academic/courses/{courseId}/subjects`
- `PUT /api/academic/courses/{courseId}/subjects/{subjectId}`
- `DELETE /api/academic/courses/{courseId}/subjects/{subjectId}`

### 15.8 Enrolments

- `POST /api/enrolments`
- `GET /api/enrolments/{enrolmentId}`
- `GET /api/enrolments/student/{studentId}`
- `GET /api/enrolments/batch/{batchId}`
- `GET /api/enrolments/branch/{branchId}`
- `PUT /api/enrolments/{enrolmentId}`
- `DELETE /api/enrolments/{enrolmentId}`

### 15.9 Leads

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

### 15.10 Dashboard / Analytics / Reports

- `GET /api/super-admin/dashboard`
- `GET /api/super-admin/analytics`
- `GET /api/super-admin/reports/summary`
- `GET /api/super-admin/reports/categories`
- `GET /api/super-admin/reports`
- `POST /api/super-admin/reports/export`
- `GET /api/super-admin/reports/{reportId}/download`
- `DELETE /api/super-admin/reports/{reportId}`

---

## 16. Authorization Rules

Recreate these method-level patterns:

- branch/admin/teacher/student/lead/report/dashboard/analytics super-admin surfaces use `@PreAuthorize("hasRole('SUPER_ADMIN')")` or equivalent method-level restrictions
- course/standard/subject creation and mutation use `hasAnyRole('SUPER_ADMIN','ADMIN')`
- enrolment create/update/delete use `hasAnyRole('SUPER_ADMIN','ADMIN')`
- some read APIs use `isAuthenticated()`

Specific conventions to preserve:

- `BranchController` class-level `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- `AdminController` class-level `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- `TeacherController` class-level `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- `StudentController` class-level `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- `LeadController` class-level `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- `ReportController` class-level `@PreAuthorize("hasRole('SUPER_ADMIN')")`

---

## 17. Service and Business Conventions

Recreate these conventions:

- controllers stay thin
- services contain business logic
- repositories contain JPA access
- request DTOs use Bean Validation
- response DTOs use Lombok builders heavily
- normalize email and login IDs
- trim strings
- convert optional blank strings to null where appropriate
- validate uniqueness before create/update
- use soft delete instead of hard delete
- deactivate linked auth user where relevant
- revoke refresh tokens on user disable/delete or password-sensitive changes
- record business activity in `operational_records`
- transactional service methods for write flows
- list APIs commonly return `summary`, `content`, and `page`

Avoid the anti-pattern of one giant generic service method for unrelated APIs.

---

## 18. Lead Module Rules

Preserve the lead workflow complexity. Do not flatten it.

Lead list filters should support:

- `search`
- `branchId`
- `status`
- `leadSource`
- `courseId`
- `batchId`
- `createdByUserId`

Lead records include support for:

- counsellor assignment
- assigned-to user
- fee fields
- scholarship/concession fields
- next follow-up data
- separate follow-up records in `lead_follow_ups`
- conversion into student and optional admission/enrolment records

The lead conversion flow touches:

1. `lead_inquiries`
2. `lead_follow_ups`
3. `students`
4. optional `student_enrolments`
5. optional `enrolment_subjects`
6. optional `enrolment_instalments`
7. `operational_records`

---

## 19. Report and Documentation Artifacts

Recreate these documentation files under `docs/`:

- `AI_FEATURE_HANDOFF_PROMPT.md`
- `API_DOCUMENTATION.md`
- `FRONTEND_API_DOCS.md`
- `POSTMAN_FRONTEND_FLOW_GUIDE.md`
- `phase1_requirements_extracted.txt`

Also recreate:

- `swagger.json`
- `generateDocs.js`

`generateDocs.js` should:

- read `swagger.json`
- resolve schemas
- generate markdown docs
- write output to `docs/FRONTEND_API_DOCS.md`

---

## 20. Email Templates

Recreate welcome email templates for these roles:

- `admin`
- `student`
- `super-admin`
- `teacher`

Each role has:

- `.html`
- `.subject.txt`
- `.txt`

The project also contains:

- `EmailTemplateService`
- `SesEmailService`
- `NoOpEmailService`
- `SesConfig`
- `SesProperties`
- `service/jpa/EmailService.java`

Preserve the AWS SES-oriented email structure even if parts are placeholder or environment-dependent.

---

## 21. Logging, Async, Cache, and Scheduler

Recreate support for:

- Log4j2 config file
- request logging filter
- application flow logging aspect
- async executor config
- caffeine cache config
- scheduling support
- cleanup scheduler

Do not silently replace Log4j2 with default Boot logging.

---

## 22. Current Test Expectations

At minimum, recreate the existing test layer shape:

- startup context test
- `CmsUserDetails` test
- `CmsUserDetailsService` test

These tests should compile and run in the rebuilt project.

---

## 23. Important Things You Must Not Do

1. Do not invent a new architecture.
2. Do not switch away from Spring Boot + Maven + PostgreSQL + Flyway.
3. Do not replace JWT + DB refresh token auth with session auth.
4. Do not remove soft delete.
5. Do not remove legacy internal services just because the controller layer was modernized.
6. Do not collapse the project into a tiny demo implementation.
7. Do not omit docs, templates, migrations, or tests.
8. Do not include real secrets.
9. Do not create the old deleted controller classes as active current controllers.
10. Do not make batch the primary lookup model for the academic APIs.

---

## 24. Expected Output Format From The Other AI

The other AI should return the project in a reconstruction-friendly format.

Ask it to:

1. create the complete file tree
2. provide file-by-file code blocks for all created files if it cannot write files directly
3. preserve exact package names and paths
4. explicitly list every file it created
5. call out any places where it had to infer details
6. ensure the project is buildable with Maven
7. ensure the app can start with PostgreSQL config and tests can run with H2

If the answer is too large for one response, it should emit the repository in multiple continuation parts, but without skipping any file.

---

## 25. Final Instruction To The Other AI

Rebuild this project as a full repository, not as a high-level sketch.

Treat the specification above as the source of truth. Match the current project state as closely as possible:

- same root package
- same file names
- same controller surface
- same security model
- same migration history
- same resource structure
- same DTO/entity/repository/service layering
- same docs and template artifacts

When in doubt, choose the version that is most conservative and most compatible with the rest of this specification.

---

## Notes For The Human Sending This Prompt

- Send this whole document, not excerpts.
- Do not send real `.env` secrets.
- If you want even tighter fidelity, also attach the live `swagger.json` content separately, because it contains the exact schema/endpoint contract.
- If the other AI can only answer in chunks, ask it to continue until every file in the manifest is produced.
