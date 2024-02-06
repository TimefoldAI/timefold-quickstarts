package org.acme.vehiclerouting.domain.dto;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;

public record ApplyRecommendationRequest(VehicleRoutePlan solution, String customerId, String vehicleId, int index) {
}
