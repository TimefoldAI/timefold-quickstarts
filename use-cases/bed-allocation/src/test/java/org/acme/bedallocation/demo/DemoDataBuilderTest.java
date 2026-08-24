package org.acme.bedallocation.demo;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.acme.bedallocation.dto.BedPlanInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        BedPlanInput problem = DemoDataBuilder.builder().build();

        assertEquals(1, problem.departments().size());
        assertEquals(15, problem.departments().get(0).rooms().size());
        assertEquals(1, problem.departments().get(0).minimumAge());
        assertEquals(100, problem.departments().get(0).maximumAge());
        assertEquals(111, problem.stays().size());
        int bedCount = problem.departments().get(0).rooms().stream().mapToInt(room -> room.beds().size()).sum();
        assertEquals(23, bedCount);
        problem.stays().forEach(stay -> {
            assertNotNull(stay.id());
            assertNotNull(stay.arrivalDate());
            assertNotNull(stay.departureDate());
            assertNull(stay.bedId());

            // Departure is always at least a day after arrival (never the same day) and stays span
            // between 1 and 5 days.
            long dayCount = DAYS.between(LocalDate.parse(stay.arrivalDate()), LocalDate.parse(stay.departureDate()));
            assertThat(dayCount).isBetween(1L, 5L);
        });
    }
}
