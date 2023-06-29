package org.acme.vehiclerouting.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Depot.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Depot {

    private final String id;

    private final Location location;

    public Depot(@JsonProperty("id") String id, @JsonProperty("location") Location location) {
        this.id = id;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }
}
