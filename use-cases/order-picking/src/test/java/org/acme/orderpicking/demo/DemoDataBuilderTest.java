package org.acme.orderpicking.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.TrolleyInputDTO;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        PickingPlanInput problem = DemoDataBuilder.builder().build();

        assertThat(problem.products()).hasSize(37);
        assertThat(problem.products()).extracting(ProductInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.products()).allSatisfy(product -> {
            assertThat(product.location().row()).isBetween(1, 10);
            assertThat(product.volume()).isLessThanOrEqualTo(problem.trolleys().getFirst().bucketCapacity());
        });

        assertThat(problem.pickTasks()).isNotEmpty();
        assertThat(problem.pickTasks()).extracting(PickTaskInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.pickTasks().stream().map(PickTaskInputDTO::orderId).distinct()).hasSize(8);

        assertThat(problem.trolleys()).hasSize(5);
        assertThat(problem.trolleys()).allSatisfy(trolley -> {
            assertThat(trolley.bucketCount()).isEqualTo(4);
            assertThat(trolley.location()).isEqualTo(problem.trolleys().getFirst().location());
        });
        assertThat(problem.trolleys()).extracting(TrolleyInputDTO::pickTaskIds).allSatisfy(
                pickTaskIds -> assertThat(pickTaskIds).isEmpty());
    }

    @Test
    void shouldBeReproducible() {
        assertThat(DemoDataBuilder.builder().build()).isEqualTo(DemoDataBuilder.builder().build());
    }
}
