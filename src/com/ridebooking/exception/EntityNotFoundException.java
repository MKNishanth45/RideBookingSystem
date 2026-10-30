package com.ridebooking.exception;

/**
 * Thrown when an expected entity (Rider, Driver, Trip, etc.) cannot be located.
 */
public class EntityNotFoundException extends RideBookingException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
