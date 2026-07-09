package org.acme.vehiclerouting.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.acme.vehiclerouting.dto.VehicleRoutingInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        VehicleRoutingInput problem = DemoDataBuilder.builder().build();

        assertEquals(6, problem.vehicles().size());
        assertEquals(55, problem.visits().size());
        assertNotNull(problem.southWestCorner());
        assertNotNull(problem.northEastCorner());
        assertNotNull(problem.startDateTime());
        assertNotNull(problem.endDateTime());

        problem.vehicles().forEach(vehicle -> {
            assertNotNull(vehicle.id());
            assertTrue(vehicle.visitIds().isEmpty());
            assertTrue(vehicle.capacity() > 0);
            assertNotNull(vehicle.homeLocation());
        });
        problem.visits().forEach(visit -> {
            assertNotNull(visit.id());
            assertNotNull(visit.name());
            assertNotNull(visit.location());
            assertNotNull(visit.minStartTime());
            assertNotNull(visit.maxEndTime());
            assertTrue(visit.serviceDurationSeconds() > 0);
        });
        assertFalse(problem.visits().isEmpty());
    }
}
