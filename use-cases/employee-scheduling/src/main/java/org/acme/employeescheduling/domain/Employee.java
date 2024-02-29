package org.acme.employeescheduling.domain;

import java.util.Set;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Employee {
    @PlanningId
    String name;

    Set<String> skills;

    public Employee() {

    }

    public Employee(String name, Set<String> skills) {
        this.name = name;
        this.skills = skills;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getSkills() {
        return skills;
    }

    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }

    @Override
    public String toString() {
        return name;
    }
}
