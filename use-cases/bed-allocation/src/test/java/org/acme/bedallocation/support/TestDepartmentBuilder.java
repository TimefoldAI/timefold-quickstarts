package org.acme.bedallocation.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Room;

/**
 * Builds a {@link Department} for tests, so a test only has to state the fields it actually cares about.
 * <p>
 * Production code calls the {@link Department} constructor directly; this builder deliberately lives in
 * the test sources so the domain class stays free of construction scaffolding.
 */
public final class TestDepartmentBuilder {

    private final String id;
    private String name;
    private Integer minimumAge;
    private Integer maximumAge;
    private Map<String, Integer> specialtyToPriority = Map.of();
    private final List<Room> rooms = new ArrayList<>();

    private TestDepartmentBuilder(String id) {
        this.id = id;
        this.name = id;
    }

    public static TestDepartmentBuilder aDepartment(String id) {
        return new TestDepartmentBuilder(id);
    }

    public TestDepartmentBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TestDepartmentBuilder minimumAge(Integer minimumAge) {
        this.minimumAge = minimumAge;
        return this;
    }

    public TestDepartmentBuilder maximumAge(Integer maximumAge) {
        this.maximumAge = maximumAge;
        return this;
    }

    public TestDepartmentBuilder specialtyToPriority(Map<String, Integer> specialtyToPriority) {
        this.specialtyToPriority = specialtyToPriority;
        return this;
    }

    public Department build() {
        return new Department(id, name, minimumAge, maximumAge, specialtyToPriority, rooms);
    }
}
