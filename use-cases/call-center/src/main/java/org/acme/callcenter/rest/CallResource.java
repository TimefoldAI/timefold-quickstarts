package org.acme.callcenter.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.acme.callcenter.service.SimulationService;
import org.acme.callcenter.service.SolverService;

@Path("/call")
public class CallResource {

    @Inject
    SolverService solverService;

    @Inject
    SimulationService simulationService;

    @DELETE
    @Path("{id}")
    public void deleteCall(@PathParam("id") String id) {
        solverService.removeCall(id);
    }

    @PUT
    @Path("{id}")
    public void prolongCall(@PathParam("id") String id) {
        solverService.prolongCall(id);
        simulationService.prolongCall(id);
    }
}
