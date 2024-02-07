package org.acme.vehiclerouting.rest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo data", description = "Timefold-provided demo vehicle routing data.")
@Path("demo-data")
public class VehicleRouteDemoResource {

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Chad", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt" };
    private static final int[] SERVICE_DURATION_MINUTES = { 10, 20, 30, 40 };
    private static final LocalTime MORNING_WINDOW_START = LocalTime.of(8, 0);
    private static final LocalTime MORNING_WINDOW_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_WINDOW_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_WINDOW_END = LocalTime.of(18, 0);

    public enum DemoData {
        PHILADELPHIA(0, 55, 6, LocalTime.of(7, 30),
                1, 2, 15, 30,
                new Location(39.7656099067391, -76.83782328143754),
                new Location(40.77636644354855, -74.9300739430771)),
        HARTFORT(1, 50, 6, LocalTime.of(7, 30),
                1, 3, 20, 30,
                new Location(41.48366520850297, -73.15901689943055),
                new Location(41.99512052869307, -72.25114548877427)),
        FIRENZE(2, 77, 6, LocalTime.of(7, 30),
                1, 2, 20, 40,
                new Location(43.751466, 11.177210), new Location(43.809291, 11.290195));

        private long seed;
        private int visitCount;
        private int vehicleCount;
        private LocalTime vehicleStartTime;
        private int minDemand;
        private int maxDemand;
        private int minVehicleCapacity;
        private int maxVehicleCapacity;
        private Location southWestCorner;
        private Location northEastCorner;

        DemoData(long seed, int visitCount, int vehicleCount, LocalTime vehicleStartTime,
                 int minDemand, int maxDemand, int minVehicleCapacity, int maxVehicleCapacity,
                 Location southWestCorner, Location northEastCorner) {
            if (minDemand < 1) {
                throw new IllegalStateException("minDemand (%s) must be greater than zero.".formatted(minDemand));
            }
            if (maxDemand < 1) {
                throw new IllegalStateException("maxDemand (%s) must be greater than zero.".formatted(maxDemand));
            }
            if (minDemand >= maxDemand) {
                throw new IllegalStateException("maxDemand (%s) must be greater than minDemand (%s)."
                        .formatted(maxDemand, minDemand));
            }
            if (minVehicleCapacity < 1) {
                throw new IllegalStateException(
                        "Number of minVehicleCapacity (%s) must be greater than zero.".formatted(minVehicleCapacity));
            }
            if (maxVehicleCapacity < 1) {
                throw new IllegalStateException(
                        "Number of maxVehicleCapacity (%s) must be greater than zero.".formatted(maxVehicleCapacity));
            }
            if (minVehicleCapacity >= maxVehicleCapacity) {
                throw new IllegalStateException("maxVehicleCapacity (%s) must be greater than minVehicleCapacity (%s)."
                        .formatted(maxVehicleCapacity, minVehicleCapacity));
            }
            if (visitCount < 1) {
                throw new IllegalStateException(
                        "Number of visitCount (%s) must be greater than zero.".formatted(visitCount));
            }
            if (vehicleCount < 1) {
                throw new IllegalStateException(
                        "Number of vehicleCount (%s) must be greater than zero.".formatted(vehicleCount));
            }
            if (northEastCorner.getLatitude() <= southWestCorner.getLatitude()) {
                throw new IllegalStateException(
                        "northEastCorner.getLatitude (%s) must be greater than southWestCorner.getLatitude(%s)."
                                .formatted(northEastCorner.getLatitude(), southWestCorner.getLatitude()));
            }
            if (northEastCorner.getLongitude() <= southWestCorner.getLongitude()) {
                throw new IllegalStateException(
                        "northEastCorner.getLongitude (%s) must be greater than southWestCorner.getLongitude(%s)."
                                .formatted(northEastCorner.getLongitude(), southWestCorner.getLongitude()));
            }

            this.seed = seed;
            this.visitCount = visitCount;
            this.vehicleCount = vehicleCount;
            this.vehicleStartTime = vehicleStartTime;
            this.minDemand = minDemand;
            this.maxDemand = maxDemand;
            this.minVehicleCapacity = minVehicleCapacity;
            this.maxVehicleCapacity = maxVehicleCapacity;
            this.southWestCorner = southWestCorner;
            this.northEastCorner = northEastCorner;
        }
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of demo data represented as IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DemoData.class, type = SchemaType.ARRAY))) })
    @Operation(summary = "List demo data.")
    @GET
    public DemoData[] list() {
        return DemoData.values();
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Unsolved demo route plan.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = VehicleRoutePlan.class))) })
    @Operation(summary = "Find an unsolved demo route plan by ID.")
    @GET
    @Path("/{demoDataId}")
    public VehicleRoutePlan generate(@Parameter(description = "Unique identifier of the demo data.",
            required = true) @PathParam("demoDataId") DemoData demoData) {
        return build(demoData);
    }

    public VehicleRoutePlan build(DemoData demoData) {
        String name = "demo";

        Random random = new Random(demoData.seed);
        PrimitiveIterator.OfDouble latitudes = random
                .doubles(demoData.southWestCorner.getLatitude(), demoData.northEastCorner.getLatitude()).iterator();
        PrimitiveIterator.OfDouble longitudes = random
                .doubles(demoData.southWestCorner.getLongitude(), demoData.northEastCorner.getLongitude()).iterator();

        PrimitiveIterator.OfInt demand = random.ints(demoData.minDemand, demoData.maxDemand + 1)
                .iterator();
        PrimitiveIterator.OfInt vehicleCapacity = random.ints(demoData.minVehicleCapacity, demoData.maxVehicleCapacity + 1)
                .iterator();

        AtomicLong vehicleSequence = new AtomicLong();
        Supplier<Vehicle> vehicleSupplier = () -> new Vehicle(
                String.valueOf(vehicleSequence.incrementAndGet()),
                vehicleCapacity.nextInt(),
                new Location(latitudes.nextDouble(), longitudes.nextDouble()),
                tomorrowAt(demoData.vehicleStartTime));

        List<Vehicle> vehicles = Stream.generate(vehicleSupplier)
                .limit(demoData.vehicleCount)
                .collect(Collectors.toList());

        Supplier<String> nameSupplier = () -> {
            Function<String[], String> randomStringSelector = strings -> strings[random.nextInt(strings.length)];
            String firstName = randomStringSelector.apply(FIRST_NAMES);
            String lastName = randomStringSelector.apply(LAST_NAMES);
            return firstName + " " + lastName;
        };

        AtomicLong visitSequence = new AtomicLong();
        Supplier<Visit> visitSupplier = () -> {
            boolean morningTimeWindow = random.nextBoolean();

            LocalDateTime minStartTime =
                    morningTimeWindow ? tomorrowAt(MORNING_WINDOW_START) : tomorrowAt(AFTERNOON_WINDOW_START);
            LocalDateTime maxEndTime = morningTimeWindow ? tomorrowAt(MORNING_WINDOW_END) : tomorrowAt(AFTERNOON_WINDOW_END);
            int serviceDurationMinutes = SERVICE_DURATION_MINUTES[random.nextInt(SERVICE_DURATION_MINUTES.length)];
            return new Visit(
                    String.valueOf(visitSequence.incrementAndGet()),
                    nameSupplier.get(),
                    new Location(latitudes.nextDouble(), longitudes.nextDouble()),
                    demand.nextInt(),
                    minStartTime,
                    maxEndTime,
                    Duration.ofMinutes(serviceDurationMinutes));
        };

        List<Visit> visits = Stream.generate(visitSupplier)
                .limit(demoData.visitCount)
                .collect(Collectors.toList());

        return new VehicleRoutePlan(name, demoData.southWestCorner, demoData.northEastCorner,
                tomorrowAt(demoData.vehicleStartTime), tomorrowAt(LocalTime.MIDNIGHT).plusDays(1L),
                vehicles, visits);
    }

    private static LocalDateTime tomorrowAt(LocalTime time) {
        return LocalDateTime.of(LocalDate.now().plusDays(1L), time);
    }
}
