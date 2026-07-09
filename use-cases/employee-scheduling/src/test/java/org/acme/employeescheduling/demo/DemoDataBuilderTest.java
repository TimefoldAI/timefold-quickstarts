package org.acme.employeescheduling.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.acme.employeescheduling.dto.EmployeeScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void buildBasicDemo() {
        EmployeeScheduleInput input = DemoDataBuilder.builder()
                .setDaysInSchedule(7)
                .setEmployeeCount(5)
                .build();

        assertNotNull(input);
        assertFalse(input.employees().isEmpty());
        assertFalse(input.shifts().isEmpty());
        input.shifts().forEach(shift -> assertNotNull(shift.id()));
    }
}
