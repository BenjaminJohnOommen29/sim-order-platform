package com.telecom.sim_order_platform.dto;

import com.telecom.sim_order_platform.model.OrderStatus;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderResponse(

        String orderId,
        String customerId,
        OrderStatus status
) {
}
