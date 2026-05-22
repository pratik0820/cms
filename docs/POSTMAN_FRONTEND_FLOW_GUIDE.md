# Class Management System Backend - Postman and Frontend Flow Guide

This document explains how to test the Phase 1 backend step by step in Postman and how the frontend team should consume the APIs.

Base URL:

```text
http://localhost:8095/cms
```

Swagger:

```text
http://localhost:8095/cms/swagger-ui.html
```

## 1. Postman Environment

Create a Postman environment named `CMS Local` with these variables:

| Variable | Initial value |
| --- | --- |
| `baseUrl` | `http://localhost:8095/cms` |
| `tenantSubdomain` | `greenfield` |
| `tenantId` | empty |
| `accessToken` | empty |
| `refreshToken` | empty |
| `branchId` | empty |
| `batchId` | empty |
| `teacherId` | empty |
| `studentId` | empty |
| `studentLoginId` | empty |
| `studentPassword` | empty |
| `classSessionId` | empty |
| `leadId` | empty |
| `testId` | empty |

For all authenticated requests add this header:

```http
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

Common response envelope:

```json
{
  "success": true,
  "message": "Message",
  "data": {},
  "timestamp": "2026-05-03T..."
}
```

## 2. Start Backend

Run from project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Before testing, confirm:

```http
GET {{baseUrl}}/actuator/health
```

## 3. Register Institute

Public endpoint. This creates tenant, default branch, and institute owner login.

```http
POST {{baseUrl}}/api/tenants/register
```

Body:

```json
{
  "instituteName": "Greenfield Classes",
  "subdomain": "greenfield",
  "adminEmail": "owner@greenfield.com",
  "adminPassword": "Password@123",
  "adminName": "Institute Owner",
  "contactPhone": "9876543210",
  "planType": "STARTER"
}
```

Save from response:

```text
data.id -> tenantId
```

Postman test script:

```javascript
const json = pm.response.json();
if (json.data?.id) pm.environment.set("tenantId", json.data.id);
```

## 4. Login as Owner/Admin

```http
POST {{baseUrl}}/api/auth/login
```

Body:

```json
{
  "identifier": "owner@greenfield.com",
  "password": "Password@123",
  "tenantSubdomain": "{{tenantSubdomain}}"
}
```

Backward compatible body also works for staff:

```json
{
  "email": "owner@greenfield.com",
  "password": "Password@123",
  "tenantSubdomain": "{{tenantSubdomain}}"
}
```

Save tokens:

```javascript
const json = pm.response.json();
pm.environment.set("accessToken", json.data.accessToken);
pm.environment.set("refreshToken", json.data.refreshToken);
if (json.data.user?.branchId) pm.environment.set("branchId", json.data.user.branchId);
```

## 5. Auth Smoke Tests

Current user:

```http
GET {{baseUrl}}/api/auth/me
```

Refresh token:

```http
POST {{baseUrl}}/api/auth/refresh
```

Body:

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

## 6. Branch Setup

List branches:

```http
GET {{baseUrl}}/api/branches
```

Create second branch:

```http
POST {{baseUrl}}/api/branches
```

Body:

```json
{
  "name": "Baner",
  "address": "Baner Road",
  "city": "Pune",
  "phone": "9876543211",
  "email": "baner@greenfield.com"
}
```

Batch creation is now part of the current academic course flow in section `6A` and no longer uses the removed `/api/batches` endpoint.

## 6A. Current Academic Screens Flow

The current frontend screens for Standards & Boards, Courses, and View Course should use the new screen-shaped APIs under `/api/academic`.

List available boards:

```http
GET {{baseUrl}}/api/academic/boards
```

Create a standard:

```http
POST {{baseUrl}}/api/academic/standards
```

Body:

```json
{
  "standard": "8th",
  "board": "SSC"
}
```

List standards for the table:

```http
GET {{baseUrl}}/api/academic/standards?page=0&size=10
```

Get standard dropdown options for Add New Course:

```http
GET {{baseUrl}}/api/academic/standards/options
```

Create a course row for the current UI:

```http
POST {{baseUrl}}/api/academic/courses?branchId={{branchId}}
```

Body:

```json
{
  "standard": "8th",
  "board": "SSC",
  "medium": "Marathi",
  "academicYear": "2026-2027",
  "courseName": "8th SSC",
  "batchName": "Morning Batch",
  "batchTiming": "MORNING",
  "startTime": "07:30:00",
  "endTime": "11:30:00"
}
```

List courses for the grid:

```http
GET {{baseUrl}}/api/academic/courses?branchId={{branchId}}&page=0&size=10
```

Save the UUID from `data.items[0].id` when you need to open the View Course page.

Get course detail:

```http
GET {{baseUrl}}/api/academic/courses/{{batchId}}?branchId={{branchId}}
```

Add a subject from the View Course modal:

```http
POST {{baseUrl}}/api/academic/courses/{{batchId}}/subjects?branchId={{branchId}}
```

Body:

```json
{
  "subjectName": "Mathematics"
}
```

Important:

- Do not send `category`, `description`, `sortOrder`, `subjectGroups`, `room`, `classTeacherId`, `daysOfWeek`, `startDate`, `endDate`, or other non-visible fields from these screens.
- Use `id` for API actions and the returned `courseId` / `standardId` / `subjectId` only for display in the table.

## 7. Teacher Flow

Create teacher:

```http
POST {{baseUrl}}/api/users/teachers
```

Body:

```json
{
  "fullName": "Rahul Sharma",
  "email": "rahul.sharma@greenfield.com",
  "phone": "9876543212",
  "qualification": "M.Sc., B.Ed.",
  "courseIds": ["{{courseId}}"],
  "subjectIds": ["{{subjectId}}"],
  "batchIds": ["{{batchId}}"],
  "joiningDate": "2026-05-01",
  "hourlyRate": 500,
  "branchId": "{{branchId}}"
}
```

Teacher mapping rule for the current academic setup:

- frontend selects one or more courses first
- for each selected course, call `GET /api/academic/courses/{courseId}` and read `data.subjects`
- send selected subject UUIDs in `subjectIds`
- optionally send assigned batches in `batchIds`
- selected batches must belong to the same branch and selected course set

Save:

```javascript
const json = pm.response.json();
pm.environment.set("teacherId", json.data.id);
```

List teachers:

```http
GET {{baseUrl}}/api/users/teachers?page=0&size=20
```

Teacher class IN:

```http
POST {{baseUrl}}/api/teacher/classes/start
```

Body:

```json
{
  "title": "Maths - Algebra",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}",
  "teacherId": "{{teacherId}}",
  "type": "LECTURE",
  "details": {
    "subject": "Maths",
    "chapter": "Algebra",
    "subtopics": ["Linear equations", "Practice problems"]
  }
}
```

Save:

```javascript
const json = pm.response.json();
pm.environment.set("classSessionId", json.data.id);
```

Teacher class OUT:

```http
POST {{baseUrl}}/api/teacher/classes/{{classSessionId}}/end
```

Body:

```json
{
  "description": "Students understood basic equations well.",
  "details": {
    "coveredTopics": ["Linear equations", "Practice problems"],
    "lastLectureTopicsCovered": "Linear equations and practice set 1"
  }
}
```

Teacher analytics:

```http
GET {{baseUrl}}/api/teacher/{{teacherId}}/analytics
```

## 8. Student Admission Flow

Create student draft:

```http
POST {{baseUrl}}/api/users/students
```

Body:

```json
{
  "name": "Aarav Singh",
  "dateOfBirth": "2012-03-14",
  "gender": "Male",
  "mobile": "9876543213",
  "parentName": "Rahul Singh",
  "parentPhone": "9876543214",
  "email": "aarav@example.com",
  "address": "21 Main Street, Pune",
  "schoolName": "Greenfield Public School",
  "standard": "9th",
  "board": "CBSE",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}"
}
```

Save:

```javascript
const json = pm.response.json();
pm.environment.set("studentId", json.data.id);
pm.environment.set("studentLoginId", json.data.generatedLoginId);
pm.environment.set("studentPassword", json.data.generatedPassword);
```

Upload student photo:

```http
POST {{baseUrl}}/api/users/students/{{studentId}}/photo
```

Postman body:

```text
Body -> form-data
Key: photo
Type: File
Value: select JPG/PNG/WebP file
```

Do not manually set `Content-Type`; Postman will set multipart boundary.

Finalize admission:

```http
POST {{baseUrl}}/api/users/students/{{studentId}}/finalise
```

Expected: `isAdmissionFinal: true`.

List students:

```http
GET {{baseUrl}}/api/users/students?page=0&size=20
```

Student/parent login using generated login ID:

```http
POST {{baseUrl}}/api/auth/login
```

Body:

```json
{
  "identifier": "{{studentLoginId}}",
  "password": "{{studentPassword}}",
  "tenantSubdomain": "{{tenantSubdomain}}"
}
```

## 9. Lead and Admission Inquiry Flow

Create inquiry:

```http
POST {{baseUrl}}/api/leads
```

Body:

```json
{
  "title": "Aarav Singh inquiry",
  "status": "OPEN",
  "branchId": "{{branchId}}",
  "source": "Parent referral",
  "details": {
    "studentName": "Aarav Singh",
    "parentName": "Rahul Singh",
    "phoneNumber": "9876543214",
    "email": "parent@example.com",
    "standard": "9th",
    "school": "Greenfield Public School",
    "subjectsInterested": ["Maths", "Science"],
    "sourceOfInquiry": "Parent referral"
  }
}
```

Save:

```javascript
const json = pm.response.json();
pm.environment.set("leadId", json.data.id);
```

Convert lead:

```http
POST {{baseUrl}}/api/leads/{{leadId}}/convert
```

Body:

```json
{
  "details": {
    "studentId": "{{studentId}}",
    "feeStructureCreated": true,
    "subjectAllocation": ["Maths", "Science"]
  }
}
```

List leads:

```http
GET {{baseUrl}}/api/leads?page=0&size=20
```

## 10. Attendance Flow

Mark attendance:

```http
POST {{baseUrl}}/api/attendance
```

Body:

```json
{
  "title": "9th CBSE attendance",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}",
  "studentId": "{{studentId}}",
  "status": "PRESENT",
  "eventDate": "2026-05-03",
  "details": {
    "reasonForLeave": null,
    "markedBy": "ADMIN"
  }
}
```

Allowed statuses recommended for frontend:

```text
PRESENT, ABSENT, LEAVE
```

List attendance:

```http
GET {{baseUrl}}/api/attendance?branchId={{branchId}}&page=0&size=20
```

## 11. Timetable and Calendar Flow

Create timetable entry:

```http
POST {{baseUrl}}/api/timetables
```

Body:

```json
{
  "title": "Maths - 9th CBSE",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}",
  "teacherId": "{{teacherId}}",
  "eventDate": "2026-05-04",
  "startTime": "2026-05-04T16:30:00",
  "endTime": "2026-05-04T17:30:00",
  "details": {
    "subject": "Maths",
    "room": "Room 101",
    "repeat": "MONTHLY"
  }
}
```

List timetable:

```http
GET {{baseUrl}}/api/timetables?branchId={{branchId}}&page=0&size=50
```

Frontend usage:

- Admin calendar reads `/api/timetables`.
- Teacher dashboard filters records by `teacherId`.
- Student dashboard filters records by `batchId`.

## 12. Syllabus Flow

Upload syllabus structure:

```http
POST {{baseUrl}}/api/syllabus
```

Body:

```json
{
  "title": "Maths syllabus - 9th CBSE",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}",
  "details": {
    "subject": "Maths",
    "chapters": [
      {
        "name": "Algebra",
        "subtopics": ["Linear equations", "Practice problems"]
      },
      {
        "name": "Geometry",
        "subtopics": ["Triangles", "Circles"]
      }
    ]
  }
}
```

Update progress:

```http
POST {{baseUrl}}/api/syllabus/progress
```

Body:

```json
{
  "title": "Maths progress - Algebra",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}",
  "teacherId": "{{teacherId}}",
  "percentage": 65,
  "details": {
    "subject": "Maths",
    "chapter": "Algebra",
    "completedSubtopics": ["Linear equations"],
    "pendingSubtopics": ["Practice problems"]
  }
}
```

List progress:

```http
GET {{baseUrl}}/api/syllabus/progress?branchId={{branchId}}&page=0&size=50
```

## 13. Homework and Assignment Flow

Create homework:

```http
POST {{baseUrl}}/api/homeworks
```

Body:

```json
{
  "title": "Linear Equation Worksheet",
  "description": "Solve all questions.",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}",
  "teacherId": "{{teacherId}}",
  "eventDate": "2026-05-03",
  "status": "PUBLISHED",
  "details": {
    "subject": "Maths",
    "dueDate": "2026-05-10",
    "studentIds": ["{{studentId}}"],
    "allowLateSubmission": false
  }
}
```

List homework:

```http
GET {{baseUrl}}/api/homeworks?branchId={{branchId}}&page=0&size=20
```

Create assignment:

```http
POST {{baseUrl}}/api/assignments
```

Body:

```json
{
  "title": "Algebra Assignment 1",
  "description": "Complete assignment problems.",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}",
  "teacherId": "{{teacherId}}",
  "status": "PUBLISHED",
  "details": {
    "subject": "Maths",
    "dueDate": "2026-05-12",
    "studentIds": ["{{studentId}}"]
  }
}
```

## 14. Online MCQ Test Flow

Create MCQ test:

```http
POST {{baseUrl}}/api/tests
```

Body:

```json
{
  "title": "Maths Unit Test 1",
  "type": "MCQ",
  "branchId": "{{branchId}}",
  "batchId": "{{batchId}}",
  "teacherId": "{{teacherId}}",
  "eventDate": "2026-05-15",
  "maxScore": 50,
  "status": "PUBLISHED",
  "details": {
    "subject": "Maths",
    "durationMinutes": 45,
    "questions": [
      {
        "id": "q1",
        "question": "2 + 2 = ?",
        "options": ["2", "3", "4", "5"],
        "correctOption": "4",
        "marks": 5
      }
    ]
  }
}
```

Save:

```javascript
const json = pm.response.json();
pm.environment.set("testId", json.data.id);
```

Submit student attempt:

```http
POST {{baseUrl}}/api/student/tests/{{testId}}/attempts
```

Body:

```json
{
  "title": "Aarav - Maths Unit Test 1",
  "studentId": "{{studentId}}",
  "score": 45,
  "maxScore": 50,
  "percentage": 90,
  "status": "SUBMITTED",
  "details": {
    "answers": [
      {
        "questionId": "q1",
        "selectedOption": "4",
        "correct": true
      }
    ]
  }
}
```

Student performance:

```http
GET {{baseUrl}}/api/students/{{studentId}}/performance
```

## 15. Lecture Feedback Flow

Submit feedback as student:

```http
POST {{baseUrl}}/api/feedback/lecture
```

Body:

```json
{
  "title": "Feedback for Maths lecture",
  "studentId": "{{studentId}}",
  "teacherId": "{{teacherId}}",
  "subjectId": "{{classSessionId}}",
  "status": "SUBMITTED",
  "details": {
    "lectureId": "{{classSessionId}}",
    "understoodLecture": 5,
    "explanationClear": 5,
    "paceComfortable": 4,
    "doubtsSolved": 5,
    "overallQuality": 5,
    "remarks": "Very clear explanation."
  }
}
```

Admin/Super Admin list feedback:

```http
GET {{baseUrl}}/api/feedback/lecture?page=0&size=20
```

Important frontend rule:

- Do not show feedback data on teacher screens.
- Only Admin/Super Admin should call feedback list endpoint.

## 16. Fee Management Flow

Create fee record:

```http
POST {{baseUrl}}/api/fees
```

Body:

```json
{
  "title": "Aarav Singh annual fee",
  "studentId": "{{studentId}}",
  "branchId": "{{branchId}}",
  "amount": 50000,
  "paidAmount": 20000,
  "pendingAmount": 30000,
  "status": "PARTIAL",
  "details": {
    "installments": [
      {
        "name": "Installment 1",
        "amount": 20000,
        "paidDate": "2026-05-03"
      }
    ],
    "paymentMode": "UPI"
  }
}
```

List fees:

```http
GET {{baseUrl}}/api/fees?branchId={{branchId}}&page=0&size=20
```

Dashboard will use fee totals:

```http
GET {{baseUrl}}/api/dashboard/summary
```

## 17. Stationery Flow

Create stationery issue record:

```http
POST {{baseUrl}}/api/stationery/items
```

Body:

```json
{
  "title": "Aarav Singh stationery kit",
  "studentId": "{{studentId}}",
  "branchId": "{{branchId}}",
  "status": "PARTIAL",
  "details": {
    "items": [
      {
        "name": "Maths book",
        "status": "GIVEN"
      },
      {
        "name": "Bag",
        "status": "GIVEN"
      },
      {
        "name": "T-shirt",
        "status": "PENDING"
      }
    ]
  }
}
```

List stationery:

```http
GET {{baseUrl}}/api/stationery/items?branchId={{branchId}}&page=0&size=20
```

## 18. Announcements and Notifications

Create announcement:

```http
POST {{baseUrl}}/api/announcements
```

Body:

```json
{
  "title": "Parent Meeting",
  "description": "Parent meeting on Saturday.",
  "branchId": "{{branchId}}",
  "status": "PUBLISHED",
  "details": {
    "audience": ["STUDENT", "PARENT"],
    "category": "MEETING"
  }
}
```

Student announcement list:

```http
GET {{baseUrl}}/api/student/announcements?page=0&size=20
```

Notification inbox existing endpoints:

```http
GET {{baseUrl}}/api/v1/notifications?page=0&size=20
GET {{baseUrl}}/api/v1/notifications/unread-count
PATCH {{baseUrl}}/api/v1/notifications/{id}/read
PATCH {{baseUrl}}/api/v1/notifications/mark-all-read
```

WebSocket endpoint:

```text
/ws
```

Subscribe topic:

```text
/topic/user/{userId}/notifications
```

## 19. Dashboard and Reports

Dashboard summary:

```http
GET {{baseUrl}}/api/dashboard/summary
```

Branch dashboard summary:

```http
GET {{baseUrl}}/api/dashboard/summary?branchId={{branchId}}
```

Returned fields:

```json
{
  "totalStudents": 1,
  "totalTeachers": 1,
  "totalFeesCollected": 20000,
  "pendingFees": 30000,
  "activeClasses": 0,
  "attendanceRecords": 1,
  "openLeads": 0,
  "testsCreated": 1,
  "homeworkAssigned": 1,
  "stationeryIssued": 1
}
```

## 20. Generic Record Update/Delete

Many Phase 1 records use the same backing model. For edit screens, use:

```http
GET {{baseUrl}}/api/phase1/{id}
PUT {{baseUrl}}/api/phase1/{id}
PATCH {{baseUrl}}/api/phase1/{id}
DELETE {{baseUrl}}/api/phase1/{id}
```

Use the same body shape as create:

```json
{
  "title": "Updated title",
  "status": "ACTIVE",
  "details": {
    "anyModuleSpecificField": "value"
  }
}
```

## 21. Recommended Frontend Integration Order

1. Auth client:
   - Login with `identifier`
   - Store `accessToken` and `refreshToken`
   - Add bearer token to authenticated requests
   - Use `/api/auth/me` for route guards

2. Tenant setup:
   - Register institute
   - Load branches and batches

3. Admin portal:
   - Teacher CRUD basics
   - Student admission flow with mandatory photo
   - Leads/inquiries
   - Attendance
   - Fees
   - Stationery
   - Dashboard

4. Teacher portal:
   - Timetable list
   - Class IN/OUT
   - Homework/assignments
   - Syllabus progress
   - Test marks/attempt results
   - Student remarks

5. Student portal:
   - Login using generated `loginId`
   - View attendance, tests, homework, assignments, syllabus progress, announcements
   - Submit MCQ attempts and lecture feedback

6. Parent portal:
   - Login using same student `loginId`
   - Show child attendance, tests, fees, syllabus progress, teacher remarks, notifications

## 22. Frontend Data Model Notes

`Phase1RecordResponse` is intentionally flexible for Phase 1. Common fields:

```json
{
  "id": "uuid",
  "module": "HOMEWORK",
  "type": "MCQ",
  "title": "Title",
  "description": "Description",
  "status": "PUBLISHED",
  "branchId": "uuid",
  "batchId": "uuid",
  "studentId": "uuid",
  "teacherId": "uuid",
  "eventDate": "2026-05-03",
  "startTime": "2026-05-03T16:30:00",
  "endTime": "2026-05-03T17:30:00",
  "durationMinutes": 60,
  "amount": 50000,
  "paidAmount": 20000,
  "pendingAmount": 30000,
  "score": 45,
  "maxScore": 50,
  "percentage": 90,
  "details": {}
}
```

Frontend should keep module-specific fields inside `details`.

Examples:

- Lead: `details.studentName`, `details.sourceOfInquiry`
- Syllabus: `details.chapters`
- Homework: `details.dueDate`, `details.studentIds`
- Test: `details.questions`
- Feedback: `details.overallQuality`, `details.remarks`
- Stationery: `details.items`

## 23. Common Issues

`400 Invalid email or password`

- Check `tenantSubdomain`.
- For student login, use `identifier`, not `email`.
- Use generated `studentLoginId` and `studentPassword` from student creation response.

`403 Forbidden`

- Logged-in role is not allowed for that endpoint.
- Admin-only endpoints should not be called from teacher/student screens.

Admission finalise fails with `PHOTO_REQUIRED`

- Upload photo first using multipart form-data.

No branches/batches found

- Register tenant first.
- Create branch or use the default branch returned by login.
- Create batch before creating students.

## 24. Minimal Happy Path Checklist

Use this order to prove the whole backend flow:

1. Register tenant.
2. Login owner.
3. List branches and save `branchId`.
4. Create batch and save `batchId`.
5. Create teacher and save `teacherId`.
6. Create student and save `studentId`, `studentLoginId`, `studentPassword`.
7. Upload student photo.
8. Finalise admission.
9. Create lead and convert it.
10. Mark attendance.
11. Create timetable.
12. Start class.
13. End class.
14. Upload syllabus.
15. Update syllabus progress.
16. Assign homework.
17. Create assignment.
18. Create MCQ test.
19. Login as student with `studentLoginId`.
20. Submit test attempt.
21. Submit lecture feedback.
22. Login back as owner/admin.
23. Create fee record.
24. Create stationery record.
25. Create announcement.
26. Check dashboard summary.
27. Check student performance.
28. Check teacher analytics.
