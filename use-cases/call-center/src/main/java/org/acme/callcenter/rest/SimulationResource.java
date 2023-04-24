package org.acme.callcenter.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.acme.callcenter.service.SimulationService;

@Path("/simulation")
public class SimulationResource {

    @Inject
    SimulationService simulationService;

    @PUT
    public void updateSimulationParameters(SimulationParameters simulationParameters) {
        simulationService.restartSimulation(simulationParameters.getFrequency(), simulationParameters.getDuration());
    }
}
