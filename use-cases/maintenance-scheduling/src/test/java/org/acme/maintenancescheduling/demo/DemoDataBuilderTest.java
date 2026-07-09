package org.acme.maintenancescheduling.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.acme.maintenancescheduling.dto.MaintenanceScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        MaintenanceScheduleInput problem = correctBuilder().build();

        assertNotNull(problem.workCalendar());
        assertEquals(3, problem.crews().size());
        assertFalse(problem.jobs().isEmpty());
        problem.jobs().forEach(job -> {
            assertNotNull(job.id());
            assertEquals(null, job.crewId());
            assertEquals(null, job.startDate());
        });
    }

    @Test
    void weekListSizeGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setWeekListSize(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void atLeastOneCrew() {
        DemoDataBuilder builder = DemoDataBuilder.builder().setWeekListSize(8);
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder()
                .setWeekListSize(8)
                .addCrew("Alpha crew")
                .addCrew("Beta crew")
                .addCrew("Gamma crew");
    }
}
