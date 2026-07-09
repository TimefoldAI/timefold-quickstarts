package org.acme.facilitylocation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.facilitylocation.dto.ConsumerDTO;
import org.acme.facilitylocation.dto.ConsumerIdDetail;
import org.acme.facilitylocation.dto.FacilityDTO;
import org.acme.facilitylocation.dto.FacilityIdDetail;
import org.acme.facilitylocation.dto.FacilityLocationConfigOverrides;
import org.acme.facilitylocation.dto.FacilityLocationInput;
import org.acme.facilitylocation.dto.FacilityLocationInputMetrics;
import org.acme.facilitylocation.dto.FacilityLocationOutput;
import org.acme.facilitylocation.dto.FacilityLocationOutputMetrics;
import org.acme.facilitylocation.dto.LocationDTO;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseLocation = new LocationDTO(1.0, 2.0);
        var updatedLocation = baseLocation.withLatitude(3.0).withLongitude(4.0);

        var baseFacility = new FacilityDTO("facility-1", "Facility", baseLocation, 10L, 20L, 5L, false);
        var updatedFacility = baseFacility.withId("facility-2")
                .withName("Updated Facility")
                .withLocation(updatedLocation)
                .withSetupCost(30L)
                .withCapacity(40L)
                .withUsedCapacity(15L)
                .withUsed(true);

        var baseConsumer = new ConsumerDTO("consumer-1", baseLocation, 5L, "", false);
        var updatedConsumer = baseConsumer.withId("consumer-2")
                .withLocation(updatedLocation)
                .withDemand(6L)
                .withFacilityId("facility-2")
                .withAssigned(true);

        var updatedConsumerIdDetail = new ConsumerIdDetail("consumer-1").withConsumerId("consumer-2");
        var updatedFacilityIdDetail = new FacilityIdDetail("facility-1").withFacilityId("facility-2");
        var updatedOverrides = new FacilityLocationConfigOverrides().withSetupCostWeight(10L)
                .withDistanceFromFacilityWeight(20L);

        var updatedInput = new FacilityLocationInput(List.of(baseFacility), List.of(baseConsumer), List.of(baseLocation))
                .withFacilities(List.of(updatedFacility))
                .withConsumers(List.of(updatedConsumer))
                .withBounds(List.of(updatedLocation));

        var updatedOutput = new FacilityLocationOutput(List.of(baseFacility), List.of(baseConsumer), "0hard", 10L, 11L,
                "12km", List.of(baseLocation))
                .withFacilities(List.of(updatedFacility))
                .withConsumers(List.of(updatedConsumer))
                .withScore("1hard")
                .withTotalCost(50L)
                .withPotentialCost(60L)
                .withTotalDistance("70km")
                .withBounds(List.of(updatedLocation));

        var updatedInputMetrics = new FacilityLocationInputMetrics(1, 2, 3L, 4L, 5L)
                .withFacilities(10)
                .withConsumers(20)
                .withTotalDemand(30L)
                .withTotalCapacity(40L)
                .withTotalPotentialSetupCost(50L);

        var updatedOutputMetrics = new FacilityLocationOutputMetrics(1, 2, 3L, 4, 5, 6L, 7L, 8.0)
                .withTotalActivatedFacilities(10)
                .withTotalUnusedFacilities(20)
                .withTotalSetupCost(30L)
                .withTotalAssignedConsumers(40)
                .withTotalUnassignedConsumers(50)
                .withTotalTravelDistanceMeters(60L)
                .withAverageTravelDistanceMetersPerConsumer(70L)
                .withCapacityUtilizationPercentage(80.0);

        assertThat(updatedLocation.latitude()).isEqualTo(3.0);
        assertThat(updatedLocation.longitude()).isEqualTo(4.0);
        assertThat(updatedFacility.id()).isEqualTo("facility-2");
        assertThat(updatedFacility.name()).isEqualTo("Updated Facility");
        assertThat(updatedFacility.location()).isEqualTo(updatedLocation);
        assertThat(updatedFacility.setupCost()).isEqualTo(30L);
        assertThat(updatedFacility.capacity()).isEqualTo(40L);
        assertThat(updatedFacility.usedCapacity()).isEqualTo(15L);
        assertThat(updatedFacility.used()).isTrue();
        assertThat(updatedConsumer.id()).isEqualTo("consumer-2");
        assertThat(updatedConsumer.location()).isEqualTo(updatedLocation);
        assertThat(updatedConsumer.demand()).isEqualTo(6L);
        assertThat(updatedConsumer.facilityId()).isEqualTo("facility-2");
        assertThat(updatedConsumer.assigned()).isTrue();
        assertThat(updatedConsumerIdDetail.consumerId()).isEqualTo("consumer-2");
        assertThat(updatedFacilityIdDetail.facilityId()).isEqualTo("facility-2");
        assertThat(updatedOverrides.setupCostWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.distanceFromFacilityWeight()).isEqualTo(20L);
        assertThat(updatedInput.facilities()).containsExactly(updatedFacility);
        assertThat(updatedInput.consumers()).containsExactly(updatedConsumer);
        assertThat(updatedInput.bounds()).containsExactly(updatedLocation);
        assertThat(updatedOutput.facilities()).containsExactly(updatedFacility);
        assertThat(updatedOutput.consumers()).containsExactly(updatedConsumer);
        assertThat(updatedOutput.score()).isEqualTo("1hard");
        assertThat(updatedOutput.totalCost()).isEqualTo(50L);
        assertThat(updatedOutput.potentialCost()).isEqualTo(60L);
        assertThat(updatedOutput.totalDistance()).isEqualTo("70km");
        assertThat(updatedOutput.bounds()).containsExactly(updatedLocation);
        assertThat(updatedInputMetrics.facilities()).isEqualTo(10);
        assertThat(updatedInputMetrics.consumers()).isEqualTo(20);
        assertThat(updatedInputMetrics.totalDemand()).isEqualTo(30L);
        assertThat(updatedInputMetrics.totalCapacity()).isEqualTo(40L);
        assertThat(updatedInputMetrics.totalPotentialSetupCost()).isEqualTo(50L);
        assertThat(updatedOutputMetrics.totalActivatedFacilities()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnusedFacilities()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalSetupCost()).isEqualTo(30L);
        assertThat(updatedOutputMetrics.totalAssignedConsumers()).isEqualTo(40);
        assertThat(updatedOutputMetrics.totalUnassignedConsumers()).isEqualTo(50);
        assertThat(updatedOutputMetrics.totalTravelDistanceMeters()).isEqualTo(60L);
        assertThat(updatedOutputMetrics.averageTravelDistanceMetersPerConsumer()).isEqualTo(70L);
        assertThat(updatedOutputMetrics.capacityUtilizationPercentage()).isEqualTo(80.0);
    }
}
