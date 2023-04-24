package org.acme.vehiclerouting.persistence;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.vehiclerouting.domain.VehicleRoutingSolution;

@ApplicationScoped
public class VehicleRoutingSolutionRepository {

    private VehicleRoutingSolution vehicleRoutingSolution;

    public Optional<VehicleRoutingSolution> solution() {
        return Optional.ofNullable(vehicleRoutingSolution);
    }

    public void update(VehicleRoutingSolution vehicleRoutingSolution) {
        this.vehicleRoutingSolution = vehicleRoutingSolution;
    }
}
