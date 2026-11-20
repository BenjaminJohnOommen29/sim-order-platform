package com.telecom.sim_order_platform.model;

import com.telecom.sim_order_platform.exception.InvalidOrderStateTransitionException;

public record SimOrder(
        String orderId,
        String customerId,
        String simType,
        String planCode,
        OrderStatus status
) {


        public SimOrder markInventoryReserved() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateTransitionException(
                    "Cannot reserve inventory for order in status: " + status
            );
        }

        return new SimOrder(
                orderId,
                customerId,
                simType,
                planCode,
                OrderStatus.INVENTORY_RESERVED //Java records are immutable. We cannot simply overwrite
        );
    }


}
