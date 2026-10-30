package com.ridebooking.model;

/**
 * Enumeration representing available vehicle categories in the platform,
 * along with their base rate per kilometer and seating capacity.
 */
public enum VehicleType {
    BIKE(6.0, 1, "Bike"),
    AUTO(10.0, 3, "Auto Rickshaw"),
    MINI(12.0, 4, "Mini Hatchback"),
    SEDAN(15.0, 4, "Prime Sedan"),
    SUV(20.0, 6, "Prime SUV");

    private final double ratePerKm;
    private final int capacity;
    private final String displayName;

    VehicleType(double ratePerKm, int capacity, String displayName) {
        this.ratePerKm = ratePerKm;
        this.capacity = capacity;
        this.displayName = displayName;
    }

    public double getRatePerKm() {
        return ratePerKm;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getDisplayName() {
        return displayName;
    }
}
