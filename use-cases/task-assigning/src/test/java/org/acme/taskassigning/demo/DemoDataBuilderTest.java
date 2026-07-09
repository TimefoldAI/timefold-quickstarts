package org.acme.taskassigning.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.acme.taskassigning.dto.TaskAssigningInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        TaskAssigningInput problem = DemoDataBuilder.builder().build();

        assertEquals(4, problem.taskTypes().size());
        assertEquals(4, problem.customers().size());
        assertEquals(8, problem.employees().size());
        assertEquals(28, problem.tasks().size());

        problem.employees().forEach(employee -> {
            assertNotNull(employee.id());
            assertTrue(employee.taskIds().isEmpty());
            assertEquals(4, employee.affinities().size());
        });
        problem.tasks().forEach(task -> {
            assertNotNull(task.id());
            assertNotNull(task.taskTypeCode());
            assertNotNull(task.priority());
        });
        assertFalse(problem.tasks().isEmpty());
    }
}
