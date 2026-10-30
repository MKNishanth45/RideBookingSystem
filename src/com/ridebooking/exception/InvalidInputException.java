package com.ridebooking.exception;

/**
 * Thrown when an input violates business validation rules.
 */
public class InvalidInputException extends RideBookingException {
    public InvalidInputException(String message) {
        super(message);
    }
}
