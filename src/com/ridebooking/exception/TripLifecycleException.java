package com.ridebooking.exception;

/**
 * Thrown when an illegal trip state transition or timing violation is attempted.
 */
public class TripLifecycleException extends RideBookingException {
    public TripLifecycleException(String message) {
        super(message);
    }
}
