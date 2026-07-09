package org.acme.vehiclerouting.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A geographic location with a latitude and a longitude.")
public record LocationDTO(
        @Schema(description = "Latitude of the location.") double latitude,
        @Schema(description = "Longitude of the location.") double longitude) {

    public LocationDTO {
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            throw new IllegalArgumentException("Location coordinates must be valid numbers.");
        }
    }

    public LocationDTO withLatitude(double latitude) {
        return new LocationDTO(latitude, longitude);
    }

    public LocationDTO withLongitude(double longitude) {
        return new LocationDTO(latitude, longitude);
    }
}
