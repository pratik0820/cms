# Feature API Flow Map

## Purpose

This document is the "what actually happens" map for the current backend.

It is meant to answer:

- which controller receives each API
- which service method executes the main logic
- which tables are read
- which tables are written
- where the flow is currently indirect or confusing
- what the target feature-based module layout should be

## Target Module Layout

The backend should move toward this structure for every feature:

```text
src/main/java/com/classmanager/cms_backend/feature/
  auth/
    controller/
    service/
    repository/
  branch/
    controller/
    service/
    repository/
  standard/
    controller/
    service/
    repository/
  course/
    controller/
    service/
    repository/
  enrolment/
    controller/
    service/
    repository/
  lead/
    controller/
    service/
    repository/
  admin/
    controller/
    service/
    repository/
  teacher/
    controller/
    service/
    repository/
  student/
    controller/
    service/
    repository/
  dashboard/
    controller/
    service/
    repository/
  analytics/
    controller/
    service/
    repository/
  report/
    controller/
    service/
    repository/
```

Rules for the refactor:

- controller classes should be feature-based, not role-bucket based
- each API should call a clearly named service method
- repositories should be feature-facing, even if they wrap lower-level JPA repositories
- read flow should not jump unexpectedly across unrelated tables
- write flow should be explicit when one API writes to more than one table

## Current Status

The code now uses feature-oriented classes inside the existing package structure:

- [StandardController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/StandardController.java)
- [CourseController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/CourseController.java)
- [CourseSubjectController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/CourseSubjectController.java)
- [AdminController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/AdminController.java)
- [BranchController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/BranchController.java)
- [TeacherController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/TeacherController.java)
- [StudentController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/StudentController.java)
- [LeadController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/LeadController.java)
- [EnrolmentController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/EnrolmentController.java)
- [DashboardController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/DashboardController.java)
- [AnalyticsController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/AnalyticsController.java)
- [ReportController](/D:/d%20folder/CMS/cms-backend/src/main/java/com/classmanager/cms_backend/controller/ReportController.java)

Supporting feature services and repositories now live in the same existing `service` and `repository` packages.

Important note:

- several new feature services currently delegate to older implementation classes internally
- this keeps the behavior stable while we move the business logic feature by feature
- the next step is to migrate the internal implementation classes and repositories the same way

## Main Flow Issue Found

The course flow is the clearest example of why the current code feels confusing:

- `POST /api/academic/courses` creates a row in `courses`
- the same API also creates a row in `batches`
- `GET /api/academic/courses/{courseId}` first loads `courses`
- after that it still requires an active matching row in `batches`
- if the batch is missing, inactive, branch-mismatched, or soft-deleted, the course lookup fails even though the `courses` row exists

So the current model is not really "course only".
It is effectively "course + active batch view".

That is why the read flow feels indirect.

## Table Reference

Important tables used by the current APIs:

- `users`
- `roles`
- `user_roles`
- `refresh_tokens`
- `branches`
- `admin_profiles`
- `teachers`
- `teacher_subjects`
- `teacher_subject_assignments`
- `teacher_course_assignments`
- `teacher_batch_assignments`
- `students`
- `standards`
- `courses`
- `subjects`
- `course_subjects`
- `subject_groups`
- `batches`
- `student_enrolments`
- `enrolment_subjects`
- `enrolment_instalments`
- `lead_inquiries`
- `lead_subjects`
- `lead_follow_ups`
- `generated_reports`
- `operational_records`

## API Flow Map

