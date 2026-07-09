package org.acme.facilitylocation;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.facilitylocation.dto.ConsumerDTO;
import org.acme.facilitylocation.dto.LocationDTO;
import org.junit.jupiter.api.Test;

class ConsumerDTOTest {

    @Test
    void blankFacilityIdIsNormalizedToNull() {
        var consumer = new ConsumerDTO("consumer-1", new LocationDTO(0.0, 0.0), 1L, "", false);

        assertThat(consumer.facilityId()).isNull();
    }
}
