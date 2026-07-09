package org.acme.taskassigning.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A customer for whom tasks are performed.")
public record CustomerDTO(
        @Schema(description = "Unique identifier of the customer.") String id,
        @Schema(description = "Display name of the customer.") String name) {

    public CustomerDTO {
        id = id == null ? "" : id;
        name = name == null ? "" : name;
    }

    public CustomerDTO withId(String id) {
        return new CustomerDTO(id, name);
    }

    public CustomerDTO withName(String name) {
        return new CustomerDTO(id, name);
    }
}
