package org.acme.facilitylocation.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Geographic location with latitude and longitude.")
public record LocationDTO(
        @Schema(description = "Latitude in decimal degrees.") double latitude,
        @Schema(description = "Longitude in decimal degrees.") double longitude) {

    public LocationDTO {
        // no-op compact constructor required by repository rules
    }

    public LocationDTO withLatitude(double latitude) {
        return new LocationDTO(latitude, longitude);
    }

    public LocationDTO withLongitude(double longitude) {
        return new LocationDTO(latitude, longitude);
    }
}
