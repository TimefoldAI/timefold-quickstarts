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

    // Parallel to DATASETS: the map area each of those datasets generates its locations within.
    // Not part of VehicleRoutePlanInput itself - that bounding box only ever existed to generate
    // demo data, not for the UI - so it is asserted here against DemoDataBuilder's own bounds.
    private static final List<DemoDataBuilder.Dataset> DATASET_BOUNDS = List.of(
            DemoDataBuilder.PHILADELPHIA, DemoDataBuilder.GHENT, DemoDataBuilder.HARTFORT,
            DemoDataBuilder.FIRENZE);

    @Test
    void shouldBuildData() {
        assertThat(DemoDataBuilder.philadelphia().visits()).hasSize(55);
        assertThat(DemoDataBuilder.ghent().visits()).hasSize(65);
        assertThat(DemoDataBuilder.hartfort().visits()).hasSize(50);
        assertThat(DemoDataBuilder.firenze().visits()).hasSize(77);

        for (int i = 0; i < DATASETS.size(); i++) {
            VehicleRoutePlanInput problem = DATASETS.get(i).get();
            DemoDataBuilder.Dataset bounds = DATASET_BOUNDS.get(i);

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
        }
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

    private static void assertWithinBounds(DemoDataBuilder.Dataset bounds, LocationInputDTO location) {
        assertThat(location.latitude())
                .isBetween(bounds.southWestCorner().latitude(), bounds.northEastCorner().latitude());
        assertThat(location.longitude())
                .isBetween(bounds.southWestCorner().longitude(), bounds.northEastCorner().longitude());
    }
}
