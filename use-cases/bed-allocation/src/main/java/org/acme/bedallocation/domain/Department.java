package org.acme.bedallocation.domain;

import java.util.Map;
import java.util.Objects;

public record Department(
        String id,
        String name,
        Integer minimumAge,
        Integer maximumAge,
        Map<String, Integer> specialtyToPriority) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Department department)) {
            return false;
        }
        return Objects.equals(id, department.id);
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
