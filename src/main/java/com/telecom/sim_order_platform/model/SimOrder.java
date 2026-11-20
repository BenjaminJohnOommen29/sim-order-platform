package com.telecom.sim_order_platform.model;

public record SimOrder(
        String orderId,
        String customerId,
        String simType,
        String planCode,
        OrderStatus status
) {
}
