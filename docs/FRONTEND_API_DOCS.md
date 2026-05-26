# Complete API Documentation

## GET /api/super-admin/teachers/{teacherId}

**Summary:** Get a single teacher profile

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `teacherId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "email": string,
    "phone": string,
    "loginId": string,
    "dateOfBirth": string,
    "gender": string,
    "profilePhotoUrl": string,
    "qualification": string,
    "experienceYears": integer,
    "subjects": Array<string>
,
    "subjectIds": Array<string>
,
    "specialization": string,
    "joiningDate": string,
    "employmentType": string,
    "salaryType": string,
    "hourlyRate": number,
    "address": string,
    "branchId": string,
    "branchName": string,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/super-admin/teachers/{teacherId}

**Summary:** Update an existing teacher profile

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `teacherId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "fullName": string,
  "email": string,
  "phone": string,
  "dateOfBirth": string,
  "gender": string,
  "profilePhotoUrl": string,
  "qualification": string,
  "experienceYears": integer,
  "subjects": Array<string>
,
  "subjectIds": Array<string>
,
  "specialization": string,
  "joiningDate": string,
  "employmentType": string,
  "salaryType": string,
  "hourlyRate": number,
  "loginId": string,
  "password": string,
  "confirmPassword": string,
  "branchId": string,
  "address": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "email": string,
    "phone": string,
    "loginId": string,
    "dateOfBirth": string,
    "gender": string,
    "profilePhotoUrl": string,
    "qualification": string,
    "experienceYears": integer,
    "subjects": Array<string>
,
    "subjectIds": Array<string>
,
    "specialization": string,
    "joiningDate": string,
    "employmentType": string,
    "salaryType": string,
    "hourlyRate": number,
    "address": string,
    "branchId": string,
    "branchName": string,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/super-admin/teachers/{teacherId}

**Summary:** Soft delete a teacher account

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `teacherId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/students/{studentId}

**Summary:** Get a single student profile

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `studentId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "studentId": string,
    "branchId": string,
    "branchName": string,
    "standard": string,
    "batch": string,
    "gender": string,
    "dateOfBirth": string,
    "mobile": string,
    "parentName": string,
    "parentPhone": string,
    "email": string,
    "address": string,
    "schoolName": string,
    "board": Enum(SSC | CBSE | ICSE | HSC),
    "admissionDate": string,
    "loginId": string,
    "profilePhotoUrl": string,
    "isAdmissionFinal": boolean,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/super-admin/students/{studentId}

**Summary:** Update an existing student profile

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `studentId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "fullName": string,
  "studentId": string,
  "branchId": string,
  "standard": string,
  "batch": string,
  "gender": string,
  "dateOfBirth": string,
  "mobile": string,
  "parentName": string,
  "parentPhone": string,
  "email": string,
  "address": string,
  "schoolName": string,
  "board": string,
  "admissionDate": string,
  "loginId": string,
  "password": string,
  "confirmPassword": string,
  "profilePhotoUrl": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "studentId": string,
    "branchId": string,
    "branchName": string,
    "standard": string,
    "batch": string,
    "gender": string,
    "dateOfBirth": string,
    "mobile": string,
    "parentName": string,
    "parentPhone": string,
    "email": string,
    "address": string,
    "schoolName": string,
    "board": Enum(SSC | CBSE | ICSE | HSC),
    "admissionDate": string,
    "loginId": string,
    "profilePhotoUrl": string,
    "isAdmissionFinal": boolean,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/super-admin/students/{studentId}

**Summary:** Soft delete a student account

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `studentId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/leads/{leadId}

**Summary:** Get lead detail

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `leadId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "leadCode": string,
    "studentName": string,
    "gender": string,
    "dateOfBirth": string,
    "bloodGroup": string,
    "classInterestedIn": string,
    "board": string,
    "medium": string,
    "stream": string,
    "currentSchool": string,
    "lastClassCompleted": string,
    "lastExamPercentage": string,
    "address": string,
    "mobileNumber": string,
    "alternateMobileNumber": string,
    "email": string,
    "fatherName": string,
    "motherName": string,
    "guardianName": string,
    "relation": string,
    "fatherMobileNumber": string,
    "motherMobileNumber": string,
    "guardianMobileNumber": string,
    "parentEmail": string,
    "fatherOccupation": string,
    "motherOccupation": string,
    "annualIncome": string,
    "nationality": string,
    "leadSource": string,
    "referredBy": string,
    "heardAboutUs": string,
    "preferredBranchId": string,
    "preferredBranchName": string,
    "courseId": string,
    "courseName": string,
    "batchId": string,
    "batchName": string,
    "subjects": Array<{
      "id": string,
      "code": string,
      "displayName": string,
      "shortName": string,
      "description": string,
      "sortOrder": integer,
      "isActive": boolean,
    }>
,
    "expectedAdmissionYear": string,
    "preferredAdmissionDate": string,
    "preferredContactTime": string,
    "modeOfContact": string,
    "bestDaysToContact": string,
    "courseRecommended": string,
    "batchSuggested": string,
    "admissionLikelihood": string,
    "remarks": string,
    "nextFollowUpAt": string,
    "status": string,
    "assignedToUserId": string,
    "assignedToName": string,
    "convertedStudentId": string,
    "convertedAt": string,
    "followUps": Array<{
      "id": string,
      "followUpAt": string,
      "modeOfContact": string,
      "notes": string,
      "nextFollowUpAt": string,
      "statusAfter": string,
      "createdByName": string,
    }>
,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/super-admin/leads/{leadId}

**Summary:** Update a lead inquiry

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `leadId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "studentName": string,
  "gender": string,
  "dateOfBirth": string,
  "bloodGroup": string,
  "classInterestedIn": string,
  "board": string,
  "medium": string,
  "stream": string,
  "currentSchool": string,
  "lastClassCompleted": string,
  "lastExamPercentage": string,
  "address": string,
  "mobileNumber": string,
  "alternateMobileNumber": string,
  "email": string,
  "fatherName": string,
  "motherName": string,
  "guardianName": string,
  "relation": string,
  "fatherMobileNumber": string,
  "motherMobileNumber": string,
  "guardianMobileNumber": string,
  "parentEmail": string,
  "fatherOccupation": string,
  "motherOccupation": string,
  "annualIncome": string,
  "nationality": string,
  "leadSource": string,
  "referredBy": string,
  "heardAboutUs": string,
  "preferredBranchId": string,
  "courseId": string,
  "batchId": string,
  "subjectIds": Array<string>
