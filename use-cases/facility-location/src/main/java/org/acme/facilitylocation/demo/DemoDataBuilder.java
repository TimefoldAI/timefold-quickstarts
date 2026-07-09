package org.acme.facilitylocation.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.acme.facilitylocation.dto.ConsumerDTO;
import org.acme.facilitylocation.dto.FacilityDTO;
import org.acme.facilitylocation.dto.FacilityLocationInput;
import org.acme.facilitylocation.dto.LocationDTO;

public final class DemoDataBuilder {

    private static final long MINIMUM_VALUE = 1L;
    private static final String MUST_BE_POSITIVE = ") must be greater than zero.";
    private static final AtomicLong sequence = new AtomicLong();

    private long capacity;
    private long demand;
    private int facilityCount;
    private int consumerCount;
    private long averageSetupCost;
    private long setupCostStandardDeviation;
    private LocationDTO southWestCorner;
    private LocationDTO northEastCorner;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setCapacity(long capacity) {
        this.capacity = capacity;
        return this;
    }

    public DemoDataBuilder setDemand(long demand) {
        this.demand = demand;
        return this;
    }

    public DemoDataBuilder setFacilityCount(int facilityCount) {
        this.facilityCount = facilityCount;
        return this;
    }

    public DemoDataBuilder setConsumerCount(int consumerCount) {
        this.consumerCount = consumerCount;
        return this;
    }

    public DemoDataBuilder setAverageSetupCost(long averageSetupCost) {
        this.averageSetupCost = averageSetupCost;
        return this;
    }

    public DemoDataBuilder setSetupCostStandardDeviation(long setupCostStandardDeviation) {
        this.setupCostStandardDeviation = setupCostStandardDeviation;
        return this;
    }

    public DemoDataBuilder setSouthWestCorner(LocationDTO southWestCorner) {
        this.southWestCorner = southWestCorner;
        return this;
    }

    public DemoDataBuilder setNorthEastCorner(LocationDTO northEastCorner) {
        this.northEastCorner = northEastCorner;
        return this;
    }

    public FacilityLocationInput build() {
        List<String> errors = new ArrayList<>();
        if (demand < MINIMUM_VALUE) {
            errors.add("Demand (" + demand + MUST_BE_POSITIVE);
        }
        if (capacity < MINIMUM_VALUE) {
            errors.add("Capacity (" + capacity + MUST_BE_POSITIVE);
        }
        if (facilityCount < MINIMUM_VALUE) {
            errors.add("Number of facilities (" + facilityCount + MUST_BE_POSITIVE);
        }
        if (consumerCount < MINIMUM_VALUE) {
            errors.add("Number of consumers (" + consumerCount + MUST_BE_POSITIVE);
        }
        if (demand > capacity) {
            errors.add("Overconstrained problem not supported. The total capacity (" + capacity
                    + ") must be greater than or equal to the total demand (" + demand + ").");
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join(" ", errors));
        }

        Random random = new Random(0);
        PrimitiveIterator.OfDouble latitudes = random
                .doubles(southWestCorner.latitude(), northEastCorner.latitude()).iterator();
        PrimitiveIterator.OfDouble longitudes = random
                .doubles(southWestCorner.longitude(), northEastCorner.longitude()).iterator();
        Supplier<LocationDTO> locationSupplier = () -> new LocationDTO(latitudes.nextDouble(), longitudes.nextDouble());
        List<FacilityDTO> facilities = Stream.generate(locationSupplier).map(location -> {
            long id = sequence.incrementAndGet();
            return new FacilityDTO(Long.toString(id), "Facility " + id, location,
                    averageSetupCost + (long) (setupCostStandardDeviation * random.nextGaussian()),
                    capacity / facilityCount, 0L, false);
        }).limit(facilityCount).collect(Collectors.toList());
        List<ConsumerDTO> consumers = Stream
                .generate(locationSupplier).map(location -> new ConsumerDTO(Long.toString(sequence.incrementAndGet()),
                        location, demand / consumerCount, "", false))
                .limit(consumerCount).collect(Collectors.toList());

        return new FacilityLocationInput(facilities, consumers, List.of(southWestCorner, northEastCorner));
    }
}
