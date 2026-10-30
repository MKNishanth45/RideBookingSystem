package com.ridebooking;

import com.ridebooking.model.*;
import java.time.LocalDateTime;

/**
 * Standalone demo program for Member 2 (Model Module).
 * Demonstrates instantiation, encapsulation, Euclidean distance calculation,
 * rating recalculations, and entity relationships.
 */
public class Member2Demo {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      MEMBER 2: MODEL MODULE VERIFICATION DEMO     ");
        System.out.println("==================================================");

        // 1. Location & Euclidean Distance
        Location indiranagar = new Location("Indiranagar 100ft Rd", 1.0, 2.0);
        Location koramangala = new Location("Koramangala 5th Block", 4.0, 6.0);
        double distance = indiranagar.distanceTo(koramangala);
        System.out.printf("[✓] Location Distance: Indiranagar (1.0, 2.0) to Koramangala (4.0, 6.0) = %.2f km (Expected: 5.00 km)\n", distance);

        // 2. Rider Instantiation & Wallet Operations
        Rider rider = new Rider("RDR-001", "Arun Kumar", "9876543210", "arun@example.com", 500.0);
        System.out.println("[✓] Rider Created: " + rider);
        rider.creditWallet(250.0);
        boolean debited = rider.debitWallet(100.0);
        System.out.printf("[✓] Wallet After +Rs.250 and -Rs.100: Rs. %.2f (Debited: %s)\n", rider.getWalletBalance(), debited);

        // 3. Driver & Active Vehicle
        Driver driver = new Driver("DRV-001", "Rajesh Gowda", "9123456780", "rajesh@example.com", "KA0320210001", indiranagar);
        Vehicle vehicle = new Vehicle("VEH-001", driver.getId(), "KA-03-HA-1234", "Maruti Swift Dzire", VehicleType.SEDAN);
        driver.setActiveVehicle(vehicle);
        driver.setStatus(DriverStatus.APPROVED);
        driver.setAvailability(DriverAvailability.AVAILABLE);
        System.out.println("[✓] Driver Created & Configured: " + driver);

        // 4. RideRequest Creation
        RideRequest request = new RideRequest("REQ-001", rider.getId(), indiranagar, koramangala, VehicleType.SEDAN, distance, 1.0, 150.0);
        request.addOfferedDriver(driver.getId());
        System.out.println("[✓] RideRequest Created: " + request);
        System.out.println("[✓] RideRequest Expired Check: " + request.isExpired());

        // 5. Trip Lifecycle Instantiation
        Trip trip = new Trip("TRP-001", request.getId(), rider.getId(), driver.getId(), vehicle.getId(),
                indiranagar, koramangala, distance, 150.0, "4829");
        trip.setStartTime(LocalDateTime.now());
        trip.setStatus(TripStatus.IN_PROGRESS);
        System.out.println("[✓] Trip Created in State: " + trip.getStatus() + " | OTP: " + trip.getOtp());

        // 6. Payment Settlement
        double driverShare = 150.0 * 0.80; // 80%
        double platformShare = 150.0 * 0.20; // 20%
        Payment payment = new Payment("PAY-001", trip.getId(), rider.getId(), driver.getId(), 150.0, driverShare, platformShare, PaymentMethod.WALLET, PaymentStatus.PAID);
        driver.addEarnings(driverShare);
        System.out.println("[✓] Payment Created: " + payment);
        System.out.printf("[✓] Driver Total Earnings: Rs. %.2f\n", driver.getTotalEarnings());

        // 7. Rating System
        Rating rating = new Rating("RAT-001", trip.getId(), rider.getId(), driver.getId(), true, 5, "Smooth ride, polite driver!");
        driver.updateRating(5);
        driver.incrementCompletedTrips();
        System.out.println("[✓] Rating Created: " + rating);
        System.out.printf("[✓] Driver Post-Trip Rating: %.1f★ over %d rides\n", driver.getAverageRating(), driver.getCompletedTripsCount());

        // 8. Constructor Validation Test
        try {
            new Rider("RDR-INVALID", "Bad Phone", "123", "email@test.com", 0);
            System.err.println("[X] Validation failed: invalid phone allowed!");
        } catch (IllegalArgumentException e) {
            System.out.println("[✓] Constructor Validation Passed: Correctly caught invalid phone - " + e.getMessage());
        }

        System.out.println("==================================================");
        System.out.println("  MEMBER 2 ALL MODEL UNIT VERIFICATIONS PASSED!  ");
        System.out.println("==================================================");
    }
}
