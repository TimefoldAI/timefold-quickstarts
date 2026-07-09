package org.acme.orderpicking.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.acme.orderpicking.dto.OrderPickingInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        OrderPickingInput problem = correctBuilder().build();

        assertEquals(5, problem.trolleys().size());
        problem.trolleys().forEach(trolley -> {
            assertNotNull(trolley.id());
            assertTrue(trolley.pickTaskIds().isEmpty());
        });
        assertFalse(problem.pickTasks().isEmpty());
        problem.pickTasks().forEach(pickTask -> {
            assertNotNull(pickTask.id());
            assertNotNull(pickTask.location());
        });
    }

    @Test
    void trolleyCountGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setTrolleyCount(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void bucketCountGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setBucketCount(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void orderCountGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setOrderCount(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void bucketCapacityMustFitLargestProduct() {
        DemoDataBuilder builder = correctBuilder().setBucketCapacity(1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder()
                .setTrolleyCount(5)
                .setBucketCount(4)
                .setBucketCapacity(60 * 40 * 20)
                .setOrderCount(8);
    }
}