### Auth

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `POST /api/auth/bootstrap/super-admin` | `AuthController` | `AuthService.bootstrapSuperAdmin` | `users`, `roles` | `users`, `user_roles`, `refresh_tokens` | Creates the first super admin and login session |
| `POST /api/auth/login` | `AuthController` | `AuthService.login` | `users`, `roles` | `users`, `refresh_tokens` | Updates login metadata and saves device token if present |
| `POST /api/auth/refresh` | `AuthController` | `AuthService.refreshToken` | `refresh_tokens`, `users` | `refresh_tokens` | Marks old token used and creates a new token row |
| `POST /api/auth/logout` | `AuthController` | `AuthService.logout` | `refresh_tokens` | `refresh_tokens` | Revokes one refresh token |
| `POST /api/auth/logout-all` | `AuthController` | `AuthService.logoutAllDevices` | `refresh_tokens` | `refresh_tokens` | Revokes all sessions for the current user |
| `PUT /api/auth/change-password` | `AuthController` | `AuthService.changePassword` | `users` | `users`, `refresh_tokens` | Password change also revokes all sessions |
| `GET /api/auth/me` | `AuthController` | `AuthService.buildUserInfo` | `users`, `roles`, `branches` | None | Returns current logged-in user context |

### Branch

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/super-admin/branches/options` | `BranchController` | `BranchService.getBranchOptions` | `branches` | None | Active branches only |
| `GET /api/super-admin/branches` | `BranchController` | `BranchService.getBranches` | `branches` | None | Search + summary counts |
| `GET /api/super-admin/branches/{branchId}` | `BranchController` | `BranchService.getBranch` | `branches`, `students`, `teachers`, `users` | None | Summary counts are derived live |
| `POST /api/super-admin/branches` | `BranchController` | `BranchService.createBranch` | `branches` | `branches`, `operational_records` | Activity audit is saved |
| `PUT /api/super-admin/branches/{branchId}` | `BranchController` | `BranchService.updateBranch` | `branches` | `branches`, `operational_records` | Activity audit is saved |
| `PATCH /api/super-admin/branches/{branchId}/status` | `BranchController` | `BranchService.updateBranchStatus` | `branches` | `branches`, `operational_records` | Active flag changes only |
| `DELETE /api/super-admin/branches/{branchId}` | `BranchController` | `BranchService.deleteBranch` | `branches`, `students`, `teachers`, `users` | `branches`, `operational_records` | Soft-delete only, blocked when linked data exists |

### Admin

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/super-admin/admins` | `AdminController` | `AdminService.getAdmins` | `admin_profiles`, `users`, `branches` | None | Search is profile-driven |
| `GET /api/super-admin/admins/{adminId}` | `AdminController` | `AdminService.getAdmin` | `admin_profiles`, `users`, `branches` | None | Reads admin profile plus linked user |
| `POST /api/super-admin/admins` | `AdminController` | `AdminService.createAdmin` | `users`, `roles`, `branches` | `users`, `user_roles`, `admin_profiles`, `operational_records` | One API creates login + admin profile |
| `PUT /api/super-admin/admins/{adminId}` | `AdminController` | `AdminService.updateAdmin` | `admin_profiles`, `users`, `branches` | `users`, `admin_profiles`, `operational_records`, `refresh_tokens` | Password update revokes sessions |
| `PATCH /api/super-admin/admins/{adminId}/status` | `AdminController` | `AdminService.updateAdminStatus` | `admin_profiles`, `users` | `users`, `operational_records`, `refresh_tokens` | Deactivation revokes sessions |
| `DELETE /api/super-admin/admins/{adminId}` | `AdminController` | `AdminService.deleteAdmin` | `admin_profiles`, `users` | `admin_profiles`, `users`, `operational_records`, `refresh_tokens` | Soft-delete on both profile and user |