,
  "expectedAdmissionYear": string,
  "preferredAdmissionDate": string,
  "preferredContactTime": string,
  "modeOfContact": string,
  "bestDaysToContact": string,
  "courseRecommended": string,
  "batchSuggested": string,
  "admissionLikelihood": string,
  "remarks": string,
  "nextFollowUpAt": string,
  "assignedToUserId": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "leadCode": string,
    "studentName": string,
    "gender": string,
    "dateOfBirth": string,
    "bloodGroup": string,
    "classInterestedIn": string,
    "board": string,
    "medium": string,
    "stream": string,
    "currentSchool": string,
    "lastClassCompleted": string,
    "lastExamPercentage": string,
    "address": string,
    "mobileNumber": string,
    "alternateMobileNumber": string,
    "email": string,
    "fatherName": string,
    "motherName": string,
    "guardianName": string,
    "relation": string,
    "fatherMobileNumber": string,
    "motherMobileNumber": string,
    "guardianMobileNumber": string,
    "parentEmail": string,
    "fatherOccupation": string,
    "motherOccupation": string,
    "annualIncome": string,
    "nationality": string,
    "leadSource": string,
    "referredBy": string,
    "heardAboutUs": string,
    "preferredBranchId": string,
    "preferredBranchName": string,
    "courseId": string,
    "courseName": string,
    "batchId": string,
    "batchName": string,
    "subjects": Array<{
      "id": string,
      "code": string,
      "displayName": string,
      "shortName": string,
      "description": string,
      "sortOrder": integer,
      "isActive": boolean,
    }>
,
    "expectedAdmissionYear": string,
    "preferredAdmissionDate": string,
    "preferredContactTime": string,
    "modeOfContact": string,
    "bestDaysToContact": string,
    "courseRecommended": string,
    "batchSuggested": string,
    "admissionLikelihood": string,
    "remarks": string,
    "nextFollowUpAt": string,
    "status": string,
    "assignedToUserId": string,
    "assignedToName": string,
    "convertedStudentId": string,
    "convertedAt": string,
    "followUps": Array<{
      "id": string,
      "followUpAt": string,
      "modeOfContact": string,
      "notes": string,
      "nextFollowUpAt": string,
      "statusAfter": string,
      "createdByName": string,
    }>
,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/super-admin/leads/{leadId}

**Summary:** Soft delete a lead

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `leadId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/branches/{branchId}

**Summary:** Get a single branch

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "name": string,
    "address": string,
    "city": string,
    "phone": string,
    "email": string,
    "isActive": boolean,
    "totalStudents": integer,
    "totalTeachers": integer,
    "totalAdmins": integer,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/super-admin/branches/{branchId}

**Summary:** Update an existing branch

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "name": string,
  "address": string,
  "city": string,
  "phone": string,
  "email": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "name": string,
    "address": string,
    "city": string,
    "phone": string,
    "email": string,
    "isActive": boolean,
    "totalStudents": integer,
    "totalTeachers": integer,
    "totalAdmins": integer,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/super-admin/branches/{branchId}

**Summary:** Soft delete a branch

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/admins/{adminId}

**Summary:** Get a single admin profile

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `adminId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "email": string,
    "phone": string,
    "loginId": string,
    "dateOfBirth": string,
    "gender": string,
    "profilePhotoUrl": string,
    "role": string,
    "joiningDate": string,
    "accessLevel": string,
    "address": string,
    "allBranchesAccess": boolean,
    "branchId": string,
    "branchName": string,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/super-admin/admins/{adminId}

**Summary:** Update an existing admin profile

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `adminId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "fullName": string,
  "email": string,
  "phone": string,
  "dateOfBirth": string,
  "gender": string,
  "profilePhotoUrl": string,
  "loginId": string,
  "password": string,
  "confirmPassword": string,
  "role": string,
  "joiningDate": string,
  "accessLevel": string,
  "branchId": string,
  "address": string,
  "allBranchesAccess": boolean,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "email": string,
    "phone": string,
    "loginId": string,
    "dateOfBirth": string,
    "gender": string,
    "profilePhotoUrl": string,
    "role": string,
    "joiningDate": string,
    "accessLevel": string,
    "address": string,
    "allBranchesAccess": boolean,
    "branchId": string,
    "branchName": string,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/super-admin/admins/{adminId}

**Summary:** Soft delete an admin account

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `adminId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/enrolments/{enrolmentId}

**Summary:** Get enrolment details

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `enrolmentId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "studentId": string,
    "studentName": string,
    "studentLoginId": string,
    "batchId": string,
    "batchName": string,
    "academicYear": string,
    "courseId": string,
    "courseName": string,
    "courseCode": string,
    "subjectGroupId": string,
    "subjectGroupName": string,
    "subjects": Array<{
      "id": string,
      "code": string,
      "displayName": string,
      "shortName": string,
      "description": string,
      "sortOrder": integer,
      "isActive": boolean,
    }>
,
    "agreedTotalFee": number,
    "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
    "totalPaid": number,
    "totalPending": number,
    "instalments": Array<{
      "id": string,
      "instalmentNumber": integer,
      "label": string,
      "amount": number,
      "dueDate": string,
      "isPostDatedCheque": boolean,
      "isPaid": boolean,
      "paidDate": string,
    }>
,
    "enrolmentDate": string,
    "status": Enum(ACTIVE | COMPLETED | WITHDRAWN | TRANSFERRED | SUSPENDED),
    "notes": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/enrolments/{enrolmentId}

**Summary:** Update enrolment â change subjects, fee, payment plan, instalments or status

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `enrolmentId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "subjectGroupId": string,
  "subjectIds": Array<string>
,
  "agreedTotalFee": number,
  "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
  "instalments": Array<{
    "instalmentNumber": integer,
    "label": string,
    "amount": number,
    "dueDate": string,
    "isPostDatedCheque": boolean,
  }>
,
  "status": Enum(ACTIVE | COMPLETED | WITHDRAWN | TRANSFERRED | SUSPENDED),
  "notes": string,
  "enrolmentDate": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "studentId": string,
    "studentName": string,
    "studentLoginId": string,
    "batchId": string,
    "batchName": string,
    "academicYear": string,
    "courseId": string,
    "courseName": string,
    "courseCode": string,
    "subjectGroupId": string,
    "subjectGroupName": string,
    "subjects": Array<{
      "id": string,
      "code": string,
      "displayName": string,
      "shortName": string,
      "description": string,
      "sortOrder": integer,
      "isActive": boolean,
    }>
,
    "agreedTotalFee": number,
    "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
    "totalPaid": number,
    "totalPending": number,
    "instalments": Array<{
      "id": string,
      "instalmentNumber": integer,
      "label": string,
      "amount": number,
      "dueDate": string,
      "isPostDatedCheque": boolean,
      "isPaid": boolean,
      "paidDate": string,
    }>
,
    "enrolmentDate": string,
    "status": Enum(ACTIVE | COMPLETED | WITHDRAWN | TRANSFERRED | SUSPENDED),
    "notes": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/enrolments/{enrolmentId}

