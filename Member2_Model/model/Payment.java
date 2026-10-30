package com.ridebooking.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a payment transaction for a trip or cancellation fee.
 * Encapsulates driver revenue share (80% on trips, 100% on cancellations)
 * and platform commission (20% on trips, 0% on cancellations).
 */
public class Payment {
    private final String id;
    private final String tripId;
    private final String riderId;
    private final String driverId;
    private final double amount;
    private final double driverShare;
    private final double platformShare;
    private final PaymentMethod method;
    private PaymentStatus status;
    private final LocalDateTime timestamp;
    private String remarks;

    public Payment(String id, String tripId, String riderId, String driverId, double amount,
                   double driverShare, double platformShare, PaymentMethod method, PaymentStatus status) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be null or empty.");
        }
        if (tripId == null || riderId == null || driverId == null) {
            throw new IllegalArgumentException("Payment references cannot be null.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative.");
        }
        if (method == null || status == null) {
            throw new IllegalArgumentException("Payment method and status cannot be null.");
        }

        this.id = id.trim();
        this.tripId = tripId.trim();
        this.riderId = riderId.trim();
        this.driverId = driverId.trim();
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.driverShare = Math.round(driverShare * 100.0) / 100.0;
        this.platformShare = Math.round(platformShare * 100.0) / 100.0;
        this.method = method;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.remarks = "";
    }

    public String getId() {
        return id;
    }

    public String getTripId() {
        return tripId;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getDriverId() {
        return driverId;
    }

    public double getAmount() {
        return amount;
    }

    public double getDriverShare() {
        return driverShare;
    }

    public double getPlatformShare() {
        return platformShare;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment other)) return false;
        return id.equalsIgnoreCase(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("[%s] Trip: %s | Amount: Rs. %.2f (Driver: Rs. %.2f, Platform: Rs. %.2f) | Method: %s | Status: %s",
                id, tripId, amount, driverShare, platformShare, method, status);
    }
}
