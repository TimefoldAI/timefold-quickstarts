package org.acme.projectjobschedule.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.acme.projectjobschedule.dto.ProjectJobScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        ProjectJobScheduleInput problem = correctBuilder().build();

        assertEquals(2, problem.projects().size());
        assertEquals(7, problem.resources().size());
        assertEquals(24, problem.jobs().size());
        assertFalse(problem.allocations().isEmpty());
        problem.allocations().forEach(allocation -> assertNotNull(allocation.id()));
    }

    @Test
    void jobCountAtLeastTwo() {
        DemoDataBuilder builder = correctBuilder().setJobCount(1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void atLeastOneProject() {
        DemoDataBuilder builder = DemoDataBuilder.builder().setJobCount(24).addGlobalResource("0", 16);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void atLeastOneResource() {
        DemoDataBuilder builder = DemoDataBuilder.builder().setJobCount(24).addProject("0", 0, 10);
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder()
                .setJobCount(24)
                .addProject("0", 0, 10)
                .addProject("1", 4, 19)
                .addGlobalResource("0", 16)
                .addLocalResource("1", "0", 13, true)
                .addLocalResource("2", "0", 44, false)
                .addLocalResource("3", "0", 39, false)
                .addLocalResource("4", "1", 24, true)
                .addLocalResource("5", "1", 66, false)
                .addLocalResource("6", "1", 56, false);
    }
}
