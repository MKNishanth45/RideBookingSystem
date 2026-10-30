package com.ridebooking.service;

import com.ridebooking.model.VehicleType;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service handling fare computation, surge dynamics, and cancellation fee calculations.
 */
public class FareCalculatorService {
    public static final double BASE_FARE = 30.0;
    public static final double MINIMUM_FARE = 50.0;
    public static final double PER_MINUTE_RATE = 1.0;
    public static final double SURGE_MULTIPLIER = 1.5;
    public static final double CANCELLATION_FEE_AMOUNT = 30.0;
    public static final long FREE_CANCELLATION_MINUTES = 2;

    /**
     * Calculates the transparent fare breakdown for a trip.
     * Fare = Rs. 30 + (Distance * Rate per km) + (Duration * Rs. 1)
     * If total < Rs. 50, rounded up to Minimum Fare (Rs. 50).
     * Surge: 1.5x applied when pending requests > 2 * available drivers.
     *
     * @param distanceKm Distance in kilometers.
     * @param vehicleType Selected vehicle category.
     * @param pendingRequestsCount Number of current active pending ride requests.
     * @param availableDriversCount Number of available drivers in the system.
     * @return Complete breakdown wrapped in FareEstimate.
     */
    public FareEstimate calculateFare(double distanceKm, VehicleType vehicleType,
                                      int pendingRequestsCount, int availableDriversCount) {
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }

        // Standard speed approximation: 30 km/h in city traffic -> ~2 minutes per km
        double estimatedDurationMinutes = Math.max(5.0, Math.round(distanceKm * 2.0));

        double distanceFare = distanceKm * vehicleType.getRatePerKm();
        double timeFare = estimatedDurationMinutes * PER_MINUTE_RATE;
        double subtotal = BASE_FARE + distanceFare + timeFare;

        double minFareAdjustment = 0.0;
        if (subtotal < MINIMUM_FARE) {
            minFareAdjustment = MINIMUM_FARE - subtotal;
        }

        // Surge rule: Apply 1.5 multiplier when pending requests > 2 * available drivers
        boolean surgeApplicable = (availableDriversCount == 0 && pendingRequestsCount > 0) ||
                                  (pendingRequestsCount > 2 * availableDriversCount);
        double surgeMultiplier = surgeApplicable ? SURGE_MULTIPLIER : 1.0;

        double totalFare = (subtotal + minFareAdjustment) * surgeMultiplier;

        return new FareEstimate(distanceKm, estimatedDurationMinutes, vehicleType,
                BASE_FARE, distanceFare, timeFare, minFareAdjustment, surgeMultiplier, totalFare);
    }

    /**
     * Calculates cancellation fee according to policy:
     * - Free within 2 minutes of acceptance.
     * - Rs. 30 after 2 minutes.
     *
     * @param acceptedTime Timestamp when trip was accepted by driver.
     * @param cancelTime Timestamp when cancellation was requested.
     * @return Cancellation fee (0.0 or 30.0).
     */
    public double calculateCancellationFee(LocalDateTime acceptedTime, LocalDateTime cancelTime) {
        if (acceptedTime == null || cancelTime == null) {
            return 0.0;
        }
        Duration duration = Duration.between(acceptedTime, cancelTime);
        long minutesElapsed = duration.toMinutes();

        if (minutesElapsed > FREE_CANCELLATION_MINUTES) {
            return CANCELLATION_FEE_AMOUNT;
        }
        return 0.0;
    }
}
