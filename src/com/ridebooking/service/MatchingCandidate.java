package com.ridebooking.service;

import com.ridebooking.model.Driver;

/**
 * Represents a matching driver candidate and their distance from pickup.
 */
public class MatchingCandidate implements Comparable<MatchingCandidate> {
    private final Driver driver;
    private final double distanceKm;

    public MatchingCandidate(Driver driver, double distanceKm) {
        this.driver = driver;
        this.distanceKm = distanceKm;
    }

    public Driver getDriver() {
        return driver;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    @Override
    public int compareTo(MatchingCandidate other) {
        return Double.compare(this.distanceKm, other.distanceKm);
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f km away) - %s",
                driver.getName(), distanceKm, driver.getActiveVehicle().getMakeModel());
    }
}
