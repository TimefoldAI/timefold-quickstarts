package org.acme.vehiclerouting.demo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.acme.vehiclerouting.dto.input.LocationInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.acme.vehiclerouting.dto.input.VisitInputDTO;

/**
 * Builds the demo datasets: a fleet of vehicles and a set of visits scattered over the map area of
 * one of four cities, all on tomorrow's date.
 * <p>
 * Every coordinate, demand, capacity and time window comes from a {@link Random} with a fixed seed
 * per dataset, so a dataset is reproducible; only the date moves, because it is anchored to today.
 */
public final class DemoDataBuilder {

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy",
            "Jay" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith",
            "Watt" };
    private static final int[] SERVICE_DURATION_MINUTES = { 10, 20, 30, 40 };

    private static final LocalTime VEHICLE_START_TIME = LocalTime.of(7, 30);
    private static final LocalTime MORNING_WINDOW_START = LocalTime.of(8, 0);
    private static final LocalTime MORNING_WINDOW_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_WINDOW_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_WINDOW_END = LocalTime.of(18, 0);

    private static final int VEHICLE_COUNT = 6;

    // Package-private (not just the map area passed to build()) so DemoDataBuilderTest can assert
    // that generated locations fall within the same bounds without those bounds being part of the
    // public VehicleRoutePlanInput - they only ever existed to generate demo data, not for the UI.
    static final Dataset PHILADELPHIA = new Dataset(2L, 55, 1, 2, 15, 30,
            new LocationInputDTO(39.7656099067391, -76.83782328143754),
            new LocationInputDTO(40.77636644354855, -74.9300739430771));
    static final Dataset GHENT = new Dataset(1L, 65, 1, 2, 15, 30,
            new LocationInputDTO(50.990000, 3.620000),
            new LocationInputDTO(51.130000, 3.840000));
    static final Dataset HARTFORT = new Dataset(1L, 50, 1, 3, 20, 30,
            new LocationInputDTO(41.48366520850297, -73.15901689943055),
            new LocationInputDTO(41.99512052869307, -72.25114548877427));
    static final Dataset FIRENZE = new Dataset(2L, 77, 1, 2, 20, 40,
            new LocationInputDTO(43.751466, 11.177210),
            new LocationInputDTO(43.809291, 11.290195));

    private DemoDataBuilder() {
    }

    public static VehicleRoutePlanInput philadelphia() {
        return build(PHILADELPHIA);
    }

    public static VehicleRoutePlanInput ghent() {
        return build(GHENT);
    }

    public static VehicleRoutePlanInput hartfort() {
        return build(HARTFORT);
    }

    public static VehicleRoutePlanInput firenze() {
        return build(FIRENZE);
    }

    private static VehicleRoutePlanInput build(Dataset dataset) {
        Random random = new Random(dataset.seed());
        OffsetDateTime departureTime = tomorrowAt(VEHICLE_START_TIME);

        List<VehicleInputDTO> vehicles = IntStream.rangeClosed(1, VEHICLE_COUNT)
                .mapToObj(i -> new VehicleInputDTO(Integer.toString(i),
                        randomBetween(random, dataset.minVehicleCapacity(), dataset.maxVehicleCapacity()),
                        randomLocation(random, dataset), departureTime, List.of()))
                .toList();

        List<VisitInputDTO> visits = IntStream.rangeClosed(1, dataset.visitCount())
                .mapToObj(i -> {
                    boolean morningTimeWindow = random.nextBoolean();
                    OffsetDateTime minStartTime = tomorrowAt(
                            morningTimeWindow ? MORNING_WINDOW_START : AFTERNOON_WINDOW_START);
                    OffsetDateTime maxEndTime = tomorrowAt(
                            morningTimeWindow ? MORNING_WINDOW_END : AFTERNOON_WINDOW_END);
                    return new VisitInputDTO(Integer.toString(i), randomName(random), randomLocation(random, dataset),
                            randomBetween(random, dataset.minDemand(), dataset.maxDemand()), minStartTime, maxEndTime,
                            SERVICE_DURATION_MINUTES[random.nextInt(SERVICE_DURATION_MINUTES.length)]);
                })
                .toList();

        return new VehicleRoutePlanInput(departureTime, tomorrowAt(LocalTime.MIDNIGHT).plusDays(1L), vehicles, visits);
    }

    private static String randomName(Random random) {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }

    private static LocationInputDTO randomLocation(Random random, Dataset dataset) {
        return new LocationInputDTO(
                randomBetween(random, dataset.southWestCorner().latitude(), dataset.northEastCorner().latitude()),
                randomBetween(random, dataset.southWestCorner().longitude(), dataset.northEastCorner().longitude()));
    }

    private static int randomBetween(Random random, int minInclusive, int maxInclusive) {
        return minInclusive + random.nextInt(maxInclusive - minInclusive + 1);
    }

    private static double randomBetween(Random random, double minInclusive, double maxExclusive) {
        return minInclusive + random.nextDouble() * (maxExclusive - minInclusive);
    }

    /**
     * Everything is planned on tomorrow's date, in UTC, so a dataset never starts in the past and
     * never depends on the server's time zone.
     */
    private static OffsetDateTime tomorrowAt(LocalTime time) {
        return OffsetDateTime.of(LocalDate.now(ZoneOffset.UTC).plusDays(1L), time, ZoneOffset.UTC);
    }

    /**
     * The knobs that make one demo dataset differ from the next.
     */
    record Dataset(
            long seed,
            int visitCount,
            int minDemand,
            int maxDemand,
            int minVehicleCapacity,
            int maxVehicleCapacity,
            LocationInputDTO southWestCorner,
            LocationInputDTO northEastCorner) {

        public Dataset {
            if (minDemand < 1 || maxDemand < minDemand) {
                throw new IllegalArgumentException(
                        "The demand range (%d-%d) must be positive and non-decreasing.".formatted(minDemand, maxDemand));
            }
            if (minVehicleCapacity < 1 || maxVehicleCapacity < minVehicleCapacity) {
                throw new IllegalArgumentException("The vehicle capacity range (%d-%d) must be positive and non-decreasing."
                        .formatted(minVehicleCapacity, maxVehicleCapacity));
            }
            if (visitCount < 1) {
                throw new IllegalArgumentException("The visit count (%d) must be positive.".formatted(visitCount));
            }
            if (northEastCorner.latitude() <= southWestCorner.latitude()
                    || northEastCorner.longitude() <= southWestCorner.longitude()) {
                throw new IllegalArgumentException(
                        "The north-east corner (%s) must lie north-east of the south-west corner (%s)."
                                .formatted(northEastCorner, southWestCorner));
            }
        }
    }
}
