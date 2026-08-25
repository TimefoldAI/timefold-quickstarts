package org.acme.bedallocation.demo;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

import org.acme.bedallocation.dto.BedPlanInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        BedPlanInput problem = DemoDataBuilder.builder().build();

        assertThat(problem.departments()).hasSize(1);
        assertThat(problem.departments().get(0).rooms()).hasSize(15);
        assertThat(problem.departments().get(0).minimumAge()).isEqualTo(1);
        assertThat(problem.departments().get(0).maximumAge()).isEqualTo(100);
        assertThat(problem.stays()).hasSize(111);
        int bedCount = problem.departments().get(0).rooms().stream().mapToInt(room -> room.beds().size()).sum();
        assertThat(bedCount).isEqualTo(23);
        problem.stays().forEach(stay -> {
            assertThat(stay.id()).isNotNull();
            assertThat(stay.arrivalDate()).isNotNull();
            assertThat(stay.departureDate()).isNotNull();
            assertThat(stay.bedId()).isNull();

            long dayCount = DAYS.between(stay.arrivalDate(), stay.departureDate());
            assertThat(dayCount).isBetween(1L, 6L);
        });
    }
}
