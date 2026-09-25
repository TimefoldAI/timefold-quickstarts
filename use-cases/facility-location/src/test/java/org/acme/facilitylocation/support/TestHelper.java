package org.acme.facilitylocation.support;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.service.maps.api.model.Location;
import ai.timefold.solver.service.maps.haversine.impl.HaversineTravelTimeAndDistanceMatrixProvider;
import ai.timefold.solver.service.maps.service.test.api.TestDistanceCalculator;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityPlan;
import org.acme.facilitylocation.dto.input.ConsumerInputDTO;
import org.acme.facilitylocation.dto.input.FacilityInputDTO;
import org.acme.facilitylocation.dto.input.FacilityPlanInput;
import org.acme.facilitylocation.dto.input.LocationDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    public static final double BASE_LATITUDE = 51.0;
    public static final double BASE_LONGITUDE = 0.0;

    public static final HaversineTravelTimeAndDistanceMatrixProvider provider =
            new HaversineTravelTimeAndDistanceMatrixProvider(new ObjectMapper());

    private TestHelper() {
    }

    public static FacilityPlanInput input(List<FacilityInputDTO> facilities, List<ConsumerInputDTO> consumers) {
        return new FacilityPlanInput(facilities, consumers);
    }

    /**
     * A small but feasible problem: three facilities that can each serve 100 units of demand, and four consumers
     * demanding 20 units each.
     */
    public static FacilityPlanInput createProblem() {
        List<FacilityInputDTO> facilities = List.of(
                aFacilityDTO("f1").longitude(BASE_LONGITUDE).build(),
                aFacilityDTO("f2").longitude(BASE_LONGITUDE + 1).build(),
                aFacilityDTO("f3").longitude(BASE_LONGITUDE + 2).build());
        List<ConsumerInputDTO> consumers = List.of(
                aConsumerDTO("c1").longitude(BASE_LONGITUDE).build(),
                aConsumerDTO("c2").longitude(BASE_LONGITUDE + 1).build(),
                aConsumerDTO("c3").longitude(BASE_LONGITUDE + 2).build(),
                aConsumerDTO("c4").longitude(BASE_LONGITUDE + 2).build());
        return input(facilities, consumers);
    }

    /**
     * The distance the solver model itself would compute between two coordinates, so that a distance expectation in
     * a test is derived rather than hard-coded to a magic number.
     */
    public static long distanceMeters(double fromLatitude, double fromLongitude, double toLatitude,
            double toLongitude) {
        return provider.calculateDistance(new Location(fromLatitude, fromLongitude), new Location(toLatitude, toLongitude));
    }

    public static FacilityPlan initDistanceMap(FacilityPlan plan) {
        initDistanceMap(plan.getLocations());
        return plan;
    }

    /**
     * {@code ConstraintVerifier} bypasses the model-conversion pipeline the map service hooks into, so the distance
     * matrix covering these consumers and their facilities has to be built here instead.
     */
    public static void initDistanceMap(Consumer... consumers) {
        List<Location> locations = new ArrayList<>();
        for (Consumer consumer : consumers) {
            locations.add(consumer.getLocation());
            if (consumer.getFacility() != null) {
                locations.add(consumer.getFacility().getLocation());
            }
        }
        initDistanceMap(locations);
    }

    private static void initDistanceMap(List<Location> locations) {
        TestDistanceCalculator.initDistanceMaps(locations,
                provider::calculateDistance,
                provider::calculateTravelTime);
    }

    public static FacilityBuilder aFacility(String id) {
        return new FacilityBuilder(id);
    }

    public static ConsumerBuilder aConsumer(String id, FacilityBuilder facility) {
        return new ConsumerBuilder(id, facility);
    }

    public static FacilityDTOBuilder aFacilityDTO(String id) {
        return new FacilityDTOBuilder(id);
    }

    public static ConsumerDTOBuilder aConsumerDTO(String id) {
        return new ConsumerDTOBuilder(id);
    }

    public static final class FacilityBuilder {

        private final String id;
        private double latitude = BASE_LATITUDE;
        private double longitude = BASE_LONGITUDE;
        private long setupCost = 1_000;
        private long capacity = 100;

        private FacilityBuilder(String id) {
            this.id = id;
        }

        public FacilityBuilder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public FacilityBuilder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public FacilityBuilder setupCost(long setupCost) {
            this.setupCost = setupCost;
            return this;
        }

        public FacilityBuilder capacity(long capacity) {
            this.capacity = capacity;
            return this;
        }

        public Facility build() {
            return new Facility(id, new Location(latitude, longitude), setupCost, capacity);
        }
    }

    public static final class ConsumerBuilder {

        private final String id;
        private final FacilityBuilder facility;
        private double latitude = BASE_LATITUDE;
        private double longitude = BASE_LONGITUDE;
        private long demand = 20;

        private ConsumerBuilder(String id, FacilityBuilder facility) {
            this.id = id;
            this.facility = facility;
        }

        public ConsumerBuilder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public ConsumerBuilder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public ConsumerBuilder demand(long demand) {
            this.demand = demand;
            return this;
        }

        // Each consumer gets its own Facility instance, but Facility's equals()/hashCode() are id-based, so
        // consumers built from the same FacilityBuilder still land in the same groupBy(Consumer::getFacility)
        // group, exactly as they would during solving.
        public Consumer build() {
            Consumer consumer = new Consumer(id, new Location(latitude, longitude), demand);
            consumer.setFacility(facility == null ? null : facility.build());
            return consumer;
        }
    }

    public static final class FacilityDTOBuilder {

        private final String id;
        private double latitude = BASE_LATITUDE;
        private double longitude = BASE_LONGITUDE;
        private long setupCost = 1_000;
        private long capacity = 100;

        private FacilityDTOBuilder(String id) {
            this.id = id;
        }

        public FacilityDTOBuilder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public FacilityDTOBuilder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public FacilityDTOBuilder setupCost(long setupCost) {
            this.setupCost = setupCost;
            return this;
        }

        public FacilityDTOBuilder capacity(long capacity) {
            this.capacity = capacity;
            return this;
        }

        public FacilityInputDTO build() {
            return new FacilityInputDTO(id, new LocationDTO(latitude, longitude), setupCost, capacity);
        }
    }

    public static final class ConsumerDTOBuilder {

        private final String id;
        private double latitude = BASE_LATITUDE;
        private double longitude = BASE_LONGITUDE;
        private long demand = 20;
        private String facilityId;

        private ConsumerDTOBuilder(String id) {
            this.id = id;
        }

        public ConsumerDTOBuilder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public ConsumerDTOBuilder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public ConsumerDTOBuilder demand(long demand) {
            this.demand = demand;
            return this;
        }

        public ConsumerDTOBuilder facilityId(String facilityId) {
            this.facilityId = facilityId;
            return this;
        }

        public ConsumerInputDTO build() {
            return new ConsumerInputDTO(id, new LocationDTO(latitude, longitude), demand, facilityId);
        }
    }
}
