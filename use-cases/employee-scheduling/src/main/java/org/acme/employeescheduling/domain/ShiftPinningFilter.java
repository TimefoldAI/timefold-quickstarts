package org.acme.employeescheduling.domain;

import ai.timefold.solver.core.api.domain.entity.PinningFilter;

public class ShiftPinningFilter implements PinningFilter<EmployeeSchedule, Shift> {

    @Override
    public boolean accept(EmployeeSchedule employeeSchedule, Shift shift) {
        ScheduleState scheduleState = employeeSchedule.getScheduleState();
        return !scheduleState.isDraft(shift);
    }
}
