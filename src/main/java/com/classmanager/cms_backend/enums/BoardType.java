package com.classmanager.cms_backend.enums;

public enum BoardType {

    SSC,
    CBSE,
    ICSE,
    HSC;

    public String getDisplayName() {
        return switch (this) {
            case SSC -> "SSC (Maharashtra State Board)";
            case CBSE -> "CBSE";
            case ICSE -> "ICSE";
            case HSC -> "HSC (Maharashtra State Board)";
        };
    }
}
