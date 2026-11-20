package com.telecom.sim_order_platform.controller;

import com.telecom.sim_order_platform.dto.CreateOrderRequest;
import com.telecom.sim_order_platform.dto.CreateOrderResponse;
import com.telecom.sim_order_platform.dto.OrderResponse;
import com.telecom.sim_order_platform.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService; // Only OrderController can access this field - Encapsulation & cannot be reassigned.

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {

        CreateOrderResponse message = orderService.createOrder(request);

        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(message);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable String orderId
    ){
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping("/{orderId}/reserve-inventory")
    public ResponseEntity<OrderResponse> reserveInventory(@PathVariable String orderId) {

        return orderService.reserveInventory(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());
    }
}
