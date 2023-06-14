package org.acme.vehiclerouting.rest;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void should_build_data() {
        Integer customerCount = 77;
        Integer vehicleCount = 6;
        Integer depotCount = 2;
        Integer minDemand = 1;
        Integer maxDemand = 2;

        VehicleRoutingSolution problem = VehicleRoutingDemoResource.DemoDataBuilder.builder().setMinDemand(minDemand).setMaxDemand(maxDemand)
                .setVehicleCapacity(15).setCustomerCount(customerCount).setVehicleCount(vehicleCount)
                .setDepotCount(depotCount).setSouthWestCorner(new Location(43.751466, 11.177210))
                .setNorthEastCorner(new Location(43.809291, 11.290195)).build();

        problem.getCustomers().forEach(
                customer -> assertTrue((minDemand <= customer.getDemand()) && (maxDemand >= customer.getDemand())));

        assertEquals(customerCount, problem.getCustomers().size());
        assertEquals(vehicleCount, problem.getVehicles().size());
        assertEquals(depotCount, problem.getDepots().size());
    }

    @Test
    void correct_builder_builds_ok() {
        assertNotNull(correctBuilder().build());
    }

    @Test
    void capacity_greater_than_zero() {
        assertThatIllegalStateException().isThrownBy(correctBuilder().setVehicleCapacity(0)::build);
        assertThatIllegalStateException().isThrownBy(correctBuilder().setVehicleCapacity(-1)::build);
    }

    @Test
    void demand_test() {
        assertThatIllegalStateException().isThrownBy(correctBuilder().setMinDemand(0)::build);
        assertThatIllegalStateException().isThrownBy(correctBuilder().setMinDemand(-1)::build);

        assertThatIllegalStateException().isThrownBy(correctBuilder().setMaxDemand(0)::build);
        assertThatIllegalStateException().isThrownBy(correctBuilder().setMaxDemand(-1)::build);

        assertThatIllegalStateException().isThrownBy(correctBuilder().setMinDemand(2).setMaxDemand(1)::build);
    }

    @Test
    void map_corner_test() {
        assertThatIllegalStateException().isThrownBy(correctBuilder()
                        .setSouthWestCorner(new Location(2, 1))
                        .setNorthEastCorner(new Location(1, 2))::build)
                .withMessageMatching(".*northEast.*Latitude.*must be greater than southWest.*Latitude.*");

        assertThatIllegalStateException().isThrownBy(correctBuilder()
                        .setSouthWestCorner(new Location(1, 1))
                        .setNorthEastCorner(new Location(1, 2))::build)
                .withMessageMatching(".*northEast.*Latitude.*must be greater than southWest.*Latitude.*");

        assertThatIllegalStateException().isThrownBy(correctBuilder()
                        .setSouthWestCorner(new Location(1, 1))
                        .setNorthEastCorner(new Location(2, 1))::build)
                .withMessageMatching(".*northEast.*Longitude.*must be greater than southWest.*Longitude.*");

        assertThatIllegalStateException().isThrownBy(correctBuilder()
                        .setSouthWestCorner(new Location(1, 2))
                        .setNorthEastCorner(new Location(2, 1))::build)
                .withMessageMatching(".*northEast.*Longitude.*must be greater than southWest.*Longitude.*");
    }

    static VehicleRoutingDemoResource.DemoDataBuilder correctBuilder() {
        return VehicleRoutingDemoResource.DemoDataBuilder.builder().setMinDemand(1).setMaxDemand(2).setVehicleCapacity(15).setCustomerCount(77)
                .setVehicleCount(6).setDepotCount(2).setSouthWestCorner(new Location(43.751466, 11.177210))
                .setNorthEastCorner(new Location(43.809291, 11.290195));
    }
}
