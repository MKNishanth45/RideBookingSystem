package com.ridebooking.exception;

/**
 * Thrown when a payment processing error occurs, such as insufficient wallet balance.
 */
public class PaymentException extends RideBookingException {
    public PaymentException(String message) {
        super(message);
    }
}
