package com.ridebooking.exception;

/**
 * Thrown when an actor attempts an action they are not permitted to perform
 * (e.g., driver acting on another driver's trip, unapproved driver going online).
 */
public class UnauthorizedException extends RideBookingException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
