package com.lidcoin.reserveFound.domain.excption;

public class TransactionLimitExceededException extends RuntimeException {
    public TransactionLimitExceededException(String message) {
        super(message);
    }

    public TransactionLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
