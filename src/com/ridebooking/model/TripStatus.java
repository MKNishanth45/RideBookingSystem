package com.ridebooking.model;

/**
 * Lifecycle states of a trip.
 * Transitions must follow strictly:
 * REQUESTED -> ACCEPTED -> DRIVER_ARRIVED -> IN_PROGRESS -> COMPLETED
 * or CANCELLED from non-active states.
 */
public enum TripStatus {
    REQUESTED,
    ACCEPTED,
    DRIVER_ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
