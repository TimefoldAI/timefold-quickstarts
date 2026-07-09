package org.acme.facilitylocation.solver;

import java.util.ArrayList;
import java.util.List;

import org.acme.facilitylocation.dto.ConsumerDTO;
import org.acme.facilitylocation.dto.FacilityDTO;
import org.acme.facilitylocation.dto.FacilityLocationInput;
import org.acme.facilitylocation.dto.LocationDTO;

final class SolverTestDataFactory {

    private SolverTestDataFactory() {
    }

    static FacilityLocationInput createProblem() {
        int facilityCount = 10;
        int consumerCount = 150;
        long totalCapacity = 1200;
        long totalDemand = 900;

        List<FacilityDTO> facilities = new ArrayList<>(facilityCount);
        for (int i = 0; i < facilityCount; i++) {
            facilities.add(new FacilityDTO(
                    "facility-" + i,
                    "Facility " + i,
                    new LocationDTO(-10 + i, -10 + i),
                    1000,
                    totalCapacity / facilityCount,
                    0L,
                    false));
        }

        List<ConsumerDTO> consumers = new ArrayList<>(consumerCount);
        for (int i = 0; i < consumerCount; i++) {
            consumers.add(new ConsumerDTO(
                    "consumer-" + i,
                    new LocationDTO(-10 + (i % 20), -10 + ((i * 3) % 20)),
                    totalDemand / consumerCount,
                    "",
                    false));
        }

        return new FacilityLocationInput(
                facilities,
                consumers,
                List.of(new LocationDTO(-10, -10), new LocationDTO(10, 10)));
    }
}
