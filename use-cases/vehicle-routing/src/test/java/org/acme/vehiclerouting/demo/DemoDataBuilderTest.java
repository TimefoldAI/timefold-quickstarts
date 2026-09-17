package org.acme.vehiclerouting.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.function.Supplier;

import org.acme.vehiclerouting.dto.input.LocationInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    private static final Map<DemoDataBuilder.Dataset, Supplier<VehicleRoutePlanInput>> DATASETS = Map.of(
            DemoDataBuilder.BASIC, DemoDataBuilder::basic,
            DemoDataBuilder.PHILADELPHIA, DemoDataBuilder::philadelphia,
            DemoDataBuilder.HARTFORT, DemoDataBuilder::hartfort,
            DemoDataBuilder.FIRENZE, DemoDataBuilder::firenze);

    @Test
    void shouldBuildData() {
        assertThat(DemoDataBuilder.basic().visits()).hasSize(65);
        assertThat(DemoDataBuilder.philadelphia().visits()).hasSize(55);
        assertThat(DemoDataBuilder.hartfort().visits()).hasSize(50);
        assertThat(DemoDataBuilder.firenze().visits()).hasSize(77);

        DATASETS.forEach((bounds, dataset) -> {
            VehicleRoutePlanInput problem = dataset.get();

            assertThat(problem.vehicles()).hasSize(6);
            assertThat(problem.endDateTime()).isAfter(problem.startDateTime());

            problem.vehicles().forEach(vehicle -> {
                assertThat(vehicle.id()).isNotNull();
                assertThat(vehicle.capacity()).isPositive();
                assertThat(vehicle.departureTime()).isEqualTo(problem.startDateTime());
                // Unsolved: no route yet.
                assertThat(vehicle.visitIds()).isEmpty();
                assertWithinBounds(bounds, vehicle.homeLocation());
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
                assertWithinBounds(bounds, visit.location());
            });
        });
    }

    @Test
    void shouldBuildTheSameDataTwice() {
        // Every dataset is seeded, so two builds of the same one are identical; only the date they
        // are planned on moves, and it is anchored to today either way.
        DATASETS.values().forEach(dataset -> assertThat(dataset.get()).isEqualTo(dataset.get()));
    }

    @Test
    void shouldBuildDistinctDatasets() {
        assertThat(DATASETS.values().stream().map(Supplier::get).distinct()).hasSize(DATASETS.size());
    }

    private static void assertWithinBounds(DemoDataBuilder.Dataset bounds, LocationInputDTO location) {
        assertThat(location.latitude())
                .isBetween(bounds.southWestCorner().latitude(), bounds.northEastCorner().latitude());
        assertThat(location.longitude())
                .isBetween(bounds.southWestCorner().longitude(), bounds.northEastCorner().longitude());
    }
}
