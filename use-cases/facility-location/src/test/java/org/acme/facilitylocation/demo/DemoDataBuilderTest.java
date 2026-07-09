package org.acme.facilitylocation.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.acme.facilitylocation.dto.FacilityLocationInput;
import org.acme.facilitylocation.dto.LocationDTO;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        FacilityLocationInput problem = DemoDataBuilder.builder().setCapacity(1000).setDemand(900)
                .setAverageSetupCost(1000).setSetupCostStandardDeviation(200).setFacilityCount(10).setConsumerCount(150)
                .setSouthWestCorner(new LocationDTO(-10, -10)).setNorthEastCorner(new LocationDTO(20, 20)).build();

        assertEquals(10, problem.facilities().size());
        problem.facilities().forEach(facility -> assertEquals(100, facility.capacity()));

        assertEquals(150, problem.consumers().size());
        problem.consumers().forEach(consumer -> assertEquals(6, consumer.demand()));
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
        return DemoDataBuilder.builder().setSouthWestCorner(new LocationDTO(-1, -1))
                .setNorthEastCorner(new LocationDTO(1, 1)).setCapacity(20).setDemand(10).setConsumerCount(1)
                .setFacilityCount(1).setAverageSetupCost(100).setSetupCostStandardDeviation(1);
    }
}
