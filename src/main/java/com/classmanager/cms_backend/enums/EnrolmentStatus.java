package com.classmanager.cms_backend.enums;

/**
 * Lifecycle status of a student enrolment.
 *
 * ACTIVE      – currently enrolled and attending
 * COMPLETED   – academic year / course completed
 * WITHDRAWN   – student left mid-course
 * TRANSFERRED – moved to another batch or branch
 * SUSPENDED   – temporarily suspended
 */
public enum EnrolmentStatus {
    ACTIVE,
    COMPLETED,
    WITHDRAWN,
    TRANSFERRED,
    SUSPENDED
}
