package com.ridebooking.model;

import java.util.Objects;

/**
 * Represents a geographical coordinate point and human-readable address.
 * Uses a 2D Euclidean coordinate system where 1.0 unit = 1.0 kilometer.
 * Calculates Euclidean distance: sqrt((x2 - x1)^2 + (y2 - y1)^2)
 */
public class Location {
    private final String address;
    private final double x;
    private final double y;

    public Location(String address, double x, double y) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty.");
        }
        this.address = address.trim();
        this.x = Math.round(x * 100.0) / 100.0;
        this.y = Math.round(y * 100.0) / 100.0;
    }

    public String getAddress() {
        return address;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Aliases for geo conventions
    public double getLatitude() {
        return x;
    }

    public double getLongitude() {
        return y;
    }

    /**
     * Calculates the Euclidean distance between this location and another location.
     * Formula: sqrt((x2 - x1)^2 + (y2 - y1)^2)
     *
     * @param other Target location.
     * @return Distance in kilometers rounded to two decimal places.
     */
    public double distanceTo(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("Target location cannot be null.");
        }
        double deltaX = this.x - other.x;
        double deltaY = this.y - other.y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return Math.round(distance * 100.0) / 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location other)) return false;
        return Double.compare(other.x, x) == 0 &&
               Double.compare(other.y, y) == 0 &&
               address.equalsIgnoreCase(other.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address.toLowerCase(), x, y);
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f, %.2f)", address, x, y);
    }
}
