package org.acme.vehiclerouting.domain;

import ai.timefold.solver.service.maps.api.model.Location;

public interface LocationAware {

    Location getLocation();
}
