package org.acme.facilitylocation.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a consumer ID validation issue.")
public record ConsumerIdDetail(
        @Schema(description = "The ID of the consumer.") String consumerId) implements IssueMetadata {

    public ConsumerIdDetail {
        consumerId = consumerId == null ? "" : consumerId;
    }

    public ConsumerIdDetail withConsumerId(String consumerId) {
        return new ConsumerIdDetail(consumerId);
    }

    @Override
    public String getType() {
        return "ConsumerId";
    }
}
