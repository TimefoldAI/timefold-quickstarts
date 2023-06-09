package org.acme.vehiclerouting.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Depot.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Depot {

    private final long id;

    @JsonIdentityReference
    private final Location location;

    public Depot(long id, Location location) {
        this.id = id;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }
}
