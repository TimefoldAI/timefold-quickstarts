package org.acme.bedallocation.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.bedallocation.dto.BedScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void buildsConsistentProblem() {
        BedScheduleInput problem = DemoDataBuilder.builder()
                .setRoomCount(4)
                .setBedsPerRoom(2)
                .setDayCount(7)
                .setStayCount(10)
                .build();

        assertThat(problem.departments()).hasSize(1);
        assertThat(problem.rooms()).hasSize(4);
        assertThat(problem.beds()).hasSize(8);
        assertThat(problem.stays()).hasSize(10);
        assertThat(problem.stays()).allSatisfy(stay -> {
            assertThat(stay.id()).isNotBlank();
            assertThat(stay.arrivalDate()).isNotBlank();
            assertThat(stay.departureDate()).isNotBlank();
            assertThat(stay.bedId()).isNull();
        });
        assertThat(problem.beds()).allSatisfy(bed -> assertThat(bed.roomId()).isNotNull());
    }
}
