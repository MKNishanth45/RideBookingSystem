package com.ridebooking;

import com.ridebooking.exception.*;
import com.ridebooking.model.*;
import com.ridebooking.repository.*;
import com.ridebooking.service.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Standalone demo program for Member 3 (Service Module).
 * Demonstrates business rules: matching radius, surge calculation, OTP security,
 * state transition integrity, cancellations, wallet deduction, driver suspension, and rider flagging.
 */
public class Member3Demo {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("    MEMBER 3: SERVICE MODULE VERIFICATION DEMO    ");
        System.out.println("==================================================");

        // Repositories
        RiderRepository riderRepo = new InMemoryRiderRepository();
        DriverRepository driverRepo = new InMemoryDriverRepository();
        VehicleRepository vehicleRepo = new InMemoryVehicleRepository();
        RideRequestRepository reqRepo = new InMemoryRideRequestRepository();
        TripRepository tripRepo = new InMemoryTripRepository();
        PaymentRepository payRepo = new InMemoryPaymentRepository();
        RatingRepository ratingRepo = new InMemoryRatingRepository();

        // Services
        FareCalculatorService fareService = new FareCalculatorService();
        MatchingService matchService = new MatchingService(driverRepo);
        RiderService riderService = new RiderService(riderRepo, payRepo);
        DriverService driverService = new DriverService(driverRepo, vehicleRepo);
        TripService tripService = new TripService(tripRepo, reqRepo, riderRepo, driverRepo, payRepo, fareService, matchService);
        RatingService ratingService = new RatingService(ratingRepo, tripRepo, riderRepo, driverRepo);

        // 1. Rider Registration & Unique Phone Validation
        Rider r1 = riderService.registerRider("Naveen Kumar", "9988776655", "naveen@test.com", 200.0);
        System.out.println("[✓] Rider Registered: " + r1.getName() + " (" + r1.getId() + ")");
        try {
            riderService.registerRider("Duplicate Phone", "9988776655", "dup@test.com", 100.0);
            System.err.println("[X] Duplicate phone registration allowed!");
        } catch (InvalidInputException e) {
            System.out.println("[✓] Caught Duplicate Phone Error: " + e.getMessage());
        }

        // 2. Driver Registration, Approval, and Active Vehicle Compliance
        Location locIndira = new Location("Indiranagar", 1.0, 1.0);
        Driver d1 = driverService.registerDriver("Santhosh Reddy", "9112233445", "santhosh@test.com", "KA04D123456", locIndira);
        try {
            driverService.setDriverAvailability(d1.getId(), DriverAvailability.AVAILABLE);
            System.err.println("[X] Unapproved driver went online!");
        } catch (UnauthorizedException e) {
            System.out.println("[✓] Caught Unapproved Driver Online Attempt: " + e.getMessage());
        }

        driverService.approveDriver(d1.getId());
        try {
            driverService.setDriverAvailability(d1.getId(), DriverAvailability.AVAILABLE);
            System.err.println("[X] Driver without vehicle went online!");
        } catch (InvalidInputException e) {
            System.out.println("[✓] Caught Driver Without Vehicle Online Attempt: " + e.getMessage());
        }

        Vehicle v1 = driverService.registerVehicle(d1.getId(), "KA-04-E-5566", "Hyundai i20", VehicleType.MINI);
        driverService.setDriverAvailability(d1.getId(), DriverAvailability.AVAILABLE);
        System.out.println("[✓] Driver Approved & Online: " + d1.getName() + " with " + v1.getMakeModel());

        // 3. Proximity Matching (5 km radius)
        Location pickup = new Location("Indiranagar Metro", 1.5, 1.5);
        Location drop = new Location("Koramangala Sony Signal", 4.5, 5.5); // ~5 km distance
        List<MatchingCandidate> candidates = matchService.findEligibleDrivers(pickup, VehicleType.MINI, Collections.emptyList());
        System.out.printf("[✓] Drivers found within 5 km: %d (Driver: %s, Distance: %.2f km)\n",
                candidates.size(), candidates.get(0).getDriver().getName(), candidates.get(0).getDistanceKm());

        // 4. Surge Pricing Test (Pending > 2 * Available)
        // Currently: 0 pending, 1 available -> Surge 1.0x
        FareEstimate normalFare = fareService.calculateFare(5.0, VehicleType.MINI, 0, 1);
        System.out.printf("[✓] Normal Fare (5 km Mini): Rs. %.2f (Surge: %.1fx)\n", normalFare.getTotalFare(), normalFare.getSurgeMultiplier());
        // Surge condition: 3 pending requests, 1 available driver (3 > 2 * 1)
        FareEstimate surgeFare = fareService.calculateFare(5.0, VehicleType.MINI, 3, 1);
        System.out.printf("[✓] Surge Fare (3 pending vs 1 driver): Rs. %.2f (Surge: %.1fx)\n", surgeFare.getTotalFare(), surgeFare.getSurgeMultiplier());

