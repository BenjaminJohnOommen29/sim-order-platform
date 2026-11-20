package com.telecom.sim_order_platform.service;

import com.telecom.sim_order_platform.dto.CreateOrderRequest;
import com.telecom.sim_order_platform.dto.CreateOrderResponse;
import com.telecom.sim_order_platform.dto.OrderResponse;
import com.telecom.sim_order_platform.model.OrderStatus;
import com.telecom.sim_order_platform.model.SimOrder;
import com.telecom.sim_order_platform.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private InMemoryOrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldReturnCreatedStatus() {

        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                "customer-123",
                "PHYSICAL",
                "PLAN-5G"
        );

        // Act
        CreateOrderResponse response = orderService.createOrder(request);
        // Assert
        assertEquals(OrderStatus.CREATED, response.status());
        verify(orderRepository).save(any(SimOrder.class));
        //Verify checks whether the repository function is being called once. Mockito verification checks interactions between objects.
        assertEquals("customer-123", response.customerId());

    }

    @Test
    void getOrderById_shouldReturnOrderWhenFound() {

        // Arrange
        String orderId = "order-123";

        SimOrder existingOrder = new SimOrder(
                orderId,
                "customer-123",
                "PHYSICAL",
                "PLAN-5G",
                OrderStatus.CREATED
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder)); //Stubbing

        // Act
        Optional<OrderResponse> result = orderService.getOrderById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().orderId());
        assertEquals("customer-123", result.get().customerId());
        assertEquals(OrderStatus.CREATED, result.get().status());
    }

    @Test
    void getOrderById_shouldReturnEmptyWhenOrderNotFound() {

        // Arrange
        String orderId = "non-existing-order";

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act
        Optional<OrderResponse> result =
                orderService.getOrderById(orderId);

        // Assert
        assertTrue(result.isEmpty());
    }


}
