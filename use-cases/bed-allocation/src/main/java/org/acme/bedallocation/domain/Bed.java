package org.acme.bedallocation.domain;

import java.util.Objects;

public record Bed(
        String id,
        Room room) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bed bed)) {
            return false;
        }
        return Objects.equals(id, bed.id);
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
