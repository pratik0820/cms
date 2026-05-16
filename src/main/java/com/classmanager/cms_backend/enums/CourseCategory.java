package com.classmanager.cms_backend.enums;

/**
 * High-level category of a course offering.
 *
 * BOARD_REGULAR   – Standard board curriculum (SSC / CBSE / ICSE 8-10)
 * FOUNDATION      – Foundation / Junior programme (7th-10th)
 * BOARD_SENIOR    – Senior board (HSC / CBSE 11-12 board-only)
 * COMPETITIVE     – Entrance exam prep (JEE / NEET / CET)
 * COMBINED        – Board + competitive combined (e.g. 11-12 + JEE/NEET)
 */
public enum CourseCategory {
    BOARD_REGULAR,
    FOUNDATION,
    BOARD_SENIOR,
    COMPETITIVE,
    COMBINED
}
