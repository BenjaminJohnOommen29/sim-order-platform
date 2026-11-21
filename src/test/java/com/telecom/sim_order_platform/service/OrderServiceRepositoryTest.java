package com.telecom.sim_order_platform.service;

import com.telecom.sim_order_platform.dto.CreateOrderRequest;
import com.telecom.sim_order_platform.dto.CreateOrderResponse;
import com.telecom.sim_order_platform.dto.OrderResponse;
import com.telecom.sim_order_platform.exception.InvalidOrderStateTransitionException;
import com.telecom.sim_order_platform.model.OrderStatus;
import com.telecom.sim_order_platform.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class OrderServiceRepositoryTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository(); //Actual repository - not mockito
        service = new OrderService(repository); //Actual service injecting repository in it
    }

    @Test
    void reserveInventory_shouldPersistUpdatedStatus() {

        CreateOrderResponse created = createTestOrder(service);
        assertEquals(OrderStatus.CREATED, created.status());

        OrderResponse updated = service.reserveInventory(created.orderId()).orElseThrow();
        assertEquals(OrderStatus.INVENTORY_RESERVED, updated.status());

        OrderResponse retrieved = service.getOrderById(created.orderId()).orElseThrow();
        assertEquals(OrderStatus.INVENTORY_RESERVED, retrieved.status());
        assertEquals(created.orderId(), retrieved.orderId());

    }


    @Test
    void reserveInventory_shouldRejectSecondReservation() {
        CreateOrderResponse created = createTestOrder(service);
        assertEquals(OrderStatus.CREATED, created.status());

        service.reserveInventory(created.orderId());
        assertThrows(InvalidOrderStateTransitionException.class, () -> service.reserveInventory(created.orderId())// Second time calling reserve - hence failed.
        );
    }


    @Test
    void confirmOrder_shouldPersistConfirmedStatus() {
        CreateOrderResponse created = createTestOrder(service);
        String orderId = created.orderId();

        service.reserveInventory(orderId);

        OrderResponse confirmed = service.confirmOrder(orderId).orElseThrow();

        assertEquals(OrderStatus.CONFIRMED, confirmed.status());

        OrderResponse retrieved = service.getOrderById(orderId).orElseThrow();

        assertEquals(OrderStatus.CONFIRMED, retrieved.status());

        //Preventing repetition
        assertThrows(InvalidOrderStateTransitionException.class, () -> service.confirmOrder(created.orderId()));
    }


    @Test
    void confirmOrder_shouldRejectOrderWithoutInventoryReservation() {
        CreateOrderResponse created = createTestOrder(service);

        assertThrows(InvalidOrderStateTransitionException.class, () -> service.confirmOrder(created.orderId()));
    }


    @Test
    void dispatchOrder_shouldPersistDispatchedStatus() {
        CreateOrderResponse created = createTestOrder(service);
        String orderId = created.orderId();

        service.reserveInventory(orderId);
        service.confirmOrder(orderId);

        OrderResponse dispatched = service.dispatchOrder(orderId).orElseThrow();
        assertEquals(OrderStatus.DISPATCHED, dispatched.status());

        OrderResponse retrieved = service.getOrderById(orderId).orElseThrow();
        assertEquals(OrderStatus.DISPATCHED, retrieved.status());

        assertThrows(InvalidOrderStateTransitionException.class, () -> service.dispatchOrder(created.orderId()));

    }


    @Test
    void activateOrder_shouldPersistActivatedStatus() {
        CreateOrderResponse created = createTestOrder(service);
        String orderId = created.orderId();

        service.reserveInventory(orderId);
        service.confirmOrder(orderId);
        service.dispatchOrder(orderId);

        OrderResponse activated = service.activateOrder(orderId).orElseThrow();

        assertEquals(OrderStatus.ACTIVATED, activated.status());

        OrderResponse retrieved = service.getOrderById(orderId).orElseThrow();

        assertEquals(OrderStatus.ACTIVATED, retrieved.status());

        assertThrows(InvalidOrderStateTransitionException.class, () -> service.activateOrder(created.orderId()));

    }


    @Test
    void rejectOrder_shouldPersistRejectedStatusAndPreventReservation() {
        CreateOrderResponse created = createTestOrder(service);
        String orderId = created.orderId();

        OrderResponse rejected = service.rejectOrder(orderId).orElseThrow();

        assertEquals(OrderStatus.REJECTED, rejected.status());

        OrderResponse retrieved = service.getOrderById(orderId).orElseThrow();

        assertEquals(OrderStatus.REJECTED, retrieved.status());

        assertThrows(InvalidOrderStateTransitionException.class, () -> service.reserveInventory(orderId));
    }


    //Utility functions
    // Shared test helper
    private CreateOrderResponse createTestOrder(OrderService service) {
        CreateOrderRequest request = new CreateOrderRequest("customer-123", "PHYSICAL", "PLAN-5G");

        return service.createOrder(request);
    }


}
