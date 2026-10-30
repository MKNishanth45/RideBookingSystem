package com.ridebooking.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an accepted ride trip progressing through strict lifecycle stages.
 */
public class Trip {
    private final String id;
    private final String rideRequestId;
    private final String riderId;
    private final String driverId;
    private final String vehicleId;
    private final Location pickupLocation;
    private final Location dropLocation;
    private final double distanceKm;
    private double fare;
    private double cancellationFee;
    private final String otp;
    private TripStatus status;
    private String cancelledBy; // "RIDER", "DRIVER", "NONE"
    private String cancellationReason;

    private final LocalDateTime requestedTime;
    private LocalDateTime acceptedTime;
    private LocalDateTime driverArrivedTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Trip(String id, String rideRequestId, String riderId, String driverId, String vehicleId,
                Location pickupLocation, Location dropLocation, double distanceKm, double fare, String otp) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be null or empty.");
        }
        if (rideRequestId == null || riderId == null || driverId == null || vehicleId == null) {
            throw new IllegalArgumentException("Entities associated with trip cannot be null.");
        }
        if (pickupLocation == null || dropLocation == null) {
            throw new IllegalArgumentException("Pickup and drop locations cannot be null.");
        }
        if (otp == null || !otp.matches("\\d{4}")) {
            throw new IllegalArgumentException("Trip OTP must be a valid 4-digit numeric string.");
        }

        this.id = id.trim();
        this.rideRequestId = rideRequestId.trim();
        this.riderId = riderId.trim();
        this.driverId = driverId.trim();
        this.vehicleId = vehicleId.trim();
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.distanceKm = distanceKm;
        this.fare = Math.round(fare * 100.0) / 100.0;
        this.cancellationFee = 0.0;
        this.otp = otp;
        this.status = TripStatus.ACCEPTED; // Trip entity is instantiated upon driver acceptance
        this.cancelledBy = "NONE";
        this.cancellationReason = "";

        this.requestedTime = LocalDateTime.now();
        this.acceptedTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getRideRequestId() {
        return rideRequestId;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = Math.round(fare * 100.0) / 100.0;
    }

    public double getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(double cancellationFee) {
        this.cancellationFee = Math.round(cancellationFee * 100.0) / 100.0;
    }

    public String getOtp() {
        return otp;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getRequestedTime() {
        return requestedTime;
    }

    public LocalDateTime getAcceptedTime() {
        return acceptedTime;
    }

    public void setAcceptedTime(LocalDateTime acceptedTime) {
        this.acceptedTime = acceptedTime;
    }

    public LocalDateTime getDriverArrivedTime() {
        return driverArrivedTime;
    }

    public void setDriverArrivedTime(LocalDateTime driverArrivedTime) {
        this.driverArrivedTime = driverArrivedTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trip other)) return false;
        return id.equalsIgnoreCase(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("[%s] Rider: %s | Driver: %s | Route: %s -> %s (%.2f km) | Fare: Rs. %.2f | Status: %s | OTP: %s",
                id, riderId, driverId, pickupLocation.getAddress(), dropLocation.getAddress(), distanceKm, fare, status, otp);
    }
}