### Academic Standards

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/academic/boards` | `StandardController` | `StandardService.listBoards` | None | None | Enum-only response |
| `GET /api/academic/standards` | `StandardController` | `StandardService.listStandards` | `standards` | None | Search + paging |
| `GET /api/academic/standards/options` | `StandardController` | `StandardService.listStandardOptions` | `standards` | None | Active rows only |
| `POST /api/academic/standards` | `StandardController` | `StandardService.createStandard` | `standards` | `standards` | Creates one standard row |
| `PUT /api/academic/standards/{standardId}` | `StandardController` | `StandardService.updateStandard` | `standards` | `standards` | Updates one standard row |
| `DELETE /api/academic/standards/{standardId}` | `StandardController` | `StandardService.deleteStandard` | `standards` | `standards` | Soft-delete by flags |

### Academic Courses

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/academic/courses` | `CourseController` | `CourseService.listCourses` | `batches`, `courses`, `standards` | None | Current list is batch-driven, not pure course-driven |
| `POST /api/academic/courses` | `CourseController` | `CourseService.createCourse` | `standards`, `branches` | `courses`, `batches` | One API writes both course and batch |
| `GET /api/academic/courses/{courseId}` | `CourseController` | `CourseService.getCourse` | `courses`, `course_subjects`, `subjects`, `batches` | None | Fails if active batch resolution fails |
| `PUT /api/academic/courses/{courseId}` | `CourseController` | `CourseService.updateCourse` | `courses`, `course_subjects`, `subjects`, `batches`, `standards` | `courses`, `batches` | Updates course row plus one resolved batch row |
| `DELETE /api/academic/courses/{courseId}` | `CourseController` | `CourseService.deleteCourse` | `courses`, `batches` | `courses`, `batches` | Soft-delete both sides |

### Academic Course Subjects

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `POST /api/academic/courses/{courseId}/subjects` | `CourseSubjectController` | `CourseSubjectService.addSubject` | `courses`, `course_subjects`, `subjects`, `batches` | `subjects`, `course_subjects` | Creates subject then links it to the course |
| `PUT /api/academic/courses/{courseId}/subjects/{subjectId}` | `CourseSubjectController` | `CourseSubjectService.updateSubject` | `courses`, `course_subjects`, `subjects`, `batches` | `subjects` | Updates the shared subject row |
| `DELETE /api/academic/courses/{courseId}/subjects/{subjectId}` | `CourseSubjectController` | `CourseSubjectService.deleteSubject` | `courses`, `course_subjects`, `subjects`, `batches` | `course_subjects`, `subjects` | Unlinks and soft-deletes subject |

### Teacher

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/super-admin/teachers` | `TeacherController` | `TeacherService.getTeachers` | `teachers`, `users`, `branches`, `subjects`, `courses`, `batches` | None | Search can filter by subject, course, and batch |
| `GET /api/super-admin/teachers/{teacherId}` | `TeacherController` | `TeacherService.getTeacher` | `teachers`, `users`, `branches`, `teacher_subject_assignments`, `teacher_course_assignments`, `teacher_batch_assignments` | None | Returns linked assignments |
| `POST /api/super-admin/teachers` | `TeacherController` | `TeacherService.createTeacher` | `users`, `roles`, `branches`, `subjects`, `courses`, `batches` | `users`, `user_roles`, `teachers`, `teacher_subjects`, `teacher_subject_assignments`, `teacher_course_assignments`, `teacher_batch_assignments`, `operational_records` | One API creates account + academic mappings |
| `PUT /api/super-admin/teachers/{teacherId}` | `TeacherController` | `TeacherService.updateTeacher` | `teachers`, `users`, `branches`, `subjects`, `courses`, `batches` | `users`, `teachers`, assignment tables, `operational_records`, `refresh_tokens` | Password change revokes sessions |
| `PATCH /api/super-admin/teachers/{teacherId}/status` | `TeacherController` | `TeacherService.updateTeacherStatus` | `teachers`, `users` | `teachers`, `users`, `operational_records`, `refresh_tokens` | Status exists in both teacher and user |
| `DELETE /api/super-admin/teachers/{teacherId}` | `TeacherController` | `TeacherService.deleteTeacher` | `teachers`, `users` | `teachers`, `users`, `operational_records`, `refresh_tokens` | Soft-delete both teacher and user |

### Student

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/super-admin/students` | `StudentController` | `StudentService.getStudents` | `students`, `branches` | None | Search is student-driven |
| `GET /api/super-admin/students/{studentId}` | `StudentController` | `StudentService.getStudent` | `students`, `users`, `branches` | None | Reads student plus linked user |
| `POST /api/super-admin/students` | `StudentController` | `StudentService.createStudent` | `students`, `users`, `roles`, `branches`, `batches` | `users`, `user_roles`, `students`, optional `student_enrolments`, optional `enrolment_subjects`, optional `enrolment_instalments`, `operational_records` | Can also create enrolment in the same call |
| `PUT /api/super-admin/students/{studentId}` | `StudentController` | `StudentService.updateStudent` | `students`, `users`, `branches` | `users`, `students`, `operational_records`, `refresh_tokens` | Password update revokes sessions |
| `PATCH /api/super-admin/students/{studentId}/status` | `StudentController` | `StudentService.updateStudentStatus` | `students`, `users` | `students`, `users`, `operational_records`, `refresh_tokens` | Status exists in both student and user |
| `DELETE /api/super-admin/students/{studentId}` | `StudentController` | `StudentService.deleteStudent` | `students`, `users` | `students`, `users`, `operational_records`, `refresh_tokens` | Soft-delete both student and user |

