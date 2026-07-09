package org.acme.foodpackaging.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.Operator;
import org.acme.foodpackaging.domain.Product;
import org.junit.jupiter.api.Test;

class JobShadowVariableTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2024, 1, 1, 8, 0);

    private static Job newJob(String id, Product product) {
        return new Job(id, "Job " + id, product, Duration.ofMinutes(60), BASE, BASE.plusDays(1), BASE.plusDays(2), 1,
                false);
    }

    @Test
    void suppliersReturnNullWhenLineIsNull() {
        Job job = newJob("0", new Product("0", "Product 0"));
        assertThat(job.lineOperatorSupplier()).isNull();
        assertThat(job.startCleaningDateTimeSupplier()).isNull();
        assertThat(job.startProductionDateTimeSupplier()).isNull();
    }

    @Test
    void suppliersUseLineStartForFirstJob() {
        Operator operator = new Operator("op");
        Line line = new Line("L", "Line", operator, BASE);
        Job job = newJob("0", new Product("0", "Product 0"));
        job.setLine(line);

        assertThat(job.lineOperatorSupplier()).isEqualTo(operator);
        assertThat(job.startCleaningDateTimeSupplier()).isEqualTo(BASE);
        assertThat(job.startProductionDateTimeSupplier()).isEqualTo(BASE);
    }

    @Test
    void suppliersUsePreviousJobWhenChained() {
        Line line = new Line("L", "Line", BASE);

        Product previousProduct = new Product("0", "Product 0");
        Product product = new Product("1", "Product 1");
        product.setCleaningDurations(Map.of(previousProduct, Duration.ofMinutes(15)));

        Job previousJob = newJob("0", previousProduct);
        previousJob.setLine(line);
        previousJob.setEndDateTime(BASE.plusMinutes(120));

        Job job = newJob("1", product);
        job.setLine(line);
        job.setPreviousJob(previousJob);

        // startCleaning follows the previous job's end
        assertThat(job.startCleaningDateTimeSupplier()).isEqualTo(BASE.plusMinutes(120));

        // when the cleaning start is unknown, production start is null
        assertThat(job.startProductionDateTimeSupplier()).isNull();

        // once cleaning start is known, production start adds the cleanup duration
        job.setStartCleaningDateTime(BASE.plusMinutes(120));
        assertThat(job.startProductionDateTimeSupplier()).isEqualTo(BASE.plusMinutes(135));

        // end follows production start plus duration
        job.setStartProductionDateTime(BASE.plusMinutes(135));
        assertThat(job.endDateTimeSupplier()).isEqualTo(BASE.plusMinutes(195));
    }

    @Test
    void endSupplierReturnsNullWhenProductionStartUnknown() {
        Job job = newJob("0", new Product("0", "Product 0"));
        assertThat(job.endDateTimeSupplier()).isNull();
    }
}
