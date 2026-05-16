package com.classmanager.cms_backend.enums;

/**
 * Payment plan chosen by the admin at enrolment time.
 * The actual amounts are entered manually — no backend calculation.
 *
 * REGULAR      – single full payment (standard)
 * LUMPSUM      – single upfront lump-sum (e.g. with discount)
 * INSTALMENT_2 – two-instalment schedule
 * INSTALMENT_3 – three-instalment schedule
 */
public enum FeePaymentPlan {
    REGULAR,
    LUMPSUM,
    INSTALMENT_2,
    INSTALMENT_3
}
