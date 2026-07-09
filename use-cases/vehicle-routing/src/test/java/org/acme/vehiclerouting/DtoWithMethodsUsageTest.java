package org.acme.vehiclerouting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.acme.vehiclerouting.dto.LocationDTO;
import org.acme.vehiclerouting.dto.VehicleDTO;
import org.acme.vehiclerouting.dto.VehicleIdDetail;
import org.acme.vehiclerouting.dto.VehicleRoutingConfigOverrides;
import org.acme.vehiclerouting.dto.VehicleRoutingInput;
import org.acme.vehiclerouting.dto.VehicleRoutingInputMetrics;
import org.acme.vehiclerouting.dto.VehicleRoutingOutput;
import org.acme.vehiclerouting.dto.VehicleRoutingOutputMetrics;
import org.acme.vehiclerouting.dto.VisitDTO;
import org.acme.vehiclerouting.dto.VisitIdDetail;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        OffsetDateTime base = OffsetDateTime.of(2027, 2, 1, 7, 30, 0, 0, ZoneOffset.UTC);

        var updatedLocation = new LocationDTO(1.0, 2.0)
                .withLatitude(3.0)
                .withLongitude(4.0);

        var baseVehicle = new VehicleDTO("1", 10, updatedLocation, base, List.of("v1"), 5, 100L, base);
        var updatedVehicle = baseVehicle
                .withId("2")
                .withCapacity(20)
                .withHomeLocation(updatedLocation)
                .withDepartureTime(base.plusHours(1))
                .withVisitIds(List.of("v2"))
                .withTotalDemand(7)
                .withTotalDrivingTimeSeconds(200L)
                .withArrivalTime(base.plusHours(2));

        var baseVisit = new VisitDTO("1", "John", updatedLocation, 10, base, base.plusHours(1), 1800L,
                "veh1", base, base, base, 50L);
        var updatedVisit = baseVisit
                .withId("2")
                .withName("Paul")
                .withLocation(updatedLocation)
                .withDemand(20)
                .withMinStartTime(base.plusHours(1))
                .withMaxEndTime(base.plusHours(2))
                .withServiceDurationSeconds(3600L)
                .withVehicleId("veh2")
                .withArrivalTime(base.plusHours(3))
                .withStartServiceTime(base.plusHours(3))
                .withDepartureTime(base.plusHours(4))
                .withDrivingTimeSecondsFromPreviousStandstill(60L);

        var updatedVisitIdDetail = new VisitIdDetail("1").withVisitId("2");
        var updatedVehicleIdDetail = new VehicleIdDetail("1").withVehicleId("2");

        var updatedOverrides = new VehicleRoutingConfigOverrides()
                .withMaximizeVisitsAssignedWeight(10L)
                .withMinimizeTravelTimeWeight(20L);

        var updatedInput = new VehicleRoutingInput("n", updatedLocation, updatedLocation, base, base.plusDays(1),
                List.of(baseVehicle), List.of(baseVisit))
                .withName("name")
                .withSouthWestCorner(updatedLocation)
                .withNorthEastCorner(updatedLocation)
                .withStartDateTime(base)
                .withEndDateTime(base.plusDays(1))
                .withVehicles(List.of(updatedVehicle))
                .withVisits(List.of(updatedVisit));

        var updatedOutput = new VehicleRoutingOutput(List.of(baseVehicle), List.of(baseVisit), "0hard/0medium/0soft")
                .withVehicles(List.of(updatedVehicle))
                .withVisits(List.of(updatedVisit))
                .withScore("0hard/0medium/1soft");

        var updatedInputMetrics = new VehicleRoutingInputMetrics(1, 2)
                .withVehicles(10)
                .withVisits(20);

        var updatedOutputMetrics = new VehicleRoutingOutputMetrics(1, 2, 3, 4L)
                .withAssignedVisits(10)
                .withUnassignedVisits(20)
                .withUsedVehicles(30)
                .withTotalDrivingTimeSeconds(40L);

        assertThat(updatedLocation.latitude()).isEqualTo(3.0);
        assertThat(updatedLocation.longitude()).isEqualTo(4.0);
        assertThat(updatedVehicle.id()).isEqualTo("2");
        assertThat(updatedVehicle.capacity()).isEqualTo(20);
        assertThat(updatedVehicle.visitIds()).containsExactly("v2");
        assertThat(updatedVehicle.totalDemand()).isEqualTo(7);
        assertThat(updatedVehicle.totalDrivingTimeSeconds()).isEqualTo(200L);
        assertThat(updatedVisit.id()).isEqualTo("2");
        assertThat(updatedVisit.name()).isEqualTo("Paul");
        assertThat(updatedVisit.demand()).isEqualTo(20);
        assertThat(updatedVisit.serviceDurationSeconds()).isEqualTo(3600L);
        assertThat(updatedVisit.vehicleId()).isEqualTo("veh2");
        assertThat(updatedVisit.drivingTimeSecondsFromPreviousStandstill()).isEqualTo(60L);
        assertThat(updatedVisitIdDetail.visitId()).isEqualTo("2");
        assertThat(updatedVehicleIdDetail.vehicleId()).isEqualTo("2");
        assertThat(updatedOverrides.maximizeVisitsAssignedWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.minimizeTravelTimeWeight()).isEqualTo(20L);
        assertThat(updatedInput.vehicles()).containsExactly(updatedVehicle);
        assertThat(updatedInput.visits()).containsExactly(updatedVisit);
        assertThat(updatedInput.name()).isEqualTo("name");
        assertThat(updatedOutput.vehicles()).containsExactly(updatedVehicle);
        assertThat(updatedOutput.visits()).containsExactly(updatedVisit);
        assertThat(updatedOutput.score()).isEqualTo("0hard/0medium/1soft");
        assertThat(updatedInputMetrics.vehicles()).isEqualTo(10);
        assertThat(updatedInputMetrics.visits()).isEqualTo(20);
        assertThat(updatedOutputMetrics.assignedVisits()).isEqualTo(10);
        assertThat(updatedOutputMetrics.unassignedVisits()).isEqualTo(20);
        assertThat(updatedOutputMetrics.usedVehicles()).isEqualTo(30);
        assertThat(updatedOutputMetrics.totalDrivingTimeSeconds()).isEqualTo(40L);
    }
}
