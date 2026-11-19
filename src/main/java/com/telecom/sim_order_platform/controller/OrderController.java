package com.telecom.sim_order_platform.controller;

import com.telecom.sim_order_platform.dto.CreateOrderRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    public String createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return "SIM order created successfully!" + request.customerId() +
                ", sim type: " + request.simType() +
                ", plan:" + request.planCode();
    }


}
