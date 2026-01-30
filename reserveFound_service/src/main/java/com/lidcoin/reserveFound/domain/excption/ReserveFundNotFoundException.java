package com.lidcoin.reserveFound.domain.excption;

public class ReserveFundNotFoundException extends RuntimeException {

    public ReserveFundNotFoundException(String message) {
        super(message);
    }

    public ReserveFundNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}