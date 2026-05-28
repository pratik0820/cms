# Simple Data Flow

## Goal

This document explains the current table flow in a short, straightforward way and proposes a simpler `course` and `batch` model.

## 1. Course Creation

### Current flow

Request enters:

- `AcademicManagementController.createCourse`
- `AcademicManagementService.createCourse`

Tables involved:

1. Read `standards`
   - validate selected standard and board
2. Read `branches`
   - resolve branch for the course
3. Insert into `courses`
   - creates the academic course master row
4. Insert into `batches`
   - creates the running batch row for that course

### Current meaning

- `courses` = academic definition
- `batches` = actual running instance for branch, timing, year

### Why it feels confusing

One API says “create course” but actually creates:

- one `course`
- one `batch`

So functionally it is already a combined flow.

## 2. Batch Creation

### Current flow

Right now batch is effectively created inside course creation in the academic module.

Tables involved:

1. Read `courses`
2. Read `branches`
3. Insert into `batches`

Important batch data stored separately:

- `branch_id`
- `course_id`
- `academic_year`
- `timing`
- `start_time`
- `end_time`
- `enrolled_count`

### Why batch exists at all

Because the same course may run:

- in multiple branches
- in different timings
- in different academic years
- with different student counts

## 3. Subject Mapping

### Global subject creation and usage

Tables involved:

1. Insert into `subjects`
   - master subject row like Maths, Science, English
2. Insert into `course_subjects`
   - links a subject to a course

### Meaning

- `subjects` = subject catalog
- `course_subjects` = which subjects belong to which course

### Why separate

Because one subject can belong to many courses.

Example:

- Maths can belong to CBSE 8
- Maths can belong to SSC 8
- Maths can belong to Foundation batch

If subject was stored directly in `courses`, the same subject would be duplicated many times.

## 4. Subject Group Mapping

### Current purpose

Subject groups are preset combinations for enrolment selection.

Example:

- PCM
- PCB
- Commerce with Maths

Tables involved:

1. `subject_groups`
   - group header
2. `subject_group_subjects`
   - default subjects inside the group
3. `subject_group_extra_subjects`
   - optional extra subjects allowed with that group

### Meaning

- `subject_groups` = group name
- `subject_group_subjects` = main/default subjects
- `subject_group_extra_subjects` = optional extras

## 5. Lead to Admission

### Current flow

Request enters:

- `SuperAdminLeadController.convertLeadToAdmission`
- `LeadManagementService.convertToAdmission`

Tables involved:

1. Read `lead_inquiries`
2. Read `branches`
3. Insert into `students`
4. Update `lead_inquiries`
   - mark converted
   - attach converted student
5. Insert into `operational_records`
   - activity log
6. Optional enrolment:
   - insert into `student_enrolments`
   - insert into `enrolment_subjects`
   - insert into `enrolment_instalments`
   - update `batches.enrolled_count`

### Why it feels large

Because one “convert lead” action currently bundles:

- student creation
- lead status update
- optional enrolment creation
- fee setup

That is convenient for UI, but heavy internally.

## 6. Student Enrolment

### Current flow

Request enters:

- `StudentEnrolmentController.createEnrolment`
- `StudentEnrolmentService.createEnrolment`

Tables involved:

1. Read `students`
2. Read `batches`
3. Read `subjects`
4. Optional read `subject_groups`
5. Insert into `student_enrolments`
6. Insert into `enrolment_subjects`
7. Insert into `enrolment_instalments`
8. Update `batches.enrolled_count`

### Meaning

- `student_enrolments` = student joined a batch
- `enrolment_subjects` = selected subjects for that student
- `enrolment_instalments` = fee payment schedule

## 7. Teacher Subject Mapping

Tables involved:

1. Read `teachers`
2. Read `subjects`
3. Insert into `teacher_subjects` or assignment mapping

### Meaning

- teacher can teach many subjects
- same subject can be taught by many teachers

So it is many-to-many mapping.

## 8. Should Batch Be Moved Into Course Table?

### Short answer

Yes, only if your business rule is truly:

- one course always has exactly one batch
- no separate batch lifecycle is needed
- no multi-branch or multi-timing reuse is needed
- no multiple academic-year runs under one course are needed

If that is your real business rule, then batch should not be separate.

### But in the current system

Batch is being used for:

- branch-specific running instance
- timing
- academic year
- enrolled count
- enrolment target
- lead conversion target
- teacher assignment target

So technically the current codebase is built around:

- `course` = template/master
- `batch` = actual offering

### Simplest recommendation

Do not fully remove `batches` right now.

Instead make the workflow simple like this:

1. Keep `batches` table in database
2. Remove separate batch management from UI and API surface
3. Let `create course` always create exactly one batch internally
4. If user does not send batch name, create default batch:
   - `"Morning Batch"`
5. Let `update course` also update its single linked batch
6. Do not expose batch as a separate concept unless future business really needs it

### Best practical model

For your current simplicity goal:

- keep `batches` in DB
- hide `batches` in API design
- treat batch as an internal child of course

That gives:

- simple frontend flow
- less refactor risk
- keeps enrolment and lead logic working

## 9. Straightforward Flow I Recommend

### Course API

`POST /courses`

Payload:

- course details
- optional batch details

Internal behavior:

1. create `courses`
2. create one `batches`
   - default to Morning Batch if missing
3. return combined response

### Course detail API

`GET /courses/{courseId}`

Internal behavior:

1. read `courses`
2. read its single active `batch`
3. read subjects
4. return one combined object

### Student enrolment API

Still use batch internally, but UI can simply show:

- Course
- Batch timing/name auto-derived from selected course

## 10. Final Recommendation

If your real goal is simple straight flow, the best option is:

- keep database `batches`
- stop treating batch as a separate user-facing module
- make batch an internal child of course
- always create one default batch with course

This gives the simplicity you want without breaking the rest of the system design immediately.
