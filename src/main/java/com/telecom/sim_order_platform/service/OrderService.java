package com.telecom.sim_order_platform.service;

import com.telecom.sim_order_platform.dto.CreateOrderRequest;
import com.telecom.sim_order_platform.dto.CreateOrderResponse;
import com.telecom.sim_order_platform.dto.OrderResponse;
import com.telecom.sim_order_platform.model.OrderStatus;
import com.telecom.sim_order_platform.model.SimOrder;
import com.telecom.sim_order_platform.repository.InMemoryOrderRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final InMemoryOrderRepository orderRepository;

    public OrderService(InMemoryOrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request){

        SimOrder order = new SimOrder(
                UUID.randomUUID().toString(),
                request.customerId(),
                request.simType(),
                request.planCode(),
                OrderStatus.CREATED
        );

        orderRepository.save(order);

        return new CreateOrderResponse(
                order.orderId(),
                order.customerId(),
                order.status()
        );
    }

    public Optional<OrderResponse> getOrderById(String orderId){

        return orderRepository.findById(orderId)
                .map(order -> new OrderResponse(
                        order.orderId(),
                        order.customerId(),
                        order.simType(),
                        order.planCode(),
                        order.status()
                ));
    }


    public Optional<OrderResponse> reserveInventory(String orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    SimOrder updatedOrder = order.markInventoryReserved();

                    orderRepository.save(updatedOrder);
                    //Our ConcurrentHashMap repository replaces the value stored under the same order ID when we call save(updatedOrder).

                    return new OrderResponse(
                            updatedOrder.orderId(),
                            updatedOrder.customerId(),
                            updatedOrder.simType(),
                            updatedOrder.planCode(),
                            updatedOrder.status()
                    );
                });
    }

}
