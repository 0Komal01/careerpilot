package com.careerpilot.entity;

import com.careerpilot.exception.BadRequestException;
import java.util.EnumSet;
import java.util.Set;

/** Pipeline: Applied -> Shortlisted -> Technical -> HR -> Selected (Rejected allowed from any open stage). */
public enum ApplicationStatus {
    APPLIED("Applied"),
    SHORTLISTED("Shortlisted"),
    TECHNICAL_INTERVIEW("Technical Interview"),
    HR_INTERVIEW("HR Interview"),
    SELECTED("Selected"),
    REJECTED("Rejected");

    private final String label;
    ApplicationStatus(String label) { this.label = label; }
    public String getLabel() { return label; }

    public Set<ApplicationStatus> allowedNext() {
        return switch (this) {
            case APPLIED -> EnumSet.of(SHORTLISTED, REJECTED);
            case SHORTLISTED -> EnumSet.of(TECHNICAL_INTERVIEW, REJECTED);
            case TECHNICAL_INTERVIEW -> EnumSet.of(HR_INTERVIEW, REJECTED);
            case HR_INTERVIEW -> EnumSet.of(SELECTED, REJECTED);
            case SELECTED, REJECTED -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    public boolean canTransitionTo(ApplicationStatus next) { return allowedNext().contains(next); }

    public static ApplicationStatus parse(String text) {
        if (text == null) throw new BadRequestException("Status is required");
        String key = text.trim().toUpperCase().replace(' ', '_');
        try { return valueOf(key); }
        catch (IllegalArgumentException e) { throw new BadRequestException("Unknown status: " + text); }
    }
}
