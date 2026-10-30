package com.ridebooking.service;

import com.ridebooking.model.VehicleType;

/**
 * Data Transfer Object representing the transparent breakdown of a ride fare.
 */
public class FareEstimate {
    private final double distanceKm;
    private final double durationMinutes;
    private final VehicleType vehicleType;
    private final double baseFare;           // Fixed Rs. 30
    private final double distanceFare;       // Distance * RatePerKm
    private final double timeFare;           // Duration * Rs. 1
    private final double subtotal;           // Base + Distance + Time
    private final double minFareAdjustment;  // Max(0, 50 - subtotal)
    private final double surgeMultiplier;    // 1.0 or 1.5
    private final double totalFare;          // (Subtotal + minFareAdjustment) * surgeMultiplier

    public FareEstimate(double distanceKm, double durationMinutes, VehicleType vehicleType,
                        double baseFare, double distanceFare, double timeFare,
                        double minFareAdjustment, double surgeMultiplier, double totalFare) {
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.vehicleType = vehicleType;
        this.baseFare = baseFare;
        this.distanceFare = distanceFare;
        this.timeFare = timeFare;
        this.subtotal = baseFare + distanceFare + timeFare;
        this.minFareAdjustment = minFareAdjustment;
        this.surgeMultiplier = surgeMultiplier;
        this.totalFare = Math.round(totalFare * 100.0) / 100.0;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getDurationMinutes() {
        return durationMinutes;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getDistanceFare() {
        return distanceFare;
    }

    public double getTimeFare() {
        return timeFare;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getMinFareAdjustment() {
        return minFareAdjustment;
    }

    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public boolean isSurgeApplied() {
        return surgeMultiplier > 1.0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Base Fare: Rs. %.2f | Distance Fare (%.2f km @ Rs. %.2f/km): Rs. %.2f | Time Fare (%.0f mins): Rs. %.2f\n",
                baseFare, distanceKm, vehicleType.getRatePerKm(), distanceFare, durationMinutes, timeFare));
        if (minFareAdjustment > 0) {
            sb.append(String.format("Minimum Fare Adjustment: +Rs. %.2f (Min threshold Rs. 50.00)\n", minFareAdjustment));
        }
        if (isSurgeApplied()) {
            sb.append(String.format("Surge Multiplier: %.1fx (High Demand)\n", surgeMultiplier));
        }
        sb.append(String.format("Total Estimated Fare: Rs. %.2f", totalFare));
        return sb.toString();
    }
}
