package com.classmanager.cms_backend.enums;

/**
 * Batch type / tier name.
 * Chanakya = A (premium), Drona = B, Vyasa = C, Arjuna = D (basic).
 * For HSC/CBSE 11-12 the tiers are labelled A-E.
 * Used as an optional label on a batch — fee is entered manually at enrolment.
 */
public enum BatchType {
    CHANAKYA,   // Tier A – all subjects (premium)
    DRONA,      // Tier B
    VYASA,      // Tier C
    ARJUNA,     // Tier D (basic)
    TIER_E      // Tier E – single year (11th only or 12th only)
}
