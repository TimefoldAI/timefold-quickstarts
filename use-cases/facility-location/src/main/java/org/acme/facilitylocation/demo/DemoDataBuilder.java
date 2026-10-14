package org.acme.facilitylocation.demo;

import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.IntStream;

import org.acme.facilitylocation.dto.input.ConsumerInputDTO;
import org.acme.facilitylocation.dto.input.FacilityInputDTO;
import org.acme.facilitylocation.dto.input.FacilityPlanInput;
import org.acme.facilitylocation.dto.input.LocationDTO;

/**
 * Builds a demo dataset of candidate facilities and consumers scattered over a bounding box around north-west
 * London. The randomness is seeded, so the dataset is identical on every call, and the total capacity comfortably
 * exceeds the total demand, so the dataset is always feasible.
 */
public final class DemoDataBuilder {

    private static final int FACILITY_COUNT = 30;
    private static final int CONSUMER_COUNT = 60;
    private static final long TOTAL_CAPACITY = 4_500;
    private static final long TOTAL_DEMAND = 900;
    private static final long AVERAGE_SETUP_COST = 50_000;
    private static final long SETUP_COST_STANDARD_DEVIATION = 10_000;

    private static final double SOUTH_WEST_LATITUDE = 51.44;
    private static final double SOUTH_WEST_LONGITUDE = -0.16;
    private static final double NORTH_EAST_LATITUDE = 51.56;
    private static final double NORTH_EAST_LONGITUDE = -0.01;

    private static final long RANDOM_SEED = 0;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public FacilityPlanInput build() {
        Random random = new Random(RANDOM_SEED);
        PrimitiveIterator.OfDouble latitudes = random.doubles(SOUTH_WEST_LATITUDE, NORTH_EAST_LATITUDE).iterator();
        PrimitiveIterator.OfDouble longitudes = random.doubles(SOUTH_WEST_LONGITUDE, NORTH_EAST_LONGITUDE).iterator();

        List<FacilityInputDTO> facilities = IntStream.rangeClosed(1, FACILITY_COUNT)
                .mapToObj(i -> new FacilityInputDTO(
                        "facility-" + i,
                        new LocationDTO(latitudes.nextDouble(), longitudes.nextDouble()),
                        AVERAGE_SETUP_COST + (long) (SETUP_COST_STANDARD_DEVIATION * random.nextGaussian()),
                        TOTAL_CAPACITY / FACILITY_COUNT))
                .toList();
        List<ConsumerInputDTO> consumers = IntStream.rangeClosed(1, CONSUMER_COUNT)
                .mapToObj(i -> new ConsumerInputDTO(
                        "consumer-" + i,
                        new LocationDTO(latitudes.nextDouble(), longitudes.nextDouble()),
                        TOTAL_DEMAND / CONSUMER_COUNT,
                        null))
                .toList();

        return new FacilityPlanInput(facilities, consumers);
    }
}
