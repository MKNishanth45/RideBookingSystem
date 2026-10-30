package com.ridebooking.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a ride request initiated by a rider.
 * Encapsulates matching state, offered drivers, surge details, and 2-minute expiration.
 */
public class RideRequest {
    private final String id;
    private final String riderId;
    private final Location pickupLocation;
    private final Location dropLocation;
    private final VehicleType vehicleType;
    private final double estimatedDistanceKm;
    private final double surgeMultiplier;
    private final double estimatedFare;
    private final LocalDateTime requestedTime;
    private final LocalDateTime expiryTime;
    private final List<String> offeredDriverIds;
    private String status; // PENDING, ACCEPTED, EXPIRED, CANCELLED

    public RideRequest(String id, String riderId, Location pickupLocation, Location dropLocation,
                       VehicleType vehicleType, double estimatedDistanceKm, double surgeMultiplier, double estimatedFare) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ride request ID cannot be null or empty.");
        }
        if (riderId == null || riderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider ID cannot be null or empty.");
        }
        if (pickupLocation == null || dropLocation == null) {
            throw new IllegalArgumentException("Pickup and drop locations cannot be null.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }
        if (estimatedDistanceKm <= 0) {
            throw new IllegalArgumentException("Estimated distance must be positive.");
        }

        this.id = id.trim();
        this.riderId = riderId.trim();
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.vehicleType = vehicleType;
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.surgeMultiplier = surgeMultiplier;
        this.estimatedFare = Math.round(estimatedFare * 100.0) / 100.0;
        this.requestedTime = LocalDateTime.now();
        this.expiryTime = this.requestedTime.plusMinutes(2); // Expire after two minutes rule
        this.offeredDriverIds = new ArrayList<>();
        this.status = "PENDING";
    }

    public String getId() {
        return id;
    }

    public String getRiderId() {
        return riderId;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public double getEstimatedFare() {
        return estimatedFare;
    }

    public LocalDateTime getRequestedTime() {
        return requestedTime;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return "PENDING".equalsIgnoreCase(this.status) && LocalDateTime.now().isAfter(this.expiryTime);
    }

    public List<String> getOfferedDriverIds() {
        return Collections.unmodifiableList(offeredDriverIds);
    }

    public void addOfferedDriver(String driverId) {
        if (driverId != null && !offeredDriverIds.contains(driverId)) {
            offeredDriverIds.add(driverId);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RideRequest other)) return false;
        return id.equalsIgnoreCase(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("[%s] Rider: %s | %s -> %s | Type: %s | Est. Dist: %.2f km | Est. Fare: Rs. %.2f (Surge: %.1fx) | Status: %s",
                id, riderId, pickupLocation.getAddress(), dropLocation.getAddress(), vehicleType.getDisplayName(),
                estimatedDistanceKm, estimatedFare, surgeMultiplier, status);
    }
}
