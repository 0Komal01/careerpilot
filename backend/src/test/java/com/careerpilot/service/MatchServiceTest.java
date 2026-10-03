package com.careerpilot.service;

import static org.junit.jupiter.api.Assertions.*;

import com.careerpilot.dto.Dtos.SkillMatch;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchServiceTest {
    private final MatchService service = new MatchService();

    @Test void fullMatchIs100() {
        SkillMatch m = service.calculate(List.of("Java", "SQL"), List.of("java", "sql"));
        assertEquals(100.0, m.percentage());
        assertTrue(m.missing().isEmpty());
    }

    @Test void partialMatchReturnsMatchedAndMissing() {
        SkillMatch m = service.calculate(List.of("Java", "Git"), List.of("Java", "Spring Boot", "SQL", "Git"));
        assertEquals(50.0, m.percentage());
        assertEquals(List.of("Java", "Git"), m.matched());
        assertEquals(List.of("Spring Boot", "SQL"), m.missing());
    }

    @Test void matchingIgnoresCaseAndWhitespace() {
        assertEquals(100.0, service.calculate(List.of("  spring boot "), List.of("Spring Boot")).percentage());
    }

    @Test void emptyStudentSkillsGivesZero() {
        assertEquals(0.0, service.calculate(List.of(), List.of("Java", "SQL")).percentage());
    }

    @Test void zeroRequiredSkillsDoesNotDivideByZero() {
        SkillMatch m = service.calculate(List.of("Java"), List.of());
        assertEquals(100.0, m.percentage());
        assertTrue(m.matched().isEmpty() && m.missing().isEmpty());
    }

    @Test void percentageIsRoundedToOneDecimal() {
        assertEquals(33.3, service.calculate(List.of("A"), List.of("A", "B", "C")).percentage());
    }
}
