package org.acme.vehiclerouting.solver;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.acme.vehiclerouting.dto.LocationDTO;
import org.acme.vehiclerouting.dto.VehicleDTO;
import org.acme.vehiclerouting.dto.VehicleRoutingInput;
import org.acme.vehiclerouting.dto.VisitDTO;

final class SolverTestDataFactory {

    private static final OffsetDateTime BASE = OffsetDateTime.of(2027, 2, 1, 7, 30, 0, 0, ZoneOffset.UTC);

    private SolverTestDataFactory() {
    }

    static VehicleRoutingInput createProblem() {
        LocationDTO southWest = new LocationDTO(49.1767533245638, 16.50422914190477);
        LocationDTO northEast = new LocationDTO(49.288087, 16.624466);
        OffsetDateTime startDateTime = BASE;
        OffsetDateTime endDateTime = BASE.plusDays(1L);

        OffsetDateTime unsetTime = null;
        String unassignedVehicle = null;

        List<VehicleDTO> vehicles = List.of(
                new VehicleDTO("1", 100, new LocationDTO(49.288087, 16.562172), startDateTime,
                        List.of(), 0, 0L, unsetTime),
                new VehicleDTO("2", 100, new LocationDTO(49.190922, 16.624466), startDateTime,
                        List.of(), 0, 0L, unsetTime));

        OffsetDateTime windowStart = OffsetDateTime.of(2027, 2, 1, 8, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime windowEnd = OffsetDateTime.of(2027, 2, 1, 18, 0, 0, 0, ZoneOffset.UTC);
        List<VisitDTO> visits = List.of(
                new VisitDTO("2", "John", new LocationDTO(49.190922, 16.624466), 40,
                        windowStart, windowEnd, 1800L, unassignedVehicle, unsetTime, unsetTime, unsetTime, 0L),
                new VisitDTO("3", "Paul", new LocationDTO(49.1767533245638, 16.50422914190477), 40,
                        windowStart, windowEnd, 1800L, unassignedVehicle, unsetTime, unsetTime, unsetTime, 0L),
                new VisitDTO("4", "Ann", new LocationDTO(49.250000, 16.560000), 40,
                        windowStart, windowEnd, 1800L, unassignedVehicle, unsetTime, unsetTime, unsetTime, 0L));

        return new VehicleRoutingInput("test", southWest, northEast, startDateTime, endDateTime, vehicles, visits);
    }
}
