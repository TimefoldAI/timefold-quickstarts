package org.acme.orderpicking.rest;

import jakarta.ws.rs.Path;

import ai.timefold.solver.service.rest.api.ModelRest;

@Path("/order-picking-plans")
public interface OrderPickingResource extends ModelRest {
}