### Enrolment

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `POST /api/enrolments` | `EnrolmentController` | `EnrolmentService.createEnrolment` | `students`, `batches`, `subjects`, `subject_groups`, `student_enrolments` | `student_enrolments`, `enrolment_subjects`, `enrolment_instalments`, `batches` | Also recalculates `batches.enrolled_count` |
| `GET /api/enrolments/{enrolmentId}` | `EnrolmentController` | `EnrolmentService.getEnrolment` | `student_enrolments`, `students`, `batches`, `courses`, `subjects`, `enrolment_instalments` | None | Full view response |
| `GET /api/enrolments/student/{studentId}` | `EnrolmentController` | `EnrolmentService.getEnrolmentsByStudent` | `student_enrolments`, `students`, `batches`, `courses`, `subjects`, `enrolment_instalments` | None | Student history view |
| `GET /api/enrolments/batch/{batchId}` | `EnrolmentController` | `EnrolmentService.getEnrolmentsByBatch` | `student_enrolments`, `students`, `batches`, `courses`, `subjects`, `enrolment_instalments` | None | Batch roster view |
| `GET /api/enrolments/branch/{branchId}` | `EnrolmentController` | `EnrolmentService.getEnrolmentsByBranchAndYear` | `student_enrolments`, `students`, `batches`, `courses`, `subjects`, `enrolment_instalments` | None | Branch overview filtered by academic year |
| `PUT /api/enrolments/{enrolmentId}` | `EnrolmentController` | `EnrolmentService.updateEnrolment` | `student_enrolments`, `subjects`, `subject_groups` | `student_enrolments`, `enrolment_subjects`, `enrolment_instalments`, `batches` | Replaces subject and instalment collections when provided |
| `DELETE /api/enrolments/{enrolmentId}` | `EnrolmentController` | `EnrolmentService.deleteEnrolment` | `student_enrolments`, `batches` | `student_enrolments`, `batches` | Soft-delete plus enrolled count refresh |

### Lead

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/super-admin/leads` | `LeadController` | `LeadService.getLeads` | `lead_inquiries`, `branches`, `courses`, `batches` | None | Search + summary counts |
| `GET /api/super-admin/leads/{leadId}` | `LeadController` | `LeadService.getLead` | `lead_inquiries`, `lead_follow_ups`, `lead_subjects`, `subjects` | None | Full follow-up history included |
| `POST /api/super-admin/leads` | `LeadController` | `LeadService.createLead` | `users`, `branches`, `courses`, `batches`, `subjects` | `lead_inquiries`, `lead_subjects`, `operational_records` | Creates lead and lead activity |
| `PUT /api/super-admin/leads/{leadId}` | `LeadController` | `LeadService.updateLead` | `lead_inquiries`, `branches`, `courses`, `batches`, `subjects`, `users` | `lead_inquiries`, `lead_subjects`, `operational_records` | Updates lead metadata and mappings |
| `PATCH /api/super-admin/leads/{leadId}/status` | `LeadController` | `LeadService.updateLeadStatus` | `lead_inquiries`, `users` | `lead_inquiries`, `lead_follow_ups`, `operational_records` | Status history is stored in follow-ups |
| `POST /api/super-admin/leads/{leadId}/convert-to-admission` | `LeadController` | `LeadService.convertToAdmission` | `lead_inquiries`, `branches`, optional `batches`, optional `subjects` | `students`, `lead_inquiries`, optional `student_enrolments`, optional `enrolment_subjects`, optional `enrolment_instalments`, `operational_records` | Lead conversion can create both student and enrolment |
| `DELETE /api/super-admin/leads/{leadId}` | `LeadController` | `LeadService.deleteLead` | `lead_inquiries` | `lead_inquiries`, `operational_records` | Soft-delete plus lead activity |

### Dashboard

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/super-admin/dashboard` | `DashboardController` | `DashboardService.getDashboard` | `branches`, `students`, `teachers`, `users`, `operational_records` | None | Pure reporting endpoint |

