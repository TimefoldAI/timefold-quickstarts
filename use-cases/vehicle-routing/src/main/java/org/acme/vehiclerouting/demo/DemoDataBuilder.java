package org.acme.vehiclerouting.demo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.acme.vehiclerouting.dto.LocationDTO;
import org.acme.vehiclerouting.dto.VehicleDTO;
import org.acme.vehiclerouting.dto.VehicleRoutingInput;
import org.acme.vehiclerouting.dto.VisitDTO;

/**
 * Builds a deterministic demo vehicle routing dataset (the city of Philadelphia) with a fixed random seed.
 */
@SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
public final class DemoDataBuilder {

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt" };
    private static final int[] SERVICE_DURATION_MINUTES = { 10, 20, 30, 40 };

    private static final long SEED = 2L;
    private static final int VISIT_COUNT = 55;
    private static final int VEHICLE_COUNT = 6;
    private static final int MIN_DEMAND = 1;
    private static final int MAX_DEMAND = 2;
    private static final int MIN_VEHICLE_CAPACITY = 15;
    private static final int MAX_VEHICLE_CAPACITY = 30;

    private static final LocalTime VEHICLE_START_TIME = LocalTime.of(7, 30);
    private static final LocalTime MORNING_WINDOW_START = LocalTime.of(8, 0);
    private static final LocalTime MORNING_WINDOW_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_WINDOW_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_WINDOW_END = LocalTime.of(18, 0);

    private static final LocationDTO SOUTH_WEST_CORNER = new LocationDTO(39.7656099067391, -76.83782328143754);
    private static final LocationDTO NORTH_EAST_CORNER = new LocationDTO(40.77636644354855, -74.9300739430771);

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public VehicleRoutingInput build() {
        LocalDate tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1L);
        OffsetDateTime startDateTime = OffsetDateTime.of(tomorrow, VEHICLE_START_TIME, ZoneOffset.UTC);
        OffsetDateTime endDateTime = OffsetDateTime.of(tomorrow.plusDays(1L), LocalTime.MIDNIGHT, ZoneOffset.UTC);

        Random random = new Random(SEED);
        PrimitiveIterator.OfDouble latitudes =
                random.doubles(SOUTH_WEST_CORNER.latitude(), NORTH_EAST_CORNER.latitude()).iterator();
        PrimitiveIterator.OfDouble longitudes =
                random.doubles(SOUTH_WEST_CORNER.longitude(), NORTH_EAST_CORNER.longitude()).iterator();
        PrimitiveIterator.OfInt demand = random.ints(MIN_DEMAND, MAX_DEMAND + 1).iterator();
        PrimitiveIterator.OfInt vehicleCapacity = random.ints(MIN_VEHICLE_CAPACITY, MAX_VEHICLE_CAPACITY + 1).iterator();

        OffsetDateTime unsetTime = null;
        String unassignedVehicle = null;

        AtomicLong vehicleSequence = new AtomicLong();
        Supplier<VehicleDTO> vehicleSupplier = () -> new VehicleDTO(
                String.valueOf(vehicleSequence.incrementAndGet()),
                vehicleCapacity.nextInt(),
                new LocationDTO(latitudes.nextDouble(), longitudes.nextDouble()),
                startDateTime,
                List.of(), 0, 0L, unsetTime);
        List<VehicleDTO> vehicles = Stream.generate(vehicleSupplier)
                .limit(VEHICLE_COUNT)
                .collect(Collectors.toList());

        Function<String[], String> randomName = strings -> strings[random.nextInt(strings.length)];
        AtomicLong visitSequence = new AtomicLong();
        Supplier<VisitDTO> visitSupplier = () -> {
            boolean morningTimeWindow = random.nextBoolean();
            OffsetDateTime minStartTime = OffsetDateTime.of(tomorrow,
                    morningTimeWindow ? MORNING_WINDOW_START : AFTERNOON_WINDOW_START, ZoneOffset.UTC);
            OffsetDateTime maxEndTime = OffsetDateTime.of(tomorrow,
                    morningTimeWindow ? MORNING_WINDOW_END : AFTERNOON_WINDOW_END, ZoneOffset.UTC);
            int serviceDurationMinutes = SERVICE_DURATION_MINUTES[random.nextInt(SERVICE_DURATION_MINUTES.length)];
            return new VisitDTO(
                    String.valueOf(visitSequence.incrementAndGet()),
                    randomName.apply(FIRST_NAMES) + " " + randomName.apply(LAST_NAMES),
                    new LocationDTO(latitudes.nextDouble(), longitudes.nextDouble()),
                    demand.nextInt(),
                    minStartTime,
                    maxEndTime,
                    serviceDurationMinutes * 60L,
                    unassignedVehicle, unsetTime, unsetTime, unsetTime, 0L);
        };
        List<VisitDTO> visits = Stream.generate(visitSupplier)
                .limit(VISIT_COUNT)
                .collect(Collectors.toList());

        return new VehicleRoutingInput("demo", SOUTH_WEST_CORNER, NORTH_EAST_CORNER, startDateTime, endDateTime,
                vehicles, visits);
    }
}