**Summary:** Soft-delete an enrolment

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `enrolmentId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/auth/change-password

**Summary:** Change current user's password

### Request Body

```json
{
  "currentPassword": string,
  "newPassword": string,
  "confirmPassword": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/academic/standards/{standardId}

**Summary:** Update a standard from the Standards & Boards screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `standardId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "standard": string,
  "board": Enum(SSC | CBSE | ICSE | HSC),
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "standardId": string,
    "standard": string,
    "board": Enum(SSC | CBSE | ICSE | HSC),
    "boardLabel": string,
    "status": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/academic/standards/{standardId}

**Summary:** Delete a standard from the Standards & Boards screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `standardId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/academic/courses/{courseId}

**Summary:** Get course details and subjects for the View Course screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `courseId` | `path` | Yes | `string` |  |
| `branchId` | `query` | No | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "courseId": string,
    "standard": string,
    "board": string,
    "medium": string,
    "academicYear": string,
    "courseName": string,
    "batchName": string,
    "batchTiming": string,
    "startTime": {
      "hour": integer,
      "minute": integer,
      "second": integer,
      "nano": integer,
    },
    "endTime": {
      "hour": integer,
      "minute": integer,
      "second": integer,
      "nano": integer,
    },
    "subjects": Array<{
      "id": string,
      "subjectId": string,
      "subjectName": string,
      "subjectCode": string,
      "status": string,
    }>
,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/academic/courses/{courseId}

**Summary:** Update a course and its batch details from the Edit Course screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `courseId` | `path` | Yes | `string` |  |
| `branchId` | `query` | No | `string` |  |

### Request Body

```json
{
  "standard": string,
  "board": Enum(SSC | CBSE | ICSE | HSC),
  "medium": string,
  "academicYear": string,
  "courseName": string,
  "batchName": string,
  "batchTiming": Enum(MORNING | EVENING | CUSTOM),
  "startTime": {
    "hour": integer,
    "minute": integer,
    "second": integer,
    "nano": integer,
  },
  "endTime": {
    "hour": integer,
    "minute": integer,
    "second": integer,
    "nano": integer,
  },
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "courseId": string,
    "standard": string,
    "board": string,
    "medium": string,
    "academicYear": string,
    "courseName": string,
    "batchName": string,
    "batchTiming": string,
    "startTime": {
      "hour": integer,
      "minute": integer,
      "second": integer,
      "nano": integer,
    },
    "endTime": {
      "hour": integer,
      "minute": integer,
      "second": integer,
      "nano": integer,
    },
    "subjects": Array<{
      "id": string,
      "subjectId": string,
      "subjectName": string,
      "subjectCode": string,
      "status": string,
    }>
,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/academic/courses/{courseId}

**Summary:** Delete a course row from the Courses screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `courseId` | `path` | Yes | `string` |  |
| `branchId` | `query` | No | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PUT /api/academic/courses/{courseId}/subjects/{subjectId}

**Summary:** Update a subject name from the View Course screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `courseId` | `path` | Yes | `string` |  |
| `subjectId` | `path` | Yes | `string` |  |
| `branchId` | `query` | No | `string` |  |

### Request Body

```json
{
  "subjectName": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "subjectId": string,
    "subjectName": string,
    "subjectCode": string,
    "status": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/academic/courses/{courseId}/subjects/{subjectId}

**Summary:** Delete a subject from the View Course screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `courseId` | `path` | Yes | `string` |  |
| `subjectId` | `path` | Yes | `string` |  |
| `branchId` | `query` | No | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/teachers

**Summary:** Get teacher management summary and paginated teacher list

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `search` | `query` | No | `string` |  |
| `isActive` | `query` | No | `boolean` |  |
| `branchId` | `query` | No | `string` |  |
| `subject` | `query` | No | `string` |  |
| `subjectId` | `query` | No | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "summary": {
      "totalTeachers": integer,
      "activeTeachers": integer,
      "inactiveTeachers": integer,
    },
    "content": Array<{
      "id": string,
      "userId": string,
      "fullName": string,
      "email": string,
      "phone": string,
      "loginId": string,
      "dateOfBirth": string,
      "gender": string,
      "profilePhotoUrl": string,
      "qualification": string,
      "experienceYears": integer,
      "subjects": Array<string>
,
      "subjectIds": Array<string>
,
      "specialization": string,
      "joiningDate": string,
      "employmentType": string,
      "salaryType": string,
      "hourlyRate": number,
      "address": string,
      "branchId": string,
      "branchName": string,
      "isActive": boolean,
      "lastLoginAt": string,
      "createdAt": string,
      "updatedAt": string,
    }>
,
    "page": {
      "pageNumber": integer,
      "pageSize": integer,
      "totalElements": integer,
      "totalPages": integer,
      "first": boolean,
      "last": boolean,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/super-admin/teachers

**Summary:** Create a new teacher account

### Request Body

```json
{
  "fullName": string,
  "email": string,
  "phone": string,
  "dateOfBirth": string,
  "gender": string,
  "profilePhotoUrl": string,
  "qualification": string,
  "experienceYears": integer,
  "subjects": Array<string>
,
  "subjectIds": Array<string>
,
  "specialization": string,
  "joiningDate": string,
  "employmentType": string,
  "salaryType": string,
  "hourlyRate": number,
  "loginId": string,
  "password": string,
  "confirmPassword": string,
  "branchId": string,
  "address": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "email": string,
    "phone": string,
    "loginId": string,
    "dateOfBirth": string,
    "gender": string,
    "profilePhotoUrl": string,
    "qualification": string,
    "experienceYears": integer,
    "subjects": Array<string>
,
    "subjectIds": Array<string>
,
    "specialization": string,
    "joiningDate": string,
    "employmentType": string,
    "salaryType": string,
    "hourlyRate": number,
    "address": string,
    "branchId": string,
    "branchName": string,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/students

**Summary:** Get student management summary and paginated student list

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `search` | `query` | No | `string` |  |
| `isActive` | `query` | No | `boolean` |  |
| `branchId` | `query` | No | `string` |  |
| `standard` | `query` | No | `string` |  |
| `batch` | `query` | No | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "summary": {
      "totalTeachers": integer,
      "activeTeachers": integer,
      "inactiveTeachers": integer,
    },
    "content": Array<{
      "id": string,
      "userId": string,
      "fullName": string,
      "studentId": string,
      "branchId": string,
      "branchName": string,
      "standard": string,
      "batch": string,
      "gender": string,
      "dateOfBirth": string,
      "mobile": string,
      "parentName": string,
      "parentPhone": string,
      "email": string,
      "address": string,
      "schoolName": string,
      "board": Enum(SSC | CBSE | ICSE | HSC),
      "admissionDate": string,
      "loginId": string,
      "profilePhotoUrl": string,
      "isAdmissionFinal": boolean,
      "isActive": boolean,
      "lastLoginAt": string,
      "createdAt": string,
      "updatedAt": string,
    }>
,
    "page": {
      "pageNumber": integer,
      "pageSize": integer,
      "totalElements": integer,
      "totalPages": integer,
      "first": boolean,
      "last": boolean,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/super-admin/students

**Summary:** Create a new student account

### Request Body

```json
{
  "fullName": string,
  "studentId": string,
  "branchId": string,
  "standard": string,
  "batch": string,
  "courseId": string,
  "batchId": string,
  "subjectGroupId": string,
  "subjectIds": Array<string>
,
  "agreedTotalFee": number,
  "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
  "instalments": Array<{
    "instalmentNumber": integer,
    "label": string,
    "amount": number,
    "dueDate": string,
    "isPostDatedCheque": boolean,
  }>
,
  "enrolmentNotes": string,
  "gender": string,
  "dateOfBirth": string,
  "mobile": string,
  "parentName": string,
  "parentPhone": string,
  "email": string,
  "address": string,
  "schoolName": string,
  "board": string,
  "admissionDate": string,
  "loginId": string,
  "password": string,
  "confirmPassword": string,
  "profilePhotoUrl": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "studentId": string,
    "branchId": string,
    "branchName": string,
    "standard": string,
    "batch": string,
    "gender": string,
    "dateOfBirth": string,
    "mobile": string,
    "parentName": string,
    "parentPhone": string,
    "email": string,
    "address": string,
    "schoolName": string,
    "board": Enum(SSC | CBSE | ICSE | HSC),
    "admissionDate": string,
    "loginId": string,
    "profilePhotoUrl": string,
    "isAdmissionFinal": boolean,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/super-admin/reports/export

**Summary:** Generate a report and return its download metadata

### Request Body

```json
{
  "category": string,
  "reportType": string,
  "format": string,
  "branchId": string,
  "fromDate": string,
  "toDate": string,
  "standard": string,
  "batch": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "reportName": string,
    "category": string,
    "reportType": string,
    "format": string,
    "status": string,
    "downloadUrl": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/leads

**Summary:** Get lead summary and paginated lead list

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `search` | `query` | No | `string` |  |
| `branchId` | `query` | No | `string` |  |
| `status` | `query` | No | `string` |  |
| `leadSource` | `query` | No | `string` |  |
| `courseId` | `query` | No | `string` |  |
| `batchId` | `query` | No | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "summary": {
      "totalTeachers": integer,
      "activeTeachers": integer,
      "inactiveTeachers": integer,
    },
    "content": Array<{
      "id": string,
      "leadCode": string,
      "studentName": string,
      "gender": string,
      "dateOfBirth": string,
      "bloodGroup": string,
      "classInterestedIn": string,
      "board": string,
      "medium": string,
      "stream": string,
      "currentSchool": string,
      "lastClassCompleted": string,
      "lastExamPercentage": string,
      "address": string,
      "mobileNumber": string,
      "alternateMobileNumber": string,
      "email": string,
      "fatherName": string,
      "motherName": string,
      "guardianName": string,
      "relation": string,
      "fatherMobileNumber": string,
      "motherMobileNumber": string,
      "guardianMobileNumber": string,
      "parentEmail": string,
      "fatherOccupation": string,
      "motherOccupation": string,
      "annualIncome": string,
      "nationality": string,
      "leadSource": string,
      "referredBy": string,
      "heardAboutUs": string,
      "preferredBranchId": string,
      "preferredBranchName": string,
      "courseId": string,
      "courseName": string,
      "batchId": string,
      "batchName": string,
      "subjects": Array<{
        "id": string,
        "code": string,
        "displayName": string,
        "shortName": string,
        "description": string,
        "sortOrder": integer,
        "isActive": boolean,
      }>
,
      "expectedAdmissionYear": string,
      "preferredAdmissionDate": string,
      "preferredContactTime": string,
      "modeOfContact": string,
      "bestDaysToContact": string,
      "courseRecommended": string,
      "batchSuggested": string,
      "admissionLikelihood": string,
      "remarks": string,
      "nextFollowUpAt": string,
      "status": string,
      "assignedToUserId": string,
      "assignedToName": string,
      "convertedStudentId": string,
      "convertedAt": string,
      "followUps": Array<{
        "id": string,
        "followUpAt": string,
        "modeOfContact": string,
        "notes": string,
        "nextFollowUpAt": string,
        "statusAfter": string,
        "createdByName": string,
      }>
,
      "createdAt": string,
      "updatedAt": string,
    }>
,
    "page": {
      "pageNumber": integer,
      "pageSize": integer,
      "totalElements": integer,
      "totalPages": integer,
      "first": boolean,
      "last": boolean,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/super-admin/leads

**Summary:** Create a new lead inquiry

### Request Body

```json
{
  "studentName": string,
  "gender": string,
  "dateOfBirth": string,
  "bloodGroup": string,
  "classInterestedIn": string,
  "board": string,
  "medium": string,
  "stream": string,
  "currentSchool": string,
  "lastClassCompleted": string,
  "lastExamPercentage": string,
  "address": string,
  "mobileNumber": string,
  "alternateMobileNumber": string,
  "email": string,
  "fatherName": string,
  "motherName": string,
  "guardianName": string,
  "relation": string,
  "fatherMobileNumber": string,
  "motherMobileNumber": string,
  "guardianMobileNumber": string,
  "parentEmail": string,
  "fatherOccupation": string,
  "motherOccupation": string,
  "annualIncome": string,
  "nationality": string,
  "leadSource": string,
  "referredBy": string,
  "heardAboutUs": string,
  "preferredBranchId": string,
  "courseId": string,
  "batchId": string,
  "subjectIds": Array<string>
,
  "expectedAdmissionYear": string,
  "preferredAdmissionDate": string,
  "preferredContactTime": string,
  "modeOfContact": string,
  "bestDaysToContact": string,
  "courseRecommended": string,
  "batchSuggested": string,
  "admissionLikelihood": string,
  "remarks": string,
  "nextFollowUpAt": string,
  "assignedToUserId": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "leadCode": string,
    "studentName": string,
    "gender": string,
    "dateOfBirth": string,
    "bloodGroup": string,
    "classInterestedIn": string,
    "board": string,
    "medium": string,
    "stream": string,
    "currentSchool": string,
    "lastClassCompleted": string,
    "lastExamPercentage": string,
    "address": string,
    "mobileNumber": string,
    "alternateMobileNumber": string,
    "email": string,
    "fatherName": string,
    "motherName": string,
    "guardianName": string,
    "relation": string,
    "fatherMobileNumber": string,
    "motherMobileNumber": string,
    "guardianMobileNumber": string,
    "parentEmail": string,
    "fatherOccupation": string,
    "motherOccupation": string,
    "annualIncome": string,
    "nationality": string,
    "leadSource": string,
    "referredBy": string,
    "heardAboutUs": string,
    "preferredBranchId": string,
    "preferredBranchName": string,
    "courseId": string,
    "courseName": string,
    "batchId": string,
    "batchName": string,
    "subjects": Array<{
      "id": string,
      "code": string,
      "displayName": string,
      "shortName": string,
      "description": string,
      "sortOrder": integer,
      "isActive": boolean,
    }>
,
    "expectedAdmissionYear": string,
    "preferredAdmissionDate": string,
    "preferredContactTime": string,
    "modeOfContact": string,
    "bestDaysToContact": string,
    "courseRecommended": string,
    "batchSuggested": string,
    "admissionLikelihood": string,
    "remarks": string,
    "nextFollowUpAt": string,
    "status": string,
    "assignedToUserId": string,
    "assignedToName": string,
    "convertedStudentId": string,
    "convertedAt": string,
    "followUps": Array<{
      "id": string,
      "followUpAt": string,
      "modeOfContact": string,
      "notes": string,
      "nextFollowUpAt": string,
      "statusAfter": string,
      "createdByName": string,
    }>
,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/super-admin/leads/{leadId}/convert-to-admission

**Summary:** Convert lead to admission and optionally enrol into a batch

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `leadId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "studentId": string,
  "branchId": string,
  "standard": string,
  "batch": string,
  "board": string,
  "admissionDate": string,
  "batchId": string,
  "subjectGroupId": string,
  "subjectIds": Array<string>
,
  "agreedTotalFee": number,
  "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
  "instalments": Array<{
    "instalmentNumber": integer,
    "label": string,
    "amount": number,
    "dueDate": string,
    "isPostDatedCheque": boolean,
  }>
,
  "notes": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "leadId": string,
    "leadCode": string,
    "studentId": string,
    "visibleStudentId": string,
    "convertedAt": string,
    "enrolment": {
      "id": string,
      "studentId": string,
      "studentName": string,
      "studentLoginId": string,
      "batchId": string,
      "batchName": string,
      "academicYear": string,
      "courseId": string,
      "courseName": string,
      "courseCode": string,
      "subjectGroupId": string,
      "subjectGroupName": string,
      "subjects": Array<{
        "id": string,
        "code": string,
        "displayName": string,
        "shortName": string,
        "description": string,
        "sortOrder": integer,
        "isActive": boolean,
      }>
,
      "agreedTotalFee": number,
      "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
      "totalPaid": number,
      "totalPending": number,
      "instalments": Array<{
        "id": string,
        "instalmentNumber": integer,
        "label": string,
        "amount": number,
        "dueDate": string,
        "isPostDatedCheque": boolean,
        "isPaid": boolean,
        "paidDate": string,
      }>
,
      "enrolmentDate": string,
      "status": Enum(ACTIVE | COMPLETED | WITHDRAWN | TRANSFERRED | SUSPENDED),
      "notes": string,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/branches

**Summary:** Get branch overview summary and paginated branch list

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `search` | `query` | No | `string` |  |
| `isActive` | `query` | No | `boolean` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "summary": {
      "totalTeachers": integer,
      "activeTeachers": integer,
      "inactiveTeachers": integer,
    },
    "content": Array<{
      "id": string,
      "name": string,
      "address": string,
      "city": string,
      "phone": string,
      "email": string,
      "isActive": boolean,
      "totalStudents": integer,
      "totalTeachers": integer,
      "totalAdmins": integer,
      "createdAt": string,
      "updatedAt": string,
    }>
,
    "page": {
      "pageNumber": integer,
      "pageSize": integer,
      "totalElements": integer,
      "totalPages": integer,
      "first": boolean,
      "last": boolean,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/super-admin/branches

**Summary:** Create a new branch

### Request Body

```json
{
  "name": string,
  "address": string,
  "city": string,
  "phone": string,
  "email": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "name": string,
    "address": string,
    "city": string,
    "phone": string,
    "email": string,
    "isActive": boolean,
    "totalStudents": integer,
    "totalTeachers": integer,
    "totalAdmins": integer,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/admins

**Summary:** Get admin management summary and paginated admin list

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `search` | `query` | No | `string` |  |
| `isActive` | `query` | No | `boolean` |  |
| `branchId` | `query` | No | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "summary": {
      "totalTeachers": integer,
      "activeTeachers": integer,
      "inactiveTeachers": integer,
    },
    "content": Array<{
      "id": string,
      "userId": string,
      "fullName": string,
      "email": string,
      "phone": string,
      "loginId": string,
      "dateOfBirth": string,
      "gender": string,
      "profilePhotoUrl": string,
      "role": string,
      "joiningDate": string,
      "accessLevel": string,
      "address": string,
      "allBranchesAccess": boolean,
      "branchId": string,
      "branchName": string,
      "isActive": boolean,
      "lastLoginAt": string,
      "createdAt": string,
      "updatedAt": string,
    }>
,
    "page": {
      "pageNumber": integer,
      "pageSize": integer,
      "totalElements": integer,
      "totalPages": integer,
      "first": boolean,
      "last": boolean,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/super-admin/admins

**Summary:** Create a new admin account

### Request Body

```json
{
  "fullName": string,
  "email": string,
  "phone": string,
  "dateOfBirth": string,
  "gender": string,
  "profilePhotoUrl": string,
  "loginId": string,
  "password": string,
  "confirmPassword": string,
  "role": string,
  "joiningDate": string,
  "accessLevel": string,
  "branchId": string,
  "address": string,
  "allBranchesAccess": boolean,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "email": string,
    "phone": string,
    "loginId": string,
    "dateOfBirth": string,
    "gender": string,
    "profilePhotoUrl": string,
    "role": string,
    "joiningDate": string,
    "accessLevel": string,
    "address": string,
    "allBranchesAccess": boolean,
    "branchId": string,
    "branchName": string,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/enrolments

**Summary:** Enrol a student into a batch â admin selects subjects and enters fee manually

### Request Body

```json
{
  "studentId": string,
  "batchId": string,
  "subjectGroupId": string,
  "subjectIds": Array<string>
,
  "agreedTotalFee": number,
  "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
  "enrolmentDate": string,
  "instalments": Array<{
    "instalmentNumber": integer,
    "label": string,
    "amount": number,
    "dueDate": string,
    "isPostDatedCheque": boolean,
  }>
,
  "notes": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "studentId": string,
    "studentName": string,
    "studentLoginId": string,
    "batchId": string,
    "batchName": string,
    "academicYear": string,
    "courseId": string,
    "courseName": string,
    "courseCode": string,
    "subjectGroupId": string,
    "subjectGroupName": string,
    "subjects": Array<{
      "id": string,
      "code": string,
      "displayName": string,
      "shortName": string,
      "description": string,
      "sortOrder": integer,
      "isActive": boolean,
    }>
,
    "agreedTotalFee": number,
    "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
    "totalPaid": number,
    "totalPending": number,
    "instalments": Array<{
      "id": string,
      "instalmentNumber": integer,
      "label": string,
      "amount": number,
      "dueDate": string,
      "isPostDatedCheque": boolean,
      "isPaid": boolean,
      "paidDate": string,
    }>
,
    "enrolmentDate": string,
    "status": Enum(ACTIVE | COMPLETED | WITHDRAWN | TRANSFERRED | SUSPENDED),
    "notes": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/auth/refresh

**Summary:** Refresh access token using a valid refresh token

### Request Body

```json
{
  "refreshToken": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "accessToken": string,
    "refreshToken": string,
    "tokenType": string,
    "accessTokenExpiresIn": integer,
    "user": {
      "id": string,
      "email": string,
      "loginId": string,
      "fullName": string,
      "roles": Array<string>
,
      "branchId": string,
      "branchName": string,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/auth/logout

**Summary:** Logout from current device (revoke refresh token)

### Request Body

```json
{
  "refreshToken": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/auth/logout-all

**Summary:** Logout from all devices (revoke all refresh tokens)

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/auth/login

**Summary:** Login with email and password

### Request Body

```json
{
  "email": string,
  "identifier": string,
  "password": string,
  "fcmToken": string,
  "loginIdentifier": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "accessToken": string,
    "refreshToken": string,
    "tokenType": string,
    "accessTokenExpiresIn": integer,
    "user": {
      "id": string,
      "email": string,
      "loginId": string,
      "fullName": string,
      "roles": Array<string>
,
      "branchId": string,
      "branchName": string,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/auth/bootstrap/super-admin

**Summary:** Create the initial super admin account when the system is fresh

### Request Body

```json
{
  "fullName": string,
  "email": string,
  "phone": string,
  "password": string,
  "confirmPassword": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "accessToken": string,
    "refreshToken": string,
    "tokenType": string,
    "accessTokenExpiresIn": integer,
    "user": {
      "id": string,
      "email": string,
      "loginId": string,
      "fullName": string,
      "roles": Array<string>
,
      "branchId": string,
      "branchName": string,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/academic/standards

**Summary:** List standards and boards for the Standards & Boards screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `search` | `query` | No | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "items": Array<{
      "id": string,
      "standardId": string,
      "standard": string,
      "board": Enum(SSC | CBSE | ICSE | HSC),
      "boardLabel": string,
      "status": string,
    }>
,
    "page": integer,
    "size": integer,
    "totalItems": integer,
    "totalPages": integer,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/academic/standards

**Summary:** Create a standard from the Add Standard modal

### Request Body

```json
{
  "standard": string,
  "board": Enum(SSC | CBSE | ICSE | HSC),
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "standardId": string,
    "standard": string,
    "board": Enum(SSC | CBSE | ICSE | HSC),
    "boardLabel": string,
    "status": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/academic/courses

**Summary:** List courses for the Courses screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `query` | No | `string` |  |
| `search` | `query` | No | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "items": Array<{
      "id": string,
      "courseId": string,
      "standard": string,
      "board": string,
      "medium": string,
      "academicYear": string,
      "courseName": string,
      "batchName": string,
      "batchTiming": string,
    }>
,
    "page": integer,
    "size": integer,
    "totalItems": integer,
    "totalPages": integer,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/academic/courses

**Summary:** Create a course and its batch from the Add New Course screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `query` | No | `string` |  |

### Request Body

```json
{
  "standard": string,
  "board": Enum(SSC | CBSE | ICSE | HSC),
  "medium": string,
  "academicYear": string,
  "courseName": string,
  "batchName": string,
  "batchTiming": Enum(MORNING | EVENING | CUSTOM),
  "startTime": {
    "hour": integer,
    "minute": integer,
    "second": integer,
    "nano": integer,
  },
  "endTime": {
    "hour": integer,
    "minute": integer,
    "second": integer,
    "nano": integer,
  },
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "courseId": string,
    "standard": string,
    "board": string,
    "medium": string,
    "academicYear": string,
    "courseName": string,
    "batchName": string,
    "batchTiming": string,
    "startTime": {
      "hour": integer,
      "minute": integer,
      "second": integer,
      "nano": integer,
    },
    "endTime": {
      "hour": integer,
      "minute": integer,
      "second": integer,
      "nano": integer,
    },
    "subjects": Array<{
      "id": string,
      "subjectId": string,
      "subjectName": string,
      "subjectCode": string,
      "status": string,
    }>
,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## POST /api/academic/courses/{courseId}/subjects

**Summary:** Add a subject from the View Course screen

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `courseId` | `path` | Yes | `string` |  |
| `branchId` | `query` | No | `string` |  |

### Request Body

```json
{
  "subjectName": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "subjectId": string,
    "subjectName": string,
    "subjectCode": string,
    "status": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PATCH /api/super-admin/teachers/{teacherId}/status

**Summary:** Activate or deactivate a teacher account

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `teacherId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "isActive": boolean,
  "reason": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "email": string,
    "phone": string,
    "loginId": string,
    "dateOfBirth": string,
    "gender": string,
    "profilePhotoUrl": string,
    "qualification": string,
    "experienceYears": integer,
    "subjects": Array<string>
,
    "subjectIds": Array<string>
,
    "specialization": string,
    "joiningDate": string,
    "employmentType": string,
    "salaryType": string,
    "hourlyRate": number,
    "address": string,
    "branchId": string,
    "branchName": string,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PATCH /api/super-admin/students/{studentId}/status

**Summary:** Activate or deactivate a student account

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `studentId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "isActive": boolean,
  "reason": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "studentId": string,
    "branchId": string,
    "branchName": string,
    "standard": string,
    "batch": string,
    "gender": string,
    "dateOfBirth": string,
    "mobile": string,
    "parentName": string,
    "parentPhone": string,
    "email": string,
    "address": string,
    "schoolName": string,
    "board": Enum(SSC | CBSE | ICSE | HSC),
    "admissionDate": string,
    "loginId": string,
    "profilePhotoUrl": string,
    "isAdmissionFinal": boolean,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PATCH /api/super-admin/leads/{leadId}/status

**Summary:** Update lead status and add follow-up history

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `leadId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "status": string,
  "notes": string,
  "modeOfContact": string,
  "nextFollowUpAt": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "leadCode": string,
    "studentName": string,
    "gender": string,
    "dateOfBirth": string,
    "bloodGroup": string,
    "classInterestedIn": string,
    "board": string,
    "medium": string,
    "stream": string,
    "currentSchool": string,
    "lastClassCompleted": string,
    "lastExamPercentage": string,
    "address": string,
    "mobileNumber": string,
    "alternateMobileNumber": string,
    "email": string,
    "fatherName": string,
    "motherName": string,
    "guardianName": string,
    "relation": string,
    "fatherMobileNumber": string,
    "motherMobileNumber": string,
    "guardianMobileNumber": string,
    "parentEmail": string,
    "fatherOccupation": string,
    "motherOccupation": string,
    "annualIncome": string,
    "nationality": string,
    "leadSource": string,
    "referredBy": string,
    "heardAboutUs": string,
    "preferredBranchId": string,
    "preferredBranchName": string,
    "courseId": string,
    "courseName": string,
    "batchId": string,
    "batchName": string,
    "subjects": Array<{
      "id": string,
      "code": string,
      "displayName": string,
      "shortName": string,
      "description": string,
      "sortOrder": integer,
      "isActive": boolean,
    }>
,
    "expectedAdmissionYear": string,
    "preferredAdmissionDate": string,
    "preferredContactTime": string,
    "modeOfContact": string,
    "bestDaysToContact": string,
    "courseRecommended": string,
    "batchSuggested": string,
    "admissionLikelihood": string,
    "remarks": string,
    "nextFollowUpAt": string,
    "status": string,
    "assignedToUserId": string,
    "assignedToName": string,
    "convertedStudentId": string,
    "convertedAt": string,
    "followUps": Array<{
      "id": string,
      "followUpAt": string,
      "modeOfContact": string,
      "notes": string,
      "nextFollowUpAt": string,
      "statusAfter": string,
      "createdByName": string,
    }>
,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PATCH /api/super-admin/branches/{branchId}/status

**Summary:** Activate or deactivate a branch

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "isActive": boolean,
  "reason": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "name": string,
    "address": string,
    "city": string,
    "phone": string,
    "email": string,
    "isActive": boolean,
    "totalStudents": integer,
    "totalTeachers": integer,
    "totalAdmins": integer,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## PATCH /api/super-admin/admins/{adminId}/status

**Summary:** Activate or deactivate an admin account

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `adminId` | `path` | Yes | `string` |  |

### Request Body

```json
{
  "isActive": boolean,
  "reason": string,
}
```

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "userId": string,
    "fullName": string,
    "email": string,
    "phone": string,
    "loginId": string,
    "dateOfBirth": string,
    "gender": string,
    "profilePhotoUrl": string,
    "role": string,
    "joiningDate": string,
    "accessLevel": string,
    "address": string,
    "allBranchesAccess": boolean,
    "branchId": string,
    "branchName": string,
    "isActive": boolean,
    "lastLoginAt": string,
    "createdAt": string,
    "updatedAt": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/reports

**Summary:** Get generated reports list

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `category` | `query` | No | `string` |  |
| `reportType` | `query` | No | `string` |  |
| `format` | `query` | No | `string` |  |
| `branchId` | `query` | No | `string` |  |
| `search` | `query` | No | `string` |  |
| `fromDate` | `query` | No | `string` |  |
| `toDate` | `query` | No | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "content": Array<{
      "id": string,
      "reportName": string,
      "category": string,
      "categoryLabel": string,
      "reportType": string,
      "reportTypeLabel": string,
      "description": string,
      "generatedBy": string,
      "generatedOn": string,
      "format": string,
      "status": string,
      "branchId": string,
      "branchName": string,
      "fromDate": string,
      "toDate": string,
      "downloadUrl": string,
    }>
,
    "page": {
      "pageNumber": integer,
      "pageSize": integer,
      "totalElements": integer,
      "totalPages": integer,
      "first": boolean,
      "last": boolean,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/reports/{reportId}/download

**Summary:** Download a generated report

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `reportId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
string
```

---

## GET /api/super-admin/reports/summary

**Summary:** Get reports dashboard summary

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `query` | No | `string` |  |
| `fromDate` | `query` | No | `string` |  |
| `toDate` | `query` | No | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "fromDate": string,
    "toDate": string,
    "summary": {
      "totalTeachers": integer,
      "activeTeachers": integer,
      "inactiveTeachers": integer,
    },
    "categories": Array<{
      "key": string,
      "label": string,
      "description": string,
      "reportTypes": Array<{
        "key": string,
        "label": string,
        "defaultFormat": string,
      }>
,
    }>
,
    "recentReports": Array<{
      "id": string,
      "reportName": string,
      "category": string,
      "categoryLabel": string,
      "reportType": string,
      "reportTypeLabel": string,
      "description": string,
      "generatedBy": string,
      "generatedOn": string,
      "format": string,
      "status": string,
      "branchId": string,
      "branchName": string,
      "fromDate": string,
      "toDate": string,
      "downloadUrl": string,
    }>
,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/reports/categories

**Summary:** Get report categories and available report types

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": Array<{
    "key": string,
    "label": string,
    "description": string,
    "reportTypes": Array<{
      "key": string,
      "label": string,
      "defaultFormat": string,
    }>
,
  }>
,
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/dashboard

**Summary:** Get the super admin dashboard data

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `query` | No | `string` |  |
| `fromDate` | `query` | No | `string` |  |
| `toDate` | `query` | No | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "fromDate": string,
    "toDate": string,
    "branchId": string,
    "branchName": string,
    "branches": Array<{
      "id": string,
      "name": string,
    }>
,
    "overview": {
      "totalStudents": {
        "total": integer,
        "changeThisMonth": integer,
      },
      "totalTeachers": {
        "total": integer,
        "changeThisMonth": integer,
      },
      "totalAdmins": {
        "total": integer,
        "changeThisMonth": integer,
      },
      "totalFeesCollected": {
        "total": number,
        "changeThisMonth": number,
      },
      "pendingFees": {
        "total": number,
        "changeThisMonth": number,
      },
    },
    "studentGrowth": Array<{
      "month": string,
      "totalStudents": integer,
    }>
,
    "feeCollection": Array<{
      "month": string,
      "feesCollected": number,
      "pendingFees": number,
    }>
,
    "attendanceOverview": {
      "present": integer,
      "leave": integer,
      "absent": integer,
      "averageAttendancePercentage": number,
    },
    "leadConversionOverview": {
      "stages": Array<{
        "label": string,
        "count": integer,
        "percentage": number,
      }>
,
    },
    "recentActivities": Array<{
      "type": string,
      "title": string,
      "description": string,
      "branchName": string,
      "createdAt": string,
    }>
,
    "footerMetrics": {
      "totalClassesToday": integer,
      "teachersIn": integer,
      "studentsPresent": integer,
      "testsConducted": integer,
      "feedbacksReceived": integer,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/branches/options

**Summary:** Get active branch options for filters and selectors

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": Array<{
    "id": string,
    "name": string,
  }>
,
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/super-admin/analytics

**Summary:** Get the super admin analytics data

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `tab` | `query` | No | `string` |  |
| `branchId` | `query` | No | `string` |  |
| `fromDate` | `query` | No | `string` |  |
| `toDate` | `query` | No | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "fromDate": string,
    "toDate": string,
    "branchId": string,
    "branchName": string,
    "activeTab": string,
    "branches": Array<{
      "id": string,
      "name": string,
    }>
,
    "tabs": Array<{
      "key": string,
      "label": string,
    }>
,
    "overview": {
      "cards": Array<{
        "total": integer,
        "changeThisMonth": integer,
      }>
,
      "studentGrowth": Array<{
        "month": string,
        "currentValue": integer,
        "previousValue": integer,
      }>
,
      "feeCollectionOverview": Array<{
        "month": string,
        "feesCollected": number,
        "pendingFees": number,
      }>
,
      "leadConversionFunnel": {
        "stages": Array<{
          "label": string,
          "count": integer,
          "percentage": number,
        }>
,
      },
      "admissionsOverview": {
        "totalAdmissions": integer,
        "branches": Array<{
          "label": string,
          "count": integer,
          "percentage": number,
        }>
,
        "conversionRate": number,
        "inquiryToAdmissionRate": number,
      },
    },
    "students": {
      "cards": Array<{
        "total": integer,
        "changeThisMonth": integer,
      }>
,
      "studentGrowth": Array<{
        "month": string,
        "currentValue": integer,
        "previousValue": integer,
      }>
,
      "studentsByClass": {
        "total": integer,
        "segments": Array<{
          "label": string,
          "count": integer,
          "percentage": number,
        }>
,
      },
      "studentsByGender": {
        "total": integer,
        "segments": Array<{
          "label": string,
          "count": integer,
          "percentage": number,
        }>
,
      },
      "admissionsVsDropped": Array<{
        "month": string,
        "newAdmissions": integer,
        "droppedStudents": integer,
      }>
,
    },
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/enrolments/student/{studentId}

**Summary:** Get all enrolments for a student (all batches and years)

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `studentId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": Array<{
    "id": string,
    "studentId": string,
    "studentName": string,
    "studentLoginId": string,
    "batchId": string,
    "batchName": string,
    "academicYear": string,
    "courseId": string,
    "courseName": string,
    "courseCode": string,
    "subjectGroupId": string,
    "subjectGroupName": string,
    "subjects": Array<{
      "id": string,
      "code": string,
      "displayName": string,
      "shortName": string,
      "description": string,
      "sortOrder": integer,
      "isActive": boolean,
    }>
,
    "agreedTotalFee": number,
    "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
    "totalPaid": number,
    "totalPending": number,
    "instalments": Array<{
      "id": string,
      "instalmentNumber": integer,
      "label": string,
      "amount": number,
      "dueDate": string,
      "isPostDatedCheque": boolean,
      "isPaid": boolean,
      "paidDate": string,
    }>
,
    "enrolmentDate": string,
    "status": Enum(ACTIVE | COMPLETED | WITHDRAWN | TRANSFERRED | SUSPENDED),
    "notes": string,
  }>
,
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/enrolments/branch/{branchId}

**Summary:** List enrolments by branch and academic year (paginated)

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `branchId` | `path` | Yes | `string` |  |
| `academicYear` | `query` | Yes | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "totalPages": integer,
    "totalElements": integer,
    "size": integer,
    "content": Array<{
      "id": string,
      "studentId": string,
      "studentName": string,
      "studentLoginId": string,
      "batchId": string,
      "batchName": string,
      "academicYear": string,
      "courseId": string,
      "courseName": string,
      "courseCode": string,
      "subjectGroupId": string,
      "subjectGroupName": string,
      "subjects": Array<{
        "id": string,
        "code": string,
        "displayName": string,
        "shortName": string,
        "description": string,
        "sortOrder": integer,
        "isActive": boolean,
      }>
,
      "agreedTotalFee": number,
      "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
      "totalPaid": number,
      "totalPending": number,
      "instalments": Array<{
        "id": string,
        "instalmentNumber": integer,
        "label": string,
        "amount": number,
        "dueDate": string,
        "isPostDatedCheque": boolean,
        "isPaid": boolean,
        "paidDate": string,
      }>
,
      "enrolmentDate": string,
      "status": Enum(ACTIVE | COMPLETED | WITHDRAWN | TRANSFERRED | SUSPENDED),
      "notes": string,
    }>
,
    "number": integer,
    "sort": Array<{
      "direction": string,
      "nullHandling": string,
      "ascending": boolean,
      "property": string,
      "ignoreCase": boolean,
    }>
,
    "first": boolean,
    "last": boolean,
    "numberOfElements": integer,
    "pageable": {
      "offset": integer,
      "sort": Array<{
        "direction": string,
        "nullHandling": string,
        "ascending": boolean,
        "property": string,
        "ignoreCase": boolean,
      }>
,
      "unpaged": boolean,
      "paged": boolean,
      "pageSize": integer,
      "pageNumber": integer,
    },
    "empty": boolean,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/enrolments/batch/{batchId}

**Summary:** List all enrolments in a batch (paginated)

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `batchId` | `path` | Yes | `string` |  |
| `page` | `query` | No | `integer` |  |
| `size` | `query` | No | `integer` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "totalPages": integer,
    "totalElements": integer,
    "size": integer,
    "content": Array<{
      "id": string,
      "studentId": string,
      "studentName": string,
      "studentLoginId": string,
      "batchId": string,
      "batchName": string,
      "academicYear": string,
      "courseId": string,
      "courseName": string,
      "courseCode": string,
      "subjectGroupId": string,
      "subjectGroupName": string,
      "subjects": Array<{
        "id": string,
        "code": string,
        "displayName": string,
        "shortName": string,
        "description": string,
        "sortOrder": integer,
        "isActive": boolean,
      }>
,
      "agreedTotalFee": number,
      "paymentPlan": Enum(REGULAR | LUMPSUM | INSTALMENT_2 | INSTALMENT_3),
      "totalPaid": number,
      "totalPending": number,
      "instalments": Array<{
        "id": string,
        "instalmentNumber": integer,
        "label": string,
        "amount": number,
        "dueDate": string,
        "isPostDatedCheque": boolean,
        "isPaid": boolean,
        "paidDate": string,
      }>
,
      "enrolmentDate": string,
      "status": Enum(ACTIVE | COMPLETED | WITHDRAWN | TRANSFERRED | SUSPENDED),
      "notes": string,
    }>
,
    "number": integer,
    "sort": Array<{
      "direction": string,
      "nullHandling": string,
      "ascending": boolean,
      "property": string,
      "ignoreCase": boolean,
    }>
,
    "first": boolean,
    "last": boolean,
    "numberOfElements": integer,
    "pageable": {
      "offset": integer,
      "sort": Array<{
        "direction": string,
        "nullHandling": string,
        "ascending": boolean,
        "property": string,
        "ignoreCase": boolean,
      }>
,
      "unpaged": boolean,
      "paged": boolean,
      "pageSize": integer,
      "pageNumber": integer,
    },
    "empty": boolean,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/auth/me

**Summary:** Get current authenticated user info

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
    "id": string,
    "email": string,
    "loginId": string,
    "fullName": string,
    "roles": Array<string>
,
    "branchId": string,
    "branchName": string,
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/academic/standards/options

**Summary:** List active standards for the course form dropdown

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": Array<{
    "id": string,
    "standardId": string,
    "standard": string,
    "board": Enum(SSC | CBSE | ICSE | HSC),
    "boardLabel": string,
    "status": string,
  }>
,
  "errorCode": string,
  "timestamp": string,
}
```

---

## GET /api/academic/boards

**Summary:** List available boards for the standards and course screens

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": Array<{
    "code": Enum(SSC | CBSE | ICSE | HSC),
    "label": string,
  }>
,
  "errorCode": string,
  "timestamp": string,
}
```

---

## DELETE /api/super-admin/reports/{reportId}

**Summary:** Delete a generated report record

### Parameters

| Name | In | Required | Type | Description |
|---|---|---|---|---|
| `reportId` | `path` | Yes | `string` |  |

### Responses

#### 200

**Description:** OK

```json
{
  "success": boolean,
  "message": string,
  "data": {
  },
  "errorCode": string,
  "timestamp": string,
}
```

---

