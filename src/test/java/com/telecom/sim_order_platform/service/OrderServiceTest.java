package com.telecom.sim_order_platform.service;

import com.telecom.sim_order_platform.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private InMemoryOrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldReturnCreatedStatus() {

    }
}
