package org.acme.bedallocation.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.acme.bedallocation.dto.BedPlanInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        BedPlanInput problem = DemoDataBuilder.builder().build();

        assertEquals(1, problem.departments().size());
        assertEquals(10, problem.departments().get(0).rooms().size());
        assertEquals(1, problem.departments().get(0).minimumAge());
        assertEquals(100, problem.departments().get(0).maximumAge());
        assertFalse(problem.stays().isEmpty());
        problem.stays().forEach(stay -> {
            assertNotNull(stay.id());
            assertNotNull(stay.arrivalDate());
            assertNotNull(stay.departureDate());
            assertNull(stay.bedId());
        });
    }
}
