package org.acme.vehiclerouting.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import org.acme.vehiclerouting.domain.Customer;
import org.acme.vehiclerouting.domain.Depot;
import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.acme.vehiclerouting.domain.geo.DistanceCalculator;
import org.acme.vehiclerouting.domain.geo.EuclideanDistanceCalculator;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Tag(name = "Demo data sets", description = "Timefold-provided demo vehicle routing data sets.")
@Path("demo/datasets")
public class VehicleRoutingDemoResource {

    public enum DemoDataSet {
        FIRENZE
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of demo data sets represented as IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DemoDataSet.class, type = SchemaType.ARRAY))) })
    @Operation(summary = "List demo data sets.")
    @GET
    public DemoDataSet[] list() {
        return DemoDataSet.values();
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Unsolved demo route plan.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = VehicleRoutingSolution.class)))})
    @Operation(summary = "Find an unsolved demo route plan by ID.")
    @GET
    @Path("/{dataSetId}")
    public VehicleRoutingSolution generate(@Parameter(description = "Unique identifier of the demo data set.",
            required = true) @PathParam("dataSetId") DemoDataSet demoDataSet) {
        return VehicleRoutingDemoResource.DemoDataBuilder.builder()
                .setMinDemand(1)
                .setMaxDemand(2)
                .setVehicleCapacity(25)
                .setCustomerCount(77)
                .setVehicleCount(6)
                .setDepotCount(2)
                .setSouthWestCorner(new Location(0L, 43.751466, 11.177210))
                .setNorthEastCorner(new Location(0L, 43.809291, 11.290195))
                .build();
    }

    static class DemoDataBuilder {

        private static final AtomicLong sequence = new AtomicLong();

        private final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator();

        private Location southWestCorner;
        private Location northEastCorner;
        private int customerCount;
        private int vehicleCount;
        private int depotCount;
        private int minDemand;
        private int maxDemand;
        private int vehicleCapacity;

        private DemoDataBuilder() {
        }

        public static DemoDataBuilder builder() {
            return new DemoDataBuilder();
        }

        public DemoDataBuilder setSouthWestCorner(Location southWestCorner) {
            this.southWestCorner = southWestCorner;
            return this;
        }

        public DemoDataBuilder setNorthEastCorner(Location northEastCorner) {
            this.northEastCorner = northEastCorner;
            return this;
        }

        public DemoDataBuilder setMinDemand(int minDemand) {
            this.minDemand = minDemand;
            return this;
        }

        public DemoDataBuilder setMaxDemand(int maxDemand) {
            this.maxDemand = maxDemand;
            return this;
        }

        public DemoDataBuilder setCustomerCount(int customerCount) {
            this.customerCount = customerCount;
            return this;
        }

        public DemoDataBuilder setVehicleCount(int vehicleCount) {
            this.vehicleCount = vehicleCount;
            return this;
        }

        public DemoDataBuilder setDepotCount(int depotCount) {
            this.depotCount = depotCount;
            return this;
        }

        public DemoDataBuilder setVehicleCapacity(int vehicleCapacity) {
            this.vehicleCapacity = vehicleCapacity;
            return this;
        }

        public VehicleRoutingSolution build() {
            if (minDemand < 1) {
                throw new IllegalStateException("minDemand (" + minDemand + ") must be greater than zero.");
            }
            if (maxDemand < 1) {
                throw new IllegalStateException("maxDemand (" + maxDemand + ") must be greater than zero.");
            }
            if (minDemand >= maxDemand) {
                throw new IllegalStateException("maxDemand (" + maxDemand + ") must be greater than minDemand ("
                        + minDemand + ").");
            }
            if (vehicleCapacity < 1) {
                throw new IllegalStateException(
                        "Number of vehicleCapacity (" + vehicleCapacity + ") must be greater than zero.");
            }
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

            if (northEastCorner.getLatitude() <= southWestCorner.getLatitude()) {
                throw new IllegalStateException("northEastCorner.getLatitude (" + northEastCorner.getLatitude()
                        + ") must be greater than southWestCorner.getLatitude(" + southWestCorner.getLatitude() + ").");
            }

            if (northEastCorner.getLongitude() <= southWestCorner.getLongitude()) {
                throw new IllegalStateException("northEastCorner.getLongitude (" + northEastCorner.getLongitude()
                        + ") must be greater than southWestCorner.getLongitude(" + southWestCorner.getLongitude() + ").");
            }

            String name = "demo";

            Random random = new Random(0);
            PrimitiveIterator.OfDouble latitudes = random
                    .doubles(southWestCorner.getLatitude(), northEastCorner.getLatitude()).iterator();
            PrimitiveIterator.OfDouble longitudes = random
                    .doubles(southWestCorner.getLongitude(), northEastCorner.getLongitude()).iterator();

            PrimitiveIterator.OfInt demand = random.ints(minDemand, maxDemand + 1).iterator();

            PrimitiveIterator.OfInt depotRandom = random.ints(0, depotCount).iterator();

            Supplier<Depot> depotSupplier = () -> new Depot(
                    sequence.incrementAndGet(),
                    new Location(sequence.incrementAndGet(), latitudes.nextDouble(), longitudes.nextDouble()));

            List<Depot> depotList = Stream.generate(depotSupplier)
                    .limit(depotCount)
                    .collect(Collectors.toList());

            Supplier<Vehicle> vehicleSupplier = () -> new Vehicle(
                    sequence.incrementAndGet(),
                    vehicleCapacity,
                    depotList.get(depotRandom.nextInt()));

            List<Vehicle> vehicleList = Stream.generate(vehicleSupplier)
                    .limit(vehicleCount)
                    .collect(Collectors.toList());

            Supplier<Customer> customerSupplier = () -> new Customer(
                    sequence.incrementAndGet(),
                    new Location(sequence.incrementAndGet(), latitudes.nextDouble(), longitudes.nextDouble()),
                    demand.nextInt());

            List<Customer> customerList = Stream.generate(customerSupplier)
                    .limit(customerCount)
                    .collect(Collectors.toList());

            List<Location> locationList = Stream.concat(
                            customerList.stream().map(Customer::getLocation),
                            depotList.stream().map(Depot::getLocation))
                    .collect(Collectors.toList());

            distanceCalculator.initDistanceMaps(locationList);

            return new VehicleRoutingSolution(name, locationList,
                    depotList, vehicleList, customerList, southWestCorner, northEastCorner);
        }
    }
}
