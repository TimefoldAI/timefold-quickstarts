package org.acme.vehiclerouting.domain.dto;

import org.acme.vehiclerouting.domain.Customer;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;

public record ApplyRecommendationRequest(VehicleRoutePlan solution, Customer customer, String vehicleId, int index) {
}
