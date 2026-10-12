package org.acme.facilitylocation.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.facilitylocation.dto.input.ConsumerInputDTO;
import org.acme.facilitylocation.dto.input.FacilityInputDTO;
import org.acme.facilitylocation.dto.input.FacilityPlanInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        FacilityPlanInput problem = DemoDataBuilder.builder().build();

        assertThat(problem.facilities()).hasSize(30);
        assertThat(problem.consumers()).hasSize(60);

        problem.facilities().forEach(facility -> {
            assertThat(facility.id()).isNotNull();
            assertThat(facility.capacity()).isEqualTo(150);
            assertThat(facility.setupCost()).isPositive();
            assertThat(facility.location().latitude()).isBetween(51.44, 51.56);
            assertThat(facility.location().longitude()).isBetween(-0.16, -0.01);
        });
        problem.consumers().forEach(consumer -> {
            assertThat(consumer.id()).isNotNull();
            assertThat(consumer.demand()).isEqualTo(15);
            assertThat(consumer.facilityId()).isNull();
            assertThat(consumer.location().latitude()).isBetween(51.44, 51.56);
            assertThat(consumer.location().longitude()).isBetween(-0.16, -0.01);
        });

        // Every consumer has to be served, so the dataset is only solvable if it is not over-constrained.
        long totalCapacity = problem.facilities().stream().mapToLong(FacilityInputDTO::capacity).sum();
        long totalDemand = problem.consumers().stream().mapToLong(ConsumerInputDTO::demand).sum();
        assertThat(totalCapacity).isEqualTo(4_500);
        assertThat(totalDemand).isEqualTo(900);
    }

    @Test
    void shouldBeReproducible() {
        assertThat(DemoDataBuilder.builder().build()).isEqualTo(DemoDataBuilder.builder().build());
    }
}
