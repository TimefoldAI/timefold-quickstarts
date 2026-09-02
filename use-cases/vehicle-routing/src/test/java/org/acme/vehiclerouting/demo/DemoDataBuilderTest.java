package org.acme.vehiclerouting.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Supplier;

import org.acme.vehiclerouting.dto.input.LocationInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    private static final List<Supplier<VehicleRoutePlanInput>> DATASETS = List.of(
            DemoDataBuilder::philadelphia, DemoDataBuilder::ghent, DemoDataBuilder::hartfort,
            DemoDataBuilder::firenze);

    @Test
    void shouldBuildData() {
        assertThat(DemoDataBuilder.philadelphia().visits()).hasSize(55);
        assertThat(DemoDataBuilder.ghent().visits()).hasSize(65);
        assertThat(DemoDataBuilder.hartfort().visits()).hasSize(50);
        assertThat(DemoDataBuilder.firenze().visits()).hasSize(77);

        DATASETS.forEach(dataset -> {
            VehicleRoutePlanInput problem = dataset.get();

            assertThat(problem.vehicles()).hasSize(6);
            assertThat(problem.endDateTime()).isAfter(problem.startDateTime());

            problem.vehicles().forEach(vehicle -> {
                assertThat(vehicle.id()).isNotNull();
                assertThat(vehicle.capacity()).isPositive();
                assertThat(vehicle.departureTime()).isEqualTo(problem.startDateTime());
                // Unsolved: no route yet.
                assertThat(vehicle.visitIds()).isEmpty();
                assertWithinBounds(problem, vehicle.homeLocation());
            });

            problem.visits().forEach(visit -> {
                assertThat(visit.id()).isNotNull();
                assertThat(visit.name()).isNotNull();
                assertThat(visit.demand()).isPositive();
                assertThat(visit.serviceDurationMinutes()).isBetween(10, 40);
                // The time window is inside the planning window and holds the service duration.
                assertThat(visit.minStartTime()).isAfterOrEqualTo(problem.startDateTime());
                assertThat(visit.maxEndTime()).isBeforeOrEqualTo(problem.endDateTime());
                assertThat(visit.minStartTime().plusMinutes(visit.serviceDurationMinutes()))
                        .isBeforeOrEqualTo(visit.maxEndTime());
                assertWithinBounds(problem, visit.location());
            });
        });
    }

    @Test
    void shouldBuildTheSameDataTwice() {
        // Every dataset is seeded, so two builds of the same one are identical; only the date they
        // are planned on moves, and it is anchored to today either way.
        DATASETS.forEach(dataset -> assertThat(dataset.get()).isEqualTo(dataset.get()));
    }

    @Test
    void shouldBuildDistinctDatasets() {
        assertThat(DATASETS.stream().map(Supplier::get).distinct()).hasSameSizeAs(DATASETS);
    }

    private static void assertWithinBounds(VehicleRoutePlanInput problem, LocationInputDTO location) {
        assertThat(location.latitude())
                .isBetween(problem.southWestCorner().latitude(), problem.northEastCorner().latitude());
        assertThat(location.longitude())
                .isBetween(problem.southWestCorner().longitude(), problem.northEastCorner().longitude());
    }
}
