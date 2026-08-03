package org.acme.employeescheduling.domain;

import java.util.Objects;

/**
 * Problem fact: when employeeA works a shift, employeeB must also work that same shift.
 */
public final class MustWorkTogether {

    private final Employee employeeA;
    private final Employee employeeB;

    public MustWorkTogether(Employee employeeA, Employee employeeB) {
        this.employeeA = Objects.requireNonNull(employeeA, "employeeA");
        this.employeeB = Objects.requireNonNull(employeeB, "employeeB");
    }

    public Employee getEmployeeA() {
        return employeeA;
    }

    public Employee getEmployeeB() {
        return employeeB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MustWorkTogether)) return false;
        MustWorkTogether that = (MustWorkTogether) o;
        return employeeA.equals(that.employeeA) && employeeB.equals(that.employeeB);
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
