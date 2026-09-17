package org.acme.vehiclerouting.support;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.service.maps.api.model.Location;
import ai.timefold.solver.service.maps.haversine.impl.HaversineTravelTimeAndDistanceMatrixProvider;
import ai.timefold.solver.service.maps.service.test.api.TestDistanceCalculator;

import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.dto.input.LocationInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.acme.vehiclerouting.dto.input.VisitInputDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    /** The date every test dataset is planned on, so that a time window is fully determined by its clock time. */
    public static final OffsetDateTime DAY_START = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    /** Two coordinates in the same city, so a driving time between them is a few minutes, not hours. */
    public static final LocationInputDTO SOUTH_WEST_CORNER = new LocationInputDTO(50.990000, 3.620000);
    public static final LocationInputDTO NORTH_EAST_CORNER = new LocationInputDTO(51.130000, 3.840000);

    public static final HaversineTravelTimeAndDistanceMatrixProvider provider =
            new HaversineTravelTimeAndDistanceMatrixProvider(new ObjectMapper());

    private TestHelper() {
    }

    public static OffsetDateTime at(int hour, int minute) {
        return DAY_START.with(LocalTime.of(hour, minute));
    }

    public static LocationInputDTO locationDTO(double latitude, double longitude) {
        return new LocationInputDTO(latitude, longitude);
    }

    public static VehicleRoutePlanInput input(List<VehicleInputDTO> vehicles, List<VisitInputDTO> visits) {
        return new VehicleRoutePlanInput(at(7, 30), DAY_START.plusDays(1), vehicles, visits);
    }

    /**
     * @return a small but comfortably feasible problem: two vehicles with room to spare, four
     *         nearby visits and time windows wide enough that every visit fits on a route
     */
    public static VehicleRoutePlanInput createProblem() {
        List<VehicleInputDTO> vehicles = List.of(
                aVehicleDTO("1").capacity(20).location(51.00, 3.65).build(),
                aVehicleDTO("2").capacity(20).location(51.05, 3.75).build());
        List<VisitInputDTO> visits = List.of(
                aVisitDTO("1").name("Amy Cole").location(51.01, 3.66).build(),
                aVisitDTO("2").name("Beth Fox").location(51.02, 3.68).build(),
                aVisitDTO("3").name("Carl Green").location(51.06, 3.76).build(),
                aVisitDTO("4").name("Dan Jones").location(51.07, 3.78).build());
        return input(vehicles, visits);
    }

    public static VehicleDTOBuilder aVehicleDTO(String id) {
        return new VehicleDTOBuilder(id);
    }

    public static VisitDTOBuilder aVisitDTO(String id) {
        return new VisitDTOBuilder(id);
    }

    public static VehicleBuilder aVehicle(String id) {
        return new VehicleBuilder(id);
    }

    public static VisitBuilder aVisit(String id) {
        return new VisitBuilder(id);
    }

    public static RoutePlanBuilder aRoutePlan() {
        return new RoutePlanBuilder();
    }

    /**
     * The driving time the solver model itself would compute between two coordinates, so that a
     * travel time expectation in a test is derived rather than hard-coded to a magic number.
     */
    public static long drivingTimeSeconds(double fromLatitude, double fromLongitude, double toLatitude,
            double toLongitude) {
        return provider.calculateTravelTime(new Location(fromLatitude, fromLongitude), new Location(toLatitude, toLongitude));
    }

    public static final class VehicleDTOBuilder {

        private final String id;
        private Integer capacity = 100;
        private LocationInputDTO homeLocation = SOUTH_WEST_CORNER;
        private OffsetDateTime departureTime = at(7, 30);
        private List<String> visitIds;

        private VehicleDTOBuilder(String id) {
            this.id = id;
        }

        public VehicleDTOBuilder capacity(Integer capacity) {
            this.capacity = capacity;
            return this;
        }

        public VehicleDTOBuilder location(double latitude, double longitude) {
            this.homeLocation = locationDTO(latitude, longitude);
            return this;
        }

        public VehicleDTOBuilder homeLocation(LocationInputDTO homeLocation) {
            this.homeLocation = homeLocation;
            return this;
        }

        public VehicleDTOBuilder departureTime(OffsetDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public VehicleDTOBuilder visitIds(List<String> visitIds) {
            this.visitIds = visitIds;
            return this;
        }

        public VehicleInputDTO build() {
            return new VehicleInputDTO(id, capacity, homeLocation, departureTime, visitIds);
        }
    }

    public static final class VisitDTOBuilder {

        private final String id;
        private String name;
        private LocationInputDTO location = NORTH_EAST_CORNER;
        private Integer demand = 1;
        private OffsetDateTime minStartTime = at(8, 0);
        private OffsetDateTime maxEndTime = at(18, 0);
        private Integer serviceDurationMinutes = 10;

        private VisitDTOBuilder(String id) {
            this.id = id;
            this.name = "Visit " + id;
        }

        public VisitDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VisitDTOBuilder location(double latitude, double longitude) {
            this.location = locationDTO(latitude, longitude);
            return this;
        }

        public VisitDTOBuilder demand(Integer demand) {
            this.demand = demand;
            return this;
        }

        public VisitDTOBuilder minStartTime(OffsetDateTime minStartTime) {
            this.minStartTime = minStartTime;
            return this;
        }

        public VisitDTOBuilder maxEndTime(OffsetDateTime maxEndTime) {
            this.maxEndTime = maxEndTime;
            return this;
        }

        public VisitDTOBuilder serviceDurationMinutes(Integer serviceDurationMinutes) {
            this.serviceDurationMinutes = serviceDurationMinutes;
            return this;
        }

        public VisitInputDTO build() {
            return new VisitInputDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationMinutes);
        }
    }

    public static final class VehicleBuilder {

        private final String id;
        private int capacity = 100;
        private double latitude = SOUTH_WEST_CORNER.latitude();
        private double longitude = SOUTH_WEST_CORNER.longitude();
        private OffsetDateTime departureTime = at(8, 0);

        private VehicleBuilder(String id) {
            this.id = id;
        }

        public VehicleBuilder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public VehicleBuilder location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            return this;
        }

        public VehicleBuilder departureTime(OffsetDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public Vehicle build() {
            return new Vehicle(id, capacity, new Location(latitude, longitude), departureTime);
        }
    }

    public static final class VisitBuilder {

        private final String id;
        private String name;
        private double latitude = SOUTH_WEST_CORNER.latitude();
        private double longitude = SOUTH_WEST_CORNER.longitude();
        private int demand = 1;
        private OffsetDateTime minStartTime = at(8, 0);
        private OffsetDateTime maxEndTime = at(18, 0);
        private Duration serviceDuration = Duration.ofMinutes(10);

        private VisitBuilder(String id) {
            this.id = id;
            this.name = "Visit " + id;
        }

        public VisitBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VisitBuilder location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            return this;
        }

        public VisitBuilder demand(int demand) {
            this.demand = demand;
            return this;
        }

        public VisitBuilder minStartTime(OffsetDateTime minStartTime) {
            this.minStartTime = minStartTime;
            return this;
        }

        public VisitBuilder maxEndTime(OffsetDateTime maxEndTime) {
            this.maxEndTime = maxEndTime;
            return this;
        }

        public VisitBuilder serviceDurationMinutes(long serviceDurationMinutes) {
            this.serviceDuration = Duration.ofMinutes(serviceDurationMinutes);
            return this;
        }

        public Visit build() {
            return new Visit(id, name, new Location(latitude, longitude), demand, minStartTime, maxEndTime,
                    serviceDuration);
        }
    }

    /**
     * Builds the entity array to hand to {@code ConstraintVerifier.given(...)}.
     * <p>
     * ConstraintVerifier does not run the solver's variable listeners, so the shadow variables
     * derived from the route lists - the inverse relation, the previous element and the arrival
     * time - have to be filled in here, exactly as the solver would otherwise do when it loads a
     * solution.
     */
    public static final class RoutePlanBuilder {

        private final List<Vehicle> vehicles = new ArrayList<>();
        private final List<Visit> visits = new ArrayList<>();

        private RoutePlanBuilder() {
        }

        /**
         * Adds a vehicle whose route services the given visits, in that order.
         */
        public RoutePlanBuilder route(VehicleBuilder vehicleBuilder, VisitBuilder... visitBuilders) {
            Vehicle vehicle = vehicleBuilder.build();
            List<Visit> route = new ArrayList<>(visitBuilders.length);
            for (VisitBuilder visitBuilder : visitBuilders) {
                Visit visit = visitBuilder.build();
                route.add(visit);
                visits.add(visit);
            }
            vehicle.setVisits(route);
            vehicles.add(vehicle);
            return this;
        }

        /**
         * Adds visits that are on no vehicle's route.
         */
        public RoutePlanBuilder unassigned(VisitBuilder... visitBuilders) {
            for (VisitBuilder visitBuilder : visitBuilders) {
                visits.add(visitBuilder.build());
            }
            return this;
        }

        public VehicleRoutePlan build() {
            var plan = new VehicleRoutePlan(vehicles, visits);
            // updateShadowVariables derives the solution's entity classes from the objects it is
            // given, so without a single Vehicle it cannot resolve the "visits" list variable the
            // shadows hang off. There is nothing to derive in that case anyway: every visit is
            // unassigned, so all of its shadows are legitimately null already.
            initDistanceMap(plan);
            if (!vehicles.isEmpty()) {
                SolutionManager.updateShadowVariables(VehicleRoutePlan.class, plan);
            }
            return plan;
        }
    }

    public static VehicleRoutePlan initDistanceMap(VehicleRoutePlan plan) {
        TestDistanceCalculator.initDistanceMaps(plan.getLocations(),
                provider::calculateDistance,
                provider::calculateTravelTime);
        return plan;
    }
}
