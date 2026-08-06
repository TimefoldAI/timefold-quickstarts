package org.acme.employeescheduling.rest;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.ConstraintConfiguration;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple demo data generator used by the web demo.
 * Restored a minimal valid implementation and a nested DemoData enum used by the demo resource.
 */
@ApplicationScoped
public class DemoDataGenerator {

    public static enum DemoData {
        SMALL
    }

    public EmployeeSchedule generateDemoData(DemoData demoData) {
        EmployeeSchedule employeeSchedule = new EmployeeSchedule();

        // Provide empty collections so the JSON schema includes these fields in the demo responses.
        employeeSchedule.setEmployees(new java.util.ArrayList<>());
        employeeSchedule.setShifts(new java.util.ArrayList<>());
        employeeSchedule.setMustWorkTogetherList(java.util.Collections.emptyList());
        employeeSchedule.setConstraintConfiguration(new ConstraintConfiguration());

        return employeeSchedule;
    }
}
