package com.careerpilot.service;

import static org.junit.jupiter.api.Assertions.*;

import com.careerpilot.entity.ApplicationStatus;
import com.careerpilot.exception.BadRequestException;
import org.junit.jupiter.api.Test;

class ApplicationStatusTest {
    @Test void happyPathIsAllowedStepByStep() {
        assertTrue(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.SHORTLISTED));
        assertTrue(ApplicationStatus.SHORTLISTED.canTransitionTo(ApplicationStatus.TECHNICAL_INTERVIEW));
        assertTrue(ApplicationStatus.TECHNICAL_INTERVIEW.canTransitionTo(ApplicationStatus.HR_INTERVIEW));
        assertTrue(ApplicationStatus.HR_INTERVIEW.canTransitionTo(ApplicationStatus.SELECTED));
    }

    @Test void skippingStagesIsRejected() {
        assertFalse(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.SELECTED));
        assertFalse(ApplicationStatus.APPLIED.canTransitionTo(ApplicationStatus.HR_INTERVIEW));
    }

    @Test void canRejectFromAnyOpenStage() {
        for (ApplicationStatus s : new ApplicationStatus[]{ApplicationStatus.APPLIED, ApplicationStatus.SHORTLISTED,
                ApplicationStatus.TECHNICAL_INTERVIEW, ApplicationStatus.HR_INTERVIEW})
            assertTrue(s.canTransitionTo(ApplicationStatus.REJECTED));
    }

    @Test void finalStatusesAreTerminal() {
        assertTrue(ApplicationStatus.SELECTED.allowedNext().isEmpty());
        assertTrue(ApplicationStatus.REJECTED.allowedNext().isEmpty());
        assertFalse(ApplicationStatus.REJECTED.canTransitionTo(ApplicationStatus.APPLIED));
    }

    @Test void parseAcceptsLabelsAndRejectsGarbage() {
        assertEquals(ApplicationStatus.TECHNICAL_INTERVIEW, ApplicationStatus.parse("Technical Interview"));
        assertThrows(BadRequestException.class, () -> ApplicationStatus.parse("Hired!!"));
    }
}
