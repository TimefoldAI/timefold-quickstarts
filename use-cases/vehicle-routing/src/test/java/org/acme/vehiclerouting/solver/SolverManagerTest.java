package org.acme.vehiclerouting.solver;

import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.acme.vehiclerouting.rest.VehicleRoutingDemoResource;
import org.junit.jupiter.api.Test;
import ai.timefold.solver.core.api.solver.SolverManager;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<VehicleRoutingSolution, Long> solverManager;

    @Test
    void solve() throws ExecutionException, InterruptedException {
//        VehicleRoutingSolution problem = VehicleRoutingDemoResource.DemoDataBuilder.builder().setMinDemand(1).setMaxDemand(2)
//                .setVehicleCapacity(25).setCustomerCount(75).setVehicleCount(6).setDepotCount(2)
//                .setSouthWestCorner(new Location(0L, 43.751466, 11.177210))
//                .setNorthEastCorner(new Location(0L, 43.809291, 11.290195)).build();
//        solverManager.solve(0L, id -> problem, SolverManagerTest::printSolution).getFinalBestSolution();
    }

    static void printSolution(VehicleRoutingSolution solution) {
        solution.getVehicleList().forEach(vehicle -> System.out.printf("%s: %s%n", vehicle,
                vehicle.getRoute().stream().map(Location::getId).collect(Collectors.toList())));
    }
}
