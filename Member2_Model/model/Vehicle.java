package com.ridebooking.model;

import java.util.Objects;

/**
 * Represents a vehicle registered in the ride booking platform.
 */
public class Vehicle {
    private final String id;
    private final String driverId;
    private final String registrationNumber;
    private final String makeModel;
    private final VehicleType vehicleType;
    private boolean active;

    public Vehicle(String id, String driverId, String registrationNumber, String makeModel, VehicleType vehicleType) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID cannot be null or empty.");
        }
        if (driverId == null || driverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty.");
        }
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or empty.");
        }
        if (makeModel == null || makeModel.trim().isEmpty()) {
            throw new IllegalArgumentException("Make and model cannot be null or empty.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }

        this.id = id.trim();
        this.driverId = driverId.trim();
        this.registrationNumber = registrationNumber.trim().toUpperCase();
        this.makeModel = makeModel.trim();
        this.vehicleType = vehicleType;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getMakeModel() {
        return makeModel;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle other)) return false;
        return id.equalsIgnoreCase(other.id) || registrationNumber.equalsIgnoreCase(other.registrationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrationNumber.toUpperCase());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s (Active: %s)", id, makeModel, registrationNumber, vehicleType.getDisplayName(), active ? "Yes" : "No");
    }
}
