package com.connectify.service;

public class IzipayPaymentException extends RuntimeException {

    public IzipayPaymentException(String message) {
        super(message);
    }

    public IzipayPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
