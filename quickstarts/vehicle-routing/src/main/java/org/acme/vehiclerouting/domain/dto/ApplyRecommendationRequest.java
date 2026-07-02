package org.acme.vehiclerouting.domain.dto;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;

public record ApplyRecommendationRequest(VehicleRoutePlan solution, String visitId, String vehicleId, int index) {
}
