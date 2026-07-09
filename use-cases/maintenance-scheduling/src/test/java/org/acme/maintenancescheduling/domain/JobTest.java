package org.acme.maintenancescheduling.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

import org.junit.jupiter.api.Test;

class JobTest {

    private static SequencedSet<String> tags(String... values) {
        SequencedSet<String> set = new LinkedHashSet<>();
        for (String value : values) {
            set.add(value);
        }
        return set;
    }

    @Test
    void endDateSupplierSkipsWeekends() {
        // Friday 2024-01-05 + 2 business days -> Tuesday 2024-01-09 (skips the weekend).
        Job job = new Job("1", "Job 1", 2, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31),
                LocalDate.of(2024, 1, 15), tags("A"), new Crew("1", "Crew1"), LocalDate.of(2024, 1, 5));

        assertThat(job.endDateSupplier()).isEqualTo(LocalDate.of(2024, 1, 9));
    }

    @Test
    void endDateSupplierReturnsNullWithoutStartDate() {
        Job job = new Job("1", "Job 1", 2, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31),
                LocalDate.of(2024, 1, 15), tags("A"));

        assertThat(job.endDateSupplier()).isNull();
    }
}
