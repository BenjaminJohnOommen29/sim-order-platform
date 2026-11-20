package com.telecom.sim_order_platform.controller;

import com.telecom.sim_order_platform.dto.CreateOrderRequest;
import com.telecom.sim_order_platform.dto.CreateOrderResponse;
import com.telecom.sim_order_platform.model.OrderStatus;
import com.telecom.sim_order_platform.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
@WebMvcTest(OrderController.class) loads a focused Spring MVC test context for the controller, rather than the entire application.
@Autowired MockMvc gives us a way to simulate HTTP requests without starting a real web server.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean //Here Spring manages the mock as a bean - @Mock we need to manage the dependency and all - like where to inject etc.
    private OrderService orderService;

    @Test
    void createOrder_shouldReturn201Created() throws Exception {

        // Arrange
        CreateOrderResponse serviceResponse = new CreateOrderResponse(
                "order-123",
                "customer-123",
                OrderStatus.CREATED
        );

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(serviceResponse);

        String requestJson = """
            {
                "customerId": "customer-123",
                "simType": "PHYSICAL",
                "planCode": "PLAN-5G"
            }
            """;


        // Act + Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createOrder_shouldReturn400WhenCustomerIdIsBlank() throws Exception {

        // Arrange
        String invalidRequestJson = """
            {
                "customerId": "",
                "simType": "PHYSICAL",
                "planCode": "PLAN-5G"
            }
            """;

        // Act + Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());

        // Service should not be called for invalid input
        verifyNoInteractions(orderService);
    }
}