package com.telecom.sim_order_platform.exception;

public class InvalidOrderStateTransitionException
        extends RuntimeException {

    public InvalidOrderStateTransitionException(String message) {
        super(message);
    }
}