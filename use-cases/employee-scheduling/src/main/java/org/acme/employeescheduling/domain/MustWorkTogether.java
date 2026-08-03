package org.acme.employeescheduling.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Problem fact: when employeeA works a shift, employeeB must also work that same shift.
 */
public class MustWorkTogether {

    private Employee employeeA;
    private Employee employeeB;

    public MustWorkTogether() {
        // No-arg constructor for JSON deserialization
    }

    @JsonCreator
    public MustWorkTogether(@JsonProperty("employeeA") Employee employeeA,
                            @JsonProperty("employeeB") Employee employeeB) {
        this.employeeA = Objects.requireNonNull(employeeA, "employeeA");
        this.employeeB = Objects.requireNonNull(employeeB, "employeeB");
    }

    public Employee getEmployeeA() {
        return employeeA;
    }

    public void setEmployeeA(Employee employeeA) {
        this.employeeA = employeeA;
    }

    public Employee getEmployeeB() {
        return employeeB;
    }

    public void setEmployeeB(Employee employeeB) {
        this.employeeB = employeeB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MustWorkTogether)) return false;
        MustWorkTogether that = (MustWorkTogether) o;
        return Objects.equals(employeeA, that.employeeA) && Objects.equals(employeeB, that.employeeB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeA, employeeB);
    }

    @Override
    public String toString() {
        return "MustWorkTogether{" + employeeA + " <-> " + employeeB + '}';
    }
}
