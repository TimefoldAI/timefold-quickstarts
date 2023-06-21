 package org.acme.vehiclerouting.rest;

 import java.time.Duration;
 import java.time.LocalTime;
 import java.util.List;
 import java.util.PrimitiveIterator;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.function.Supplier;
 import java.util.stream.Collectors;
 import java.util.stream.Stream;

 import jakarta.ws.rs.GET;
 import jakarta.ws.rs.Path;
 import jakarta.ws.rs.PathParam;
 import jakarta.ws.rs.core.MediaType;

 import org.acme.vehiclerouting.domain.Customer;
 import org.acme.vehiclerouting.domain.Depot;
 import org.acme.vehiclerouting.domain.Location;
 import org.acme.vehiclerouting.domain.Vehicle;
 import org.acme.vehiclerouting.domain.VehicleRoutePlan;
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

    public enum DemoData {
        FIRENZE(77, 6, 2, Duration.ofMinutes(20L), 4,
                new Location(43.751466, 11.177210), new Location(43.809291, 11.290195));

        private int customerCount;
        private int vehicleCount;
        private int depotCount;
        private Duration serviceDuration;

        private int windowSizeToDurationRatio;
        private Location southWestCorner;
        private Location northEastCorner;

        DemoData(int customerCount, int vehicleCount, int depotCount, Duration serviceDuration,
                 int windowSizeToDurationRatio, Location southWestCorner, Location northEastCorner) {
            if (customerCount < 1) {
                throw new IllegalStateException(
                        "Number of customerCount (" + customerCount + ") must be greater than zero.");
            }
            if (vehicleCount < 1) {
                throw new IllegalStateException(
                        "Number of vehicleCount (" + vehicleCount + ") must be greater than zero.");
            }
            if (depotCount < 1) {
                throw new IllegalStateException(
                        "Number of depotCount (" + depotCount + ") must be greater than zero.");
            }
            if (windowSizeToDurationRatio < 1) {
                throw new IllegalStateException(
                        "Time windows size to duration ratio (" + depotCount + ") must be greater than zero.");
            }
            if (northEastCorner.getLatitude() <= southWestCorner.getLatitude()) {
                throw new IllegalStateException("northEastCorner.getLatitude (" + northEastCorner.getLatitude()
                        + ") must be greater than southWestCorner.getLatitude(" + southWestCorner.getLatitude() + ").");
            }
            if (northEastCorner.getLongitude() <= southWestCorner.getLongitude()) {
                throw new IllegalStateException("northEastCorner.getLongitude (" + northEastCorner.getLongitude()
                        + ") must be greater than southWestCorner.getLongitude(" + southWestCorner.getLongitude() + ").");
            }

            this.customerCount = customerCount;
            this.vehicleCount = vehicleCount;
            this.depotCount = depotCount;
            this.serviceDuration = serviceDuration;
            this.windowSizeToDurationRatio = windowSizeToDurationRatio;
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
                            schema = @Schema(implementation = VehicleRoutePlan.class)))})
    @Operation(summary = "Find an unsolved demo route plan by ID.")
    @GET
    @Path("/{demoDataId}")
    public VehicleRoutePlan generate(@Parameter(description = "Unique identifier of the demo data.",
            required = true) @PathParam("demoDataId") DemoData demoData) {
        return build(demoData);
    }

    public VehicleRoutePlan build(DemoData demoData) {

        String name = "demo";

        Random random = new Random(0);
        PrimitiveIterator.OfDouble latitudes = random
                .doubles(demoData.southWestCorner.getLatitude(), demoData.northEastCorner.getLatitude()).iterator();
        PrimitiveIterator.OfDouble longitudes = random
                .doubles(demoData.southWestCorner.getLongitude(), demoData.northEastCorner.getLongitude()).iterator();

        PrimitiveIterator.OfInt depotRandom = random.ints(0, demoData.depotCount).iterator();

        AtomicLong depotSequence = new AtomicLong();
        Supplier<Depot> depotSupplier = () -> new Depot(
                depotSequence.incrementAndGet(),
                new Location(latitudes.nextDouble(), longitudes.nextDouble()));

        List<Depot> depots = Stream.generate(depotSupplier)
                .limit(demoData.depotCount)
                .collect(Collectors.toList());

        AtomicLong vehicleSequence = new AtomicLong();
        Supplier<Vehicle> vehicleSupplier = () -> new Vehicle(
                vehicleSequence.incrementAndGet(),
                depots.get(depotRandom.nextInt()),
                LocalTime.of(7, 30));

        List<Vehicle> vehicles = Stream.generate(vehicleSupplier)
                .limit(demoData.vehicleCount)
                .collect(Collectors.toList());

        AtomicLong customerSequence = new AtomicLong();
        Duration windowSize = demoData.serviceDuration.multipliedBy(demoData.windowSizeToDurationRatio);
        PrimitiveIterator.OfInt readyHourRandom = random.ints(8,  24 - (int) windowSize.toHours()).iterator();
        Supplier<Customer> customerSupplier = () -> {
            LocalTime readyTime = LocalTime.of(readyHourRandom.nextInt(), 0);
            return new Customer(
                    customerSequence.incrementAndGet(),
                    new Location(latitudes.nextDouble(), longitudes.nextDouble()),
                    readyTime,
                    readyTime.plus(windowSize),
                    demoData.serviceDuration);
        };

        List<Customer> customers = Stream.generate(customerSupplier)
                .limit(demoData.customerCount)
                .collect(Collectors.toList());

        return new VehicleRoutePlan(name, depots, vehicles, customers, demoData.southWestCorner,
                demoData.northEastCorner);
    }
}
