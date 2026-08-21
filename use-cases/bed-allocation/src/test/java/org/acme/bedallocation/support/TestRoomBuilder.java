package org.acme.bedallocation.support;

import java.util.ArrayList;
import java.util.List;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Room;

/**
 * Builds a {@link Room} for tests, so a test only has to state the fields it actually cares about.
 * <p>
 * Production code calls the {@link Room} constructor directly; this builder deliberately lives in the test
 * sources so the domain class stays free of construction scaffolding.
 */
public final class TestRoomBuilder {

    private final String id;
    private String name;
    private Department department;
    private int capacity;
    private GenderLimitation genderLimitation = GenderLimitation.ANY_GENDER;
    private List<String> equipments = List.of();
    private final List<Bed> beds = new ArrayList<>();

    private TestRoomBuilder(String id) {
        this.id = id;
        this.name = id;
    }

    public static TestRoomBuilder aRoom(String id) {
        return new TestRoomBuilder(id);
    }

    public TestRoomBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TestRoomBuilder department(Department department) {
        this.department = department;
        return this;
    }

    public TestRoomBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public TestRoomBuilder genderLimitation(GenderLimitation genderLimitation) {
        this.genderLimitation = genderLimitation;
        return this;
    }

    public TestRoomBuilder equipments(List<String> equipments) {
        this.equipments = equipments;
        return this;
    }

    public Room build() {
        Room room = new Room(id, name, department, capacity, genderLimitation, equipments, beds);
        if (department != null) {
            department.rooms().add(room);
        }
        return room;
    }
}
