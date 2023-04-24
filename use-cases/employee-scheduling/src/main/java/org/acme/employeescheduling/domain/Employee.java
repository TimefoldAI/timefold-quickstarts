package org.acme.employeescheduling.domain;

import java.util.Set;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

@Entity
public class Employee {
    @Id
    @PlanningId
    String name;

    @ElementCollection(fetch = FetchType.EAGER)
    Set<String> skillSet;

    public Employee() {

    }

    public Employee(String name, Set<String> skillSet) {
        this.name = name;
        this.skillSet = skillSet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(Set<String> skillSet) {
        this.skillSet = skillSet;
    }

    @Override
    public String toString() {
        return name;
    }
}
