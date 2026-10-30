package com.ridebooking.cli;

import com.ridebooking.exception.*;
import com.ridebooking.model.*;
import com.ridebooking.service.*;
import java.util.*;

/**
 * Controller for Rider interactions, booking workflows, payment settlements, and ratings.
 */
public class RiderCli {
    private final RiderService riderService;
    private final TripService tripService;
    private final MatchingService matchingService;
    private final RatingService ratingService;
    private final FareCalculatorService fareCalculatorService;
    private final InputValidator input;

    public RiderCli(RiderService riderService, TripService tripService,
                    MatchingService matchingService, RatingService ratingService,
                    FareCalculatorService fareCalculatorService, Scanner scanner) {
        this.riderService = riderService;
        this.tripService = tripService;
        this.matchingService = matchingService;
        this.ratingService = ratingService;
        this.fareCalculatorService = fareCalculatorService;
        this.input = new InputValidator(scanner);
    }

    public void register() {
        ConsoleUi.printHeader("Rider Registration");
        String name = input.readString("Enter your full name: ", false);
        String phone = input.readPhone("Enter 10-digit phone number: ");
        String email = input.readEmail("Enter email address: ");
        double initialWallet = input.readDouble("Initial wallet deposit (Rs., min 0.0): ", 0.0);

        try {
            Rider rider = riderService.registerRider(name, phone, email, initialWallet);
            ConsoleUi.printSuccess("Registration Successful! Your Rider ID is: " + rider.getId());
            riderDashboard(rider);
        } catch (RideBookingException e) {
            ConsoleUi.printError(e.getMessage());
        }
    }

    public void login() {
        ConsoleUi.printHeader("Rider Login");
        String phone = input.readPhone("Enter registered 10-digit phone number: ");
        try {
            Rider rider = riderService.getRiderByPhone(phone);
            ConsoleUi.printSuccess("Welcome back, " + rider.getName() + "!");
            riderDashboard(rider);
        } catch (EntityNotFoundException e) {
            ConsoleUi.printError("Login Failed: " + e.getMessage());
        }
    }

    private void riderDashboard(Rider rider) {
        while (true) {
            // Refresh rider entity state
            rider = riderService.getRiderById(rider.getId());

            ConsoleUi.printHeader("Rider Dashboard - " + rider.getName() + " (" + rider.getId() + ")");
            System.out.printf("Wallet Balance: %s | Rating: %.1f★ (%d trips) | Flagged for Review: %s\n",
                    ConsoleUi.formatCurrency(rider.getWalletBalance()),
                    rider.getAverageRating(),
                    rider.getCompletedTripsCount(),
                    rider.isFlaggedForReview() ? "YES [Action Required]" : "No");

            if (riderService.hasUnresolvedPayments(rider.getId())) {
                ConsoleUi.printWarning("You have outstanding pending or failed payments! Bookings are blocked until cleared.");
            }

            System.out.println();
            System.out.println("1. Book a Ride");
            System.out.println("2. Manage Active Trip / View Status");
            System.out.println("3. Wallet Recharge");
            System.out.println("4. Settle Outstanding Dues");
            System.out.println("5. Trip History & Rating");
            System.out.println("6. Logout to Main Menu");

            int choice = input.readInt("\nSelect an option (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> bookRideFlow(rider);
                case 2 -> manageActiveTripFlow(rider);
                case 3 -> walletRechargeFlow(rider);
                case 4 -> settleOutstandingDuesFlow(rider);
                case 5 -> tripHistoryFlow(rider);
                case 6 -> {
                    ConsoleUi.printInfo("Logged out from Rider account.");
                    return;
                }
            }
        }
    }

