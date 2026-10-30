package com.ridebooking.exception;

/**
 * Base unchecked exception for the Ride Booking System.
 */
public class RideBookingException extends RuntimeException {
    public RideBookingException(String message) {
        super(message);
    }

    public RideBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
