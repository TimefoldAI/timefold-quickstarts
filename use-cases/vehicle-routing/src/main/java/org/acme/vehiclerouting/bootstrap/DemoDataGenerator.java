package org.acme.vehiclerouting.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.StartupEvent;
import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.acme.vehiclerouting.persistence.VehicleRoutingSolutionRepository;

@ApplicationScoped
public class DemoDataGenerator {

    private final VehicleRoutingSolutionRepository repository;

    public DemoDataGenerator(VehicleRoutingSolutionRepository repository) {
        this.repository = repository;
    }

    public void generateDemoData(@Observes StartupEvent startupEvent) {
        VehicleRoutingSolution problem = DemoDataBuilder.builder()
                .setMinDemand(1)
                .setMaxDemand(2)
                .setVehicleCapacity(25)
                .setCustomerCount(77)
                .setVehicleCount(6)
                .setDepotCount(2)
                .setSouthWestCorner(new Location(0L, 43.751466, 11.177210))
                .setNorthEastCorner(new Location(0L, 43.809291, 11.290195))
                .build();

        repository.update(problem);
    }
}