Dashboard is fully derived from read models and audit-like records:

- student/admin/teacher totals come from `students`, `users`, `teachers`
- fee, attendance, lead, activity, class, test, feedback widgets come from `operational_records`

### Analytics

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/super-admin/analytics` | `AnalyticsController` | `AnalyticsService.getAnalytics` | `branches`, `students`, `teachers`, `users`, `operational_records` | None | Pure reporting endpoint |

### Reports

| API | Controller | Service Method | Reads | Writes | Notes |
|---|---|---|---|---|---|
| `GET /api/super-admin/reports/summary` | `ReportController` | `ReportService.getSummary` | `generated_reports`, `students`, `teachers`, `operational_records` | None | Summary + recent reports |
| `GET /api/super-admin/reports/categories` | `ReportController` | `ReportService.getCategories` | None | None | Static definitions |
| `GET /api/super-admin/reports` | `ReportController` | `ReportService.searchReports` | `generated_reports`, `branches` | None | Search and paging |
| `POST /api/super-admin/reports/export` | `ReportController` | `ReportService.generateReport` | `users`, `branches` | `generated_reports` | Metadata row is saved first |
| `GET /api/super-admin/reports/{reportId}/download` | `ReportController` | `ReportService.downloadReport` | `generated_reports`, plus source tables depending on report type | None | File is generated on demand |
| `DELETE /api/super-admin/reports/{reportId}` | `ReportController` | `ReportService.deleteReport` | `generated_reports` | `generated_reports` | Soft-delete report metadata row |

Report data sources by category:

- student reports: `students`
- teacher reports: `teachers`
- admission reports: `operational_records`
- attendance reports: `operational_records`
- academic and exam reports: `operational_records`
- financial reports: `operational_records`

## Straightforward Flow Recommendation

For the biggest pain points, this is the simpler rule set the project should adopt next:

### Course

- `course` should mean the `courses` table only
- `batch` should mean the `batches` table only
- if UI needs both together, create a separate "course-batch view" endpoint
- `GET /courses/{id}` should not fail because of missing batch unless the API is explicitly batch-scoped

### Student Creation

- student account creation should stay separate from enrolment creation
- if combined creation is kept, expose that explicitly as `createStudentWithEnrolment`

### Lead Conversion

- converting lead to student should be one service
- converting student into enrolment should be another service
- the orchestration endpoint can still call both, but the internals should remain separate

### Reporting

- reporting modules should remain read-only
- reporting queries should not contain hidden business-state changes

## Recommended Migration Order

To reduce risk, continue in this order:

1. auth
2. branch
3. admin
4. teacher
5. student
6. enrolment
7. lead
8. dashboard
9. analytics
10. reports

## Immediate Next Refactors

The next cleanup that will give the biggest clarity benefit:

1. split `student` and `enrolment` into separate feature packages
2. split `lead` and `admission conversion` into separate services
3. split `admin`, `teacher`, and `student` out of `super-admin` role buckets into feature modules
4. introduce dedicated read endpoints for combined views instead of forcing single-resource endpoints to hop tables
