package org.acme.employeescheduling.rest;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.ConstraintConfiguration;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple demo data generator used by the web demo.
 * This file had an accidental diff/patch fragment which broke compilation; restore a minimal valid implementation.
 */
@ApplicationScoped
public class DemoDataGenerator {

    public EmployeeSchedule generateDemoData(DemoDataParameters parameters) {
        EmployeeSchedule employeeSchedule = new EmployeeSchedule();

        // Provide empty collections so the JSON schema includes these fields in the demo responses.
        employeeSchedule.setEmployees(new java.util.ArrayList<>());
        employeeSchedule.setShifts(new java.util.ArrayList<>());
        employeeSchedule.setMustWorkTogetherList(java.util.Collections.emptyList());
        employeeSchedule.setConstraintConfiguration(new ConstraintConfiguration());

        return employeeSchedule;
    }
}
