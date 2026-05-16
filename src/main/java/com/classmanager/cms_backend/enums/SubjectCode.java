package com.classmanager.cms_backend.enums;

/**
 * Canonical subject codes used across all boards and standards.
 * New subjects can be added here without breaking existing data.
 * The display name is stored separately in the Subject entity so
 * the admin can rename labels without changing the code.
 */
public enum SubjectCode {
    // ── Core / Common ────────────────────────────────────────────────────────
    MATHS,
    SCIENCE,
    ENGLISH,
    LANGUAGE,       // Marathi / Hindi / Semi-English language paper
    SST,            // Social Studies / History-Geography / Civics
    HINDI,
    MARATHI,
    SANSKRIT,
    GERMAN,
    HISTORY,
    GEOGRAPHY,
    CIVICS,
    // ── Science stream (11th-12th) ───────────────────────────────────────────
    PHYSICS,
    CHEMISTRY,
    BIOLOGY,
    MATHS_ADVANCED, // Maths for PCM stream
    // ── Commerce / Arts ──────────────────────────────────────────────────────
    ECONOMICS,
    ACCOUNTS,
    BUSINESS_STUDIES,
    POLITICAL_SCIENCE,
    SOCIOLOGY,
    // ── Languages / Literature ───────────────────────────────────────────────
    LITERATURE,
    COMP_APP,       // Computer Applications / IT
    // ── Catch-all for institute-specific subjects ────────────────────────────
    OTHER
}
