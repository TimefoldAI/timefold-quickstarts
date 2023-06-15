package org.acme.vehiclerouting.rest;

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

@Tag(name = "Demo data sets", description = "Timefold-provided demo vehicle routing data sets.")
@Path("demo/datasets")
public class VehicleRouteDemoResource {

    public enum DemoDataSet {
        FIRENZE(77, 6, 2, 1, 2, 25,
                new Location(43.751466, 11.177210), new Location(43.809291, 11.290195));

        private int customerCount;
        private int vehicleCount;
        private int depotCount;
        private int minDemand;
        private int maxDemand;
        private int vehicleCapacity;
        private Location southWestCorner;
        private Location northEastCorner;

        DemoDataSet(int customerCount, int vehicleCount, int depotCount, int minDemand, int maxDemand,
                    int vehicleCapacity, Location southWestCorner, Location northEastCorner) {
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

            this.customerCount = customerCount;
            this.vehicleCount = vehicleCount;
            this.depotCount = depotCount;
            this.minDemand = minDemand;
            this.maxDemand = maxDemand;
            this.vehicleCapacity = vehicleCapacity;
            this.southWestCorner = southWestCorner;
            this.northEastCorner = northEastCorner;
        }
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
                            schema = @Schema(implementation = VehicleRoutePlan.class)))})
    @Operation(summary = "Find an unsolved demo route plan by ID.")
    @GET
    @Path("/{dataSetId}")
    public VehicleRoutePlan generate(@Parameter(description = "Unique identifier of the demo data set.",
            required = true) @PathParam("dataSetId") DemoDataSet demoDataSet) {
        return build(demoDataSet);
    }

    public VehicleRoutePlan build(DemoDataSet demoDataSet) {

        String name = "demo";

        Random random = new Random(0);
        PrimitiveIterator.OfDouble latitudes = random
                .doubles(demoDataSet.southWestCorner.getLatitude(), demoDataSet.northEastCorner.getLatitude()).iterator();
        PrimitiveIterator.OfDouble longitudes = random
                .doubles(demoDataSet.southWestCorner.getLongitude(), demoDataSet.northEastCorner.getLongitude()).iterator();

        PrimitiveIterator.OfInt demand = random.ints(demoDataSet.minDemand, demoDataSet.maxDemand + 1)
                .iterator();

        PrimitiveIterator.OfInt depotRandom = random.ints(0, demoDataSet.depotCount).iterator();

        AtomicLong depotSequence = new AtomicLong();
        Supplier<Depot> depotSupplier = () -> new Depot(
                depotSequence.incrementAndGet(),
                new Location(latitudes.nextDouble(), longitudes.nextDouble()));

        List<Depot> depotList = Stream.generate(depotSupplier)
                .limit(demoDataSet.depotCount)
                .collect(Collectors.toList());

        AtomicLong vehicleSequence = new AtomicLong();
        Supplier<Vehicle> vehicleSupplier = () -> new Vehicle(
                vehicleSequence.incrementAndGet(),
                demoDataSet.vehicleCapacity,
                depotList.get(depotRandom.nextInt()));

        List<Vehicle> vehicleList = Stream.generate(vehicleSupplier)
                .limit(demoDataSet.vehicleCount)
                .collect(Collectors.toList());

        AtomicLong customerSequence = new AtomicLong();
        Supplier<Customer> customerSupplier = () -> new Customer(
                customerSequence.incrementAndGet(),
                new Location(latitudes.nextDouble(), longitudes.nextDouble()),
                demand.nextInt());

        List<Customer> customerList = Stream.generate(customerSupplier)
                .limit(demoDataSet.customerCount)
                .collect(Collectors.toList());

        return new VehicleRoutePlan(name, depotList, vehicleList, customerList, demoDataSet.southWestCorner,
                demoDataSet.northEastCorner);
    }
}
