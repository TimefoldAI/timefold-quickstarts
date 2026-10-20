package org.acme.orderpicking.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingCartInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        PickingPlanInput problem = DemoDataBuilder.builder().build();

        assertThat(problem.products()).hasSize(37);
        assertThat(problem.products()).extracting(ProductInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.products()).allSatisfy(product -> {
            assertThat(product.location().bay()).isBetween(1, 10);
            assertThat(product.volume()).isLessThanOrEqualTo(problem.pickingCarts().getFirst().bucketCapacity());
        });

        assertThat(problem.pickTasks()).isNotEmpty();
        assertThat(problem.pickTasks()).extracting(PickTaskInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.pickTasks().stream().map(PickTaskInputDTO::orderId).distinct()).hasSize(8);

        assertThat(problem.pickingCarts()).hasSize(5);
        assertThat(problem.pickingCarts()).allSatisfy(pickingCart -> {
            assertThat(pickingCart.bucketCount()).isEqualTo(4);
            assertThat(pickingCart.location()).isEqualTo(problem.pickingCarts().getFirst().location());
        });
        assertThat(problem.pickingCarts()).extracting(PickingCartInputDTO::pickTaskIds).allSatisfy(
                pickTaskIds -> assertThat(pickTaskIds).isEmpty());
    }

    @Test
    void shouldBeReproducible() {
        assertThat(DemoDataBuilder.builder().build()).isEqualTo(DemoDataBuilder.builder().build());
    }
}
