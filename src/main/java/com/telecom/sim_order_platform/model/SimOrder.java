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
            System.out.println("markInventoryReserved throw exception :" + this);
            throw new InvalidOrderStateTransitionException(
                    "Cannot reserve inventory for order : " + this
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

    public SimOrder markConfirmed() {

        if (status != OrderStatus.INVENTORY_RESERVED) {
            System.out.println("markConfirmed throw exception :" + this);
            throw new InvalidOrderStateTransitionException(
                    "Cannot confirm order for : " + this
            );
        }

        return new SimOrder(
                orderId,
                customerId,
                simType,
                planCode,
                OrderStatus.CONFIRMED
        );
    }

    public SimOrder markDispatched() {

        if (status != OrderStatus.CONFIRMED) {
            System.out.println("markDispatched throw exception :" + this);
            throw new InvalidOrderStateTransitionException(
                    "Cannot dispatch order for : " + this
            );
        }

        return new SimOrder(
                orderId,
                customerId,
                simType,
                planCode,
                OrderStatus.DISPATCHED
        );
    }


    public SimOrder markActivated() {
        if (status != OrderStatus.DISPATCHED) {
            throw new InvalidOrderStateTransitionException(
                    "Cannot activate order for : " + this
            );
        }

        return new SimOrder(
                orderId,
                customerId,
                simType,
                planCode,
                OrderStatus.ACTIVATED
        );
    }


    public SimOrder markRejected() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateTransitionException(
                    "Cannot reject order in status: " + status
            );
        }

        return new SimOrder(
                orderId,
                customerId,
                simType,
                planCode,
                OrderStatus.REJECTED
        );
    }





}