    private void bookRideFlow(Rider rider) {
        ConsoleUi.printHeader("Book a Ride");

        // 1. Check unresolved payments
        if (riderService.hasUnresolvedPayments(rider.getId())) {
            ConsoleUi.printError("Booking Blocked: You have unresolved failed or pending payments. Please clear them from option 4.");
            return;
        }

        // 2. Check active trips
        Optional<Trip> activeTrip = tripService.getActiveTripForRider(rider.getId());
        if (activeTrip.isPresent()) {
            ConsoleUi.printWarning("You already have an active trip (" + activeTrip.get().getId() + ") in state: " + activeTrip.get().getStatus());
            ConsoleUi.printInfo("Please complete or cancel your active trip before booking another ride.");
            return;
        }

        // 3. Location input
        System.out.println("Select Pickup Location Mode:");
        System.out.println("1. Choose from popular locations");
        System.out.println("2. Enter custom coordinates");
        int locMode = input.readInt("Select (1-2): ", 1, 2);

        Location pickup;
        Location drop;

        if (locMode == 1) {
            List<Location> presets = getPopularLocations();
            System.out.println("\nAvailable Locations:");
            for (int i = 0; i < presets.size(); i++) {
                System.out.printf("%d. %s\n", (i + 1), presets.get(i));
            }
            int pIndex = input.readInt("Select Pickup Location (1-" + presets.size() + "): ", 1, presets.size()) - 1;
            int dIndex = input.readInt("Select Drop Location (1-" + presets.size() + "): ", 1, presets.size()) - 1;

            if (pIndex == dIndex) {
                ConsoleUi.printError("Pickup and drop locations cannot be identical.");
                return;
            }
            pickup = presets.get(pIndex);
            drop = presets.get(dIndex);
        } else {
            String pAddr = input.readString("Enter Pickup Landmark: ", false);
            double px = input.readDouble("Pickup X coordinate (km): ", -100.0);
            double py = input.readDouble("Pickup Y coordinate (km): ", -100.0);
            pickup = new Location(pAddr, px, py);

            String dAddr = input.readString("Enter Drop Landmark: ", false);
            double dx = input.readDouble("Drop X coordinate (km): ", -100.0);
            double dy = input.readDouble("Drop Y coordinate (km): ", -100.0);
            drop = new Location(dAddr, dx, dy);
        }

        double distance = pickup.distanceTo(drop);
        System.out.printf("\nCalculated Euclidean Distance: %.2f km\n", distance);

        // 4. Vehicle Selection
        System.out.println("\nSelect Vehicle Category:");
        VehicleType[] types = VehicleType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%d. %-15s (Rate: Rs. %.2f/km, Capacity: %d)\n",
                    (i + 1), types[i].getDisplayName(), types[i].getRatePerKm(), types[i].getCapacity());
        }
        int vChoice = input.readInt("Select Vehicle (1-" + types.length + "): ", 1, types.length);
        VehicleType selectedType = types[vChoice - 1];

        // 5. Fare Estimation & Surge Notice
        try {
            RideRequest request = tripService.createRideRequest(rider.getId(), pickup, drop, selectedType);
            System.out.println("\n========================================================");
            System.out.println("                 FARE ESTIMATE BREAKDOWN                ");
            System.out.println("========================================================");
            System.out.printf("Route: %s -> %s\n", pickup.getAddress(), drop.getAddress());
            System.out.printf("Distance: %.2f km | Category: %s\n", distance, selectedType.getDisplayName());
            System.out.printf("Base Fare: Rs. 30.00 | Rate/km: Rs. %.2f\n", selectedType.getRatePerKm());
            System.out.printf("Surge Multiplier: %.1fx %s\n", request.getSurgeMultiplier(),
                    request.getSurgeMultiplier() > 1.0 ? "(Surge applied due to high demand!)" : "(Standard)");
            System.out.printf("Estimated Total Fare: %s (Minimum Fare: Rs. 50.00)\n", ConsoleUi.formatCurrency(request.getEstimatedFare()));
            System.out.println("========================================================");

            if (!input.readConfirmation("Confirm and broadcast ride request to nearby drivers?")) {
                ConsoleUi.printInfo("Ride request cancelled by rider.");
                return;
            }

            // 6. Proximity Matching (Radius <= 5 km, Max 3 drivers)
            ConsoleUi.printInfo("Searching for nearest available " + selectedType.getDisplayName() + " drivers within 5 km...");
            List<MatchingCandidate> candidates = matchingService.findEligibleDrivers(pickup, selectedType, Collections.emptyList());

            if (candidates.isEmpty()) {
                ConsoleUi.printWarning("No available " + selectedType.getDisplayName() + " drivers found within 5 km radius. Please try again shortly.");
                return;
            }

            System.out.println("\nMatching Drivers Found (" + candidates.size() + "):");
            for (int i = 0; i < candidates.size(); i++) {
                MatchingCandidate c = candidates.get(i);
                System.out.printf("  %d. %s | Distance: %.2f km | Rating: %.1f★\n",
                        (i + 1), c.getDriver().getName(), c.getDistanceKm(), c.getDriver().getAverageRating());
            }

            // 7. Driver Acceptance Simulation
            ConsoleUi.printSection("Driver Dispatch Simulation");
            Trip acceptedTrip = null;

            for (int i = 0; i < candidates.size(); i++) {
                MatchingCandidate candidate = candidates.get(i);
                Driver d = candidate.getDriver();
                request.addOfferedDriver(d.getId());

                System.out.printf("\n[Offer %d/%d] Request sent to: %s (%.2f km away)...\n",
                        (i + 1), candidates.size(), d.getName(), candidate.getDistanceKm());
                System.out.println("Simulating driver response (30-second window):");
                System.out.println("1. Driver Accepts Offer");
                System.out.println("2. Driver Rejects / Ignores Offer");

                int simChoice = input.readInt("Simulate response (1-Accept, 2-Reject): ", 1, 2);
                if (simChoice == 1) {
                    acceptedTrip = tripService.acceptRide(request.getId(), d.getId());
                    ConsoleUi.printSuccess("Driver " + d.getName() + " accepted your ride request!");
                    break;
                } else {
                    ConsoleUi.printWarning("Driver " + d.getName() + " rejected the ride. Offering to next nearest driver...");
                }
            }

            if (acceptedTrip == null) {
                ConsoleUi.printError("All offered drivers rejected or timed out. Request has expired.");
                return;
            }

            // 8. Trip Confirmation Box
            ConsoleUi.printHeader("Trip Confirmed");
            System.out.println("Trip ID:         " + acceptedTrip.getId());
            System.out.println("Driver Name:     " + acceptedTrip.getDriverId());
            System.out.println("Pickup Location: " + acceptedTrip.getPickupLocation().getAddress());
            System.out.println("Drop Location:   " + acceptedTrip.getDropLocation().getAddress());
            System.out.println("Total Fare:      " + ConsoleUi.formatCurrency(acceptedTrip.getFare()));
            System.out.println("--------------------------------------------------------");
            System.out.println(">>> YOUR 4-DIGIT START OTP: " + acceptedTrip.getOtp() + " <<<");
            System.out.println("Share this OTP with your driver ONLY after they arrive!");
            System.out.println("--------------------------------------------------------");

        } catch (RideBookingException e) {
            ConsoleUi.printError(e.getMessage());
        }
    }

    private void manageActiveTripFlow(Rider rider) {
        ConsoleUi.printHeader("Active Trip Management");
        Optional<Trip> activeTripOpt = tripService.getActiveTripForRider(rider.getId());

        if (activeTripOpt.isEmpty()) {
            ConsoleUi.printInfo("You have no active or ongoing trips.");
            return;
        }

        Trip trip = activeTripOpt.get();
        ConsoleUi.printTripStatusTimeline(trip.getStatus().name());

        System.out.println("Trip ID:         " + trip.getId());
        System.out.println("Driver ID:       " + trip.getDriverId());
        System.out.println("Route:           " + trip.getPickupLocation().getAddress() + " -> " + trip.getDropLocation().getAddress());
        System.out.println("Distance:        " + trip.getDistanceKm() + " km");
        System.out.println("Estimated Fare:  " + ConsoleUi.formatCurrency(trip.getFare()));
        System.out.println("Status:          " + trip.getStatus());
        System.out.println("Start OTP:       " + trip.getOtp());

        if (trip.getStatus() == TripStatus.ACCEPTED || trip.getStatus() == TripStatus.DRIVER_ARRIVED) {
            System.out.println("\nOptions:");
            System.out.println("1. Cancel Trip");
            System.out.println("2. Refresh Status");
            System.out.println("3. Back to Dashboard");

            int opt = input.readInt("Select (1-3): ", 1, 3);
            if (opt == 1) {
                System.out.println("\n--- Cancellation Policy ---");
                System.out.println("- Free cancellation within 2 minutes of acceptance.");
                System.out.println("- Rs. 30 cancellation fee charged if more than 2 minutes have elapsed.");
                if (input.readConfirmation("Are you sure you want to cancel this trip?")) {
                    String reason = input.readString("Enter cancellation reason: ", false);
                    try {
                        tripService.cancelTripByRider(trip.getId(), rider.getId(), reason);
                        Trip updated = tripService.getTripById(trip.getId());
                        if (updated.getCancellationFee() > 0) {
                            ConsoleUi.printWarning(String.format("Trip cancelled. Cancellation fee of %s applied (>2 minutes elapsed).",
                                    ConsoleUi.formatCurrency(updated.getCancellationFee())));
                        } else {
                            ConsoleUi.printSuccess("Trip cancelled successfully. Free cancellation applied (within 2 minutes).");
                        }
                    } catch (RideBookingException e) {
                        ConsoleUi.printError(e.getMessage());
                    }
                }
            }
        } else if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            ConsoleUi.printInfo("Trip is currently in progress! Enjoy your ride. Cancellation is not permitted during transit.");
        }
    }

    private void walletRechargeFlow(Rider rider) {
        ConsoleUi.printHeader("Wallet Recharge");
        System.out.println("Current Wallet Balance: " + ConsoleUi.formatCurrency(rider.getWalletBalance()));
        double amount = input.readDouble("Enter top-up amount in Rs. (min 10.0): ", 10.0);
        try {
            riderService.addWalletBalance(rider.getId(), amount);
            Rider updated = riderService.getRiderById(rider.getId());
            ConsoleUi.printSuccess("Wallet recharged! New Balance: " + ConsoleUi.formatCurrency(updated.getWalletBalance()));
        } catch (RideBookingException e) {
            ConsoleUi.printError(e.getMessage());
        }
    }

    private void settleOutstandingDuesFlow(Rider rider) {
        ConsoleUi.printHeader("Settle Outstanding Dues");
        List<Payment> dues = riderService.getUnresolvedPayments(rider.getId());

        if (dues.isEmpty()) {
            ConsoleUi.printSuccess("No pending or failed dues found! Your account is in good standing.");
            return;
        }

        System.out.println("Unresolved Dues:");
        List<String> headers = List.of("Payment ID", "Trip ID", "Amount", "Status", "Remarks");
        List<List<String>> rows = new ArrayList<>();
        for (Payment p : dues) {
            rows.add(List.of(p.getId(), p.getTripId(), ConsoleUi.formatCurrency(p.getAmount()), p.getStatus().name(), p.getRemarks()));
        }
        ConsoleUi.printTable(headers, rows);

        System.out.println("\nSelect Payment ID to clear (or 0 to cancel):");
        String pId = input.readString("Enter Payment ID: ", false);
        if ("0".equals(pId)) return;

        System.out.println("Select Payment Method:");
        System.out.println("1. Wallet (Current balance: " + ConsoleUi.formatCurrency(rider.getWalletBalance()) + ")");
        System.out.println("2. UPI");
        System.out.println("3. Cash");
        int m = input.readInt("Select Method (1-3): ", 1, 3);
        PaymentMethod method = (m == 1) ? PaymentMethod.WALLET : (m == 2 ? PaymentMethod.UPI : PaymentMethod.CASH);

        try {
            riderService.resolvePayment(pId, method);
            ConsoleUi.printSuccess("Payment " + pId + " settled successfully! Your account is now unlocked for new bookings.");
        } catch (RideBookingException e) {
            ConsoleUi.printError(e.getMessage());
        }
    }

    private void tripHistoryFlow(Rider rider) {
        ConsoleUi.printHeader("Trip History & Ratings");
        List<Trip> trips = tripService.getTripHistoryForRider(rider.getId());

        if (trips.isEmpty()) {
            ConsoleUi.printInfo("No previous trips found.");
            return;
        }

        List<String> headers = List.of("Trip ID", "Driver ID", "Distance", "Fare", "Status");
        List<List<String>> rows = new ArrayList<>();
        for (Trip t : trips) {
            rows.add(List.of(t.getId(), t.getDriverId(), String.format("%.2f km", t.getDistanceKm()),
                    ConsoleUi.formatCurrency(t.getFare()), t.getStatus().name()));
        }
        ConsoleUi.printTable(headers, rows);

        if (input.readConfirmation("Would you like to rate a completed trip?")) {
            String tripId = input.readString("Enter Trip ID: ", false);
            int score = input.readInt("Enter rating score (1 to 5 stars): ", 1, 5);
            String feedback = input.readString("Enter feedback comments: ", true);
            try {
                Rating rating = ratingService.submitRating(tripId, rider.getId(), score, feedback);
                ConsoleUi.printSuccess("Rating submitted successfully! Thank you for your feedback.");
            } catch (RideBookingException e) {
                ConsoleUi.printError(e.getMessage());
            }
        }
    }

    private List<Location> getPopularLocations() {
        return List.of(
                new Location("Indiranagar 100ft Road", 1.0, 1.0),
                new Location("Koramangala Sony World Signal", 4.0, 5.0),
                new Location("MG Road Metro Station", 2.0, 2.5),
                new Location("Whitefield ITPL Gate 2", 8.0, 9.0),
                new Location("HSR Layout Sector 1", 5.0, 6.0),
                new Location("Electronic City Phase 1", 12.0, 14.0)
        );
    }
}
