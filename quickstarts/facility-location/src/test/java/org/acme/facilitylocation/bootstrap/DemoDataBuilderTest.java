package org.acme.facilitylocation.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        FacilityLocationProblem problem = DemoDataBuilder.builder()
                .setCapacity(1000)
                .setDemand(900)
                .setAverageSetupCost(1000).setSetupCostStandardDeviation(200)
                .setFacilityCount(10)
                .setConsumerCount(150)
                .setSouthWestCorner(new Location(-10, -10))
                .setNorthEastCorner(new Location(20, 20))
                .build();

        assertEquals(10, problem.getFacilities().size());
        // Show toString().
        problem.getFacilities().forEach(facility -> assertEquals(100, facility.getCapacity()));

        assertEquals(150, problem.getConsumers().size());
        // Show toString().
        problem.getConsumers().forEach(consumer -> assertEquals(6, consumer.getDemand()));
    }

    @Test
    void correctBuilderBuildsOk() {
        assertNotNull(correctBuilder().build());
    }

    @Test
    void capacity_greater_than_demand() {
        DemoDataBuilder builder = correctBuilder().setDemand(Long.MAX_VALUE);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void capacityGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setCapacity(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setCapacity(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void demandGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setDemand(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setDemand(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void facilityCountGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setFacilityCount(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setFacilityCount(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void consumer_count_greater_than_zero() {
        DemoDataBuilder builder = correctBuilder().setConsumerCount(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setConsumerCount(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder()
                .setSouthWestCorner(new Location(-1, -1))
                .setNorthEastCorner(new Location(1, 1))
                .setCapacity(20)
                .setDemand(10)
                .setConsumerCount(1)
                .setFacilityCount(1)
                .setAverageSetupCost(100)
                .setSetupCostStandardDeviation(1);
    }
}
