package org.acme.bedallocation.domain;

import java.util.Objects;
import java.util.Set;

public record Room(
        String id,
        String name,
        Department department,
        int capacity,
        GenderLimitation genderLimitation,
        Set<String> equipments) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Room room)) {
            return false;
        }
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