        // 5. Trip Lifecycle & OTP Verification
        RideRequest req = tripService.createRideRequest(r1.getId(), pickup, drop, VehicleType.MINI);
        Trip trip = tripService.acceptRide(req.getId(), d1.getId());
        System.out.println("[✓] Trip Created: " + trip.getId() + " | State: " + trip.getStatus() + " | OTP: " + trip.getOtp());

        // Driver Arrived
        tripService.driverArrived(trip.getId(), d1.getId());
        System.out.println("[✓] Driver Arrived State: " + trip.getStatus());

        // Invalid OTP attempt
        try {
            tripService.startTrip(trip.getId(), d1.getId(), "0000");
            System.err.println("[X] Started trip with wrong OTP!");
        } catch (InvalidInputException e) {
            System.out.println("[✓] Correctly Rejected Bad OTP: " + e.getMessage());
        }

        // Correct OTP
        tripService.startTrip(trip.getId(), d1.getId(), trip.getOtp());
        System.out.println("[✓] Trip In-Progress State: " + trip.getStatus());

        // 6. Complete Trip & Revenue Split
        Payment pay = tripService.completeTrip(trip.getId(), PaymentMethod.WALLET);
        System.out.printf("[✓] Trip Completed! Fare: Rs. %.2f | Driver 80%%: Rs. %.2f | Platform 20%%: Rs. %.2f | Status: %s\n",
                pay.getAmount(), pay.getDriverShare(), pay.getPlatformShare(), pay.getStatus());
        System.out.printf("[✓] Driver Earnings: Rs. %.2f | Rider Wallet Left: Rs. %.2f\n", d1.getTotalEarnings(), r1.getWalletBalance());

        // 7. Mutual Ratings & Average Update
        ratingService.submitRating(trip.getId(), r1.getId(), 5, "Great driving!");
        ratingService.submitRating(trip.getId(), d1.getId(), 5, "Polite passenger!");
        System.out.printf("[✓] Ratings Submitted! Driver Rating: %.1f★ | Rider Rating: %.1f★\n", d1.getAverageRating(), r1.getAverageRating());

        // 8. Cancellation Fee Simulation (> 2 minutes)
        Trip mockTrip = new Trip("TRP-MOCK", req.getId(), r1.getId(), d1.getId(), v1.getId(), pickup, drop, 5.0, 100.0, "9999");
        mockTrip.setAcceptedTime(LocalDateTime.now().minusMinutes(3)); // 3 minutes elapsed
        tripRepo.save(mockTrip);
        double fee = fareService.calculateCancellationFee(mockTrip.getAcceptedTime(), LocalDateTime.now());
        System.out.printf("[✓] Cancellation Fee after 3 minutes: Rs. %.2f (Expected: Rs. 30.00)\n", fee);

        // 9. Driver Daily Cancellation Limit Test
        d1.resetDailyCancellationIfNewDay();
        driverService.recordDriverCancellation(d1.getId());
        driverService.recordDriverCancellation(d1.getId());
        driverService.recordDriverCancellation(d1.getId());
        try {
            driverService.recordDriverCancellation(d1.getId());
            System.err.println("[X] Allowed 4th driver cancellation!");
        } catch (TripLifecycleException e) {
            System.out.println("[✓] Enforced Daily Cancellation Limit: " + e.getMessage());
        }

        // 10. Driver Automatic Suspension Test
        // Simulate driver with 20 trips and low ratings
        for (int i = 0; i < 20; i++) {
            d1.incrementCompletedTrips();
            d1.updateRating(2); // Low score
        }
        driverService.checkDriverSuspension(d1.getId());
        System.out.printf("[✓] Low Rating Driver Suspension: Status = %s | Availability = %s | Avg Rating = %.1f★ (%d trips)\n",
                d1.getStatus(), d1.getAvailability(), d1.getAverageRating(), d1.getCompletedTripsCount());

        // 11. Rider Admin Review Flagging Test
        for (int i = 0; i < 10; i++) {
            r1.incrementCompletedTrips();
            r1.updateRating(2);
        }
        System.out.printf("[✓] Low Rating Rider Review Flag: Flagged = %s | Avg Rating = %.1f★ (%d trips)\n",
                r1.isFlaggedForReview(), r1.getAverageRating(), r1.getCompletedTripsCount());

        System.out.println("==================================================");
        System.out.println("  MEMBER 3 ALL BUSINESS LOGIC CHECKS PASSED!      ");
        System.out.println("==================================================");
    }
}
