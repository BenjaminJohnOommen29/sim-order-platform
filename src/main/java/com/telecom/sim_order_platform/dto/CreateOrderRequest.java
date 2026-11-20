package com.telecom.sim_order_platform.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(

        @NotBlank(message = "Customer ID is required")
        String customerId,
        @NotBlank
        String simType,
        @NotBlank
        String planCode
) {
}
