package org.acme.projectjobschedule.domain.resource;

import java.util.Objects;

public abstract class Resource {

    private String id;
    private int capacity;

    protected Resource() {
    }

    protected Resource(String id) {
        this.id = id;
    }

    protected Resource(String id, int capacity) {
        this(id);
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public abstract boolean isRenewable();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource resource)) {
            return false;
        }
        return Objects.equals(getId(), resource.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
