package com.telecom.sim_order_platform.dto;

import com.telecom.sim_order_platform.model.OrderStatus;

public record OrderResponse(
        String orderId,
        String customerId,
        String simType,
        String planCode,
        OrderStatus status
) {
}
