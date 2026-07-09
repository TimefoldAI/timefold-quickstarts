package org.acme.orderpicking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.orderpicking.dto.OrderPickingConfigOverrides;
import org.acme.orderpicking.dto.OrderPickingInput;
import org.acme.orderpicking.dto.OrderPickingInputMetrics;
import org.acme.orderpicking.dto.OrderPickingOutput;
import org.acme.orderpicking.dto.OrderPickingOutputMetrics;
import org.acme.orderpicking.dto.PickTaskDTO;
import org.acme.orderpicking.dto.PickTaskIdDetail;
import org.acme.orderpicking.dto.TrolleyDTO;
import org.acme.orderpicking.dto.TrolleyIdDetail;
import org.acme.orderpicking.dto.WarehouseLocationDTO;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseLocation = new WarehouseLocationDTO("(A,1)", "LEFT", 1);
        var updatedLocation = baseLocation.withShelvingId("(B,2)")
                .withSide("RIGHT")
                .withRow(3);

        var basePickTask = new PickTaskDTO("p1", "o1", "prod1", "Milk", 100, baseLocation);
        var updatedPickTask = basePickTask.withId("p2")
                .withOrderId("o2")
                .withProductId("prod2")
                .withProductName("Bread")
                .withProductVolume(200)
                .withLocation(updatedLocation);

        var baseTrolley = new TrolleyDTO("t1", 4, 1000, baseLocation, List.of("p1"));
        var updatedTrolley = baseTrolley.withId("t2")
                .withBucketCount(5)
                .withBucketCapacity(2000)
                .withLocation(updatedLocation)
                .withPickTaskIds(List.of("p2"));

        var updatedTrolleyIdDetail = new TrolleyIdDetail("t1").withTrolleyId("t2");
        var updatedPickTaskIdDetail = new PickTaskIdDetail("p1").withPickTaskId("p2");

        var updatedOverrides = new OrderPickingConfigOverrides()
                .withMinimizeDistanceFromPreviousPickWeight(10L)
                .withMinimizeDistanceToPathOriginWeight(20L)
                .withMinimizeOrderSplitByTrolleyWeight(30L);

        var updatedInput = new OrderPickingInput(List.of(baseTrolley), List.of(basePickTask))
                .withTrolleys(List.of(updatedTrolley))
                .withPickTasks(List.of(updatedPickTask));

        var updatedOutput = new OrderPickingOutput(List.of(baseTrolley), List.of(basePickTask), "0hard/0soft")
                .withTrolleys(List.of(updatedTrolley))
                .withPickTasks(List.of(updatedPickTask))
                .withScore("1hard/1soft");

        var updatedInputMetrics = new OrderPickingInputMetrics(1, 2, 3, 4, 5L)
                .withTrolleys(10)
                .withOrders(20)
                .withPickTasks(30)
                .withProducts(40)
                .withTotalVolume(50L);

        var updatedOutputMetrics = new OrderPickingOutputMetrics(1, 2, 3, 4L)
                .withTotalAssignedPickTasks(10)
                .withTotalUnassignedPickTasks(20)
                .withTotalUsedTrolleys(30)
                .withTotalDistanceToTravel(40L);

        assertThat(updatedLocation.shelvingId()).isEqualTo("(B,2)");
        assertThat(updatedLocation.side()).isEqualTo("RIGHT");
        assertThat(updatedLocation.row()).isEqualTo(3);
        assertThat(updatedPickTask.id()).isEqualTo("p2");
        assertThat(updatedPickTask.orderId()).isEqualTo("o2");
        assertThat(updatedPickTask.productId()).isEqualTo("prod2");
        assertThat(updatedPickTask.productName()).isEqualTo("Bread");
        assertThat(updatedPickTask.productVolume()).isEqualTo(200);
        assertThat(updatedPickTask.location()).isEqualTo(updatedLocation);
        assertThat(updatedTrolley.id()).isEqualTo("t2");
        assertThat(updatedTrolley.bucketCount()).isEqualTo(5);
        assertThat(updatedTrolley.bucketCapacity()).isEqualTo(2000);
        assertThat(updatedTrolley.location()).isEqualTo(updatedLocation);
        assertThat(updatedTrolley.pickTaskIds()).containsExactly("p2");
        assertThat(updatedTrolleyIdDetail.trolleyId()).isEqualTo("t2");
        assertThat(updatedPickTaskIdDetail.pickTaskId()).isEqualTo("p2");
        assertThat(updatedOverrides.minimizeDistanceFromPreviousPickWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.minimizeDistanceToPathOriginWeight()).isEqualTo(20L);
        assertThat(updatedOverrides.minimizeOrderSplitByTrolleyWeight()).isEqualTo(30L);
        assertThat(updatedInput.trolleys()).containsExactly(updatedTrolley);
        assertThat(updatedInput.pickTasks()).containsExactly(updatedPickTask);
        assertThat(updatedOutput.trolleys()).containsExactly(updatedTrolley);
        assertThat(updatedOutput.pickTasks()).containsExactly(updatedPickTask);
        assertThat(updatedOutput.score()).isEqualTo("1hard/1soft");
        assertThat(updatedInputMetrics.trolleys()).isEqualTo(10);
        assertThat(updatedInputMetrics.orders()).isEqualTo(20);
        assertThat(updatedInputMetrics.pickTasks()).isEqualTo(30);
        assertThat(updatedInputMetrics.products()).isEqualTo(40);
        assertThat(updatedInputMetrics.totalVolume()).isEqualTo(50L);
        assertThat(updatedOutputMetrics.totalAssignedPickTasks()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedPickTasks()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedTrolleys()).isEqualTo(30);
        assertThat(updatedOutputMetrics.totalDistanceToTravel()).isEqualTo(40L);
    }
}
