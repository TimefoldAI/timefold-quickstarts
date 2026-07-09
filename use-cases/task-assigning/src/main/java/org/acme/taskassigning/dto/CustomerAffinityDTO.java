package org.acme.taskassigning.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The affinity an employee has with a specific customer.")
public record CustomerAffinityDTO(
        @Schema(description = "Identifier of the customer the affinity applies to.") String customerId,
        @Schema(description = "Affinity level: NONE, LOW, MEDIUM or HIGH.") String affinity) {

    public CustomerAffinityDTO {
        customerId = customerId == null ? "" : customerId;
        affinity = affinity == null ? "" : affinity;
    }

    public CustomerAffinityDTO withCustomerId(String customerId) {
        return new CustomerAffinityDTO(customerId, affinity);
    }

    public CustomerAffinityDTO withAffinity(String affinity) {
        return new CustomerAffinityDTO(customerId, affinity);
    }
}
