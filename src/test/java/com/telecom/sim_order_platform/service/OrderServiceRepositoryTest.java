
package com.telecom.sim_order_platform.service;

import com.telecom.sim_order_platform.dto.CreateOrderRequest;
import com.telecom.sim_order_platform.dto.CreateOrderResponse;
import com.telecom.sim_order_platform.dto.OrderResponse;
import com.telecom.sim_order_platform.model.OrderStatus;
import com.telecom.sim_order_platform.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class OrderServiceRepositoryTest {

    @Test
    void reserveInventory_shouldPersistUpdatedStatus() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();//Actual repository - not mockito

        OrderService service = new OrderService(repository);//Actual service injecting repository in it

        CreateOrderRequest request =
                new CreateOrderRequest(
                        "customer-123",
                        "PHYSICAL",
                        "PLAN-5G"
                );

        CreateOrderResponse created = service.createOrder(request);

        assertEquals(OrderStatus.CREATED, created.status());

        OrderResponse updated = service.reserveInventory(created.orderId())
                .orElseThrow();

        assertEquals(OrderStatus.INVENTORY_RESERVED, updated.status());

        OrderResponse retrieved = service.getOrderById(created.orderId())
                .orElseThrow();

        assertEquals(OrderStatus.INVENTORY_RESERVED, retrieved.status());
        assertEquals(created.orderId(), retrieved.orderId());

    }
}
