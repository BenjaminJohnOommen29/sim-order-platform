package com.telecom.sim_order_platform.repository;

import com.telecom.sim_order_platform.model.SimOrder;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository {

    private final Map<String, SimOrder> orders = new ConcurrentHashMap<>(); //Normal hashmap is not thread safe - Concurrent calls from two customers might fail

    public void save(SimOrder order){
        orders.put(order.orderId(),order);
    }

    public Optional<SimOrder> findById(String orderId){ //Optional just explicitly communicates - this method might return an order or null.
        return Optional.ofNullable(orders.get(orderId));
    }

}
