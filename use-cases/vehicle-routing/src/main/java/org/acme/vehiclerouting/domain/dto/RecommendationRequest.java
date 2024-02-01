package org.acme.vehiclerouting.domain.dto;

import org.acme.vehiclerouting.domain.Customer;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;

public record RecommendationRequest(VehicleRoutePlan solution, Customer customer) {
}
