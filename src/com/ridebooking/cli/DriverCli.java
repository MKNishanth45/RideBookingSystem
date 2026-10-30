package com.ridebooking.cli;

import com.ridebooking.exception.*;
import com.ridebooking.model.*;
import com.ridebooking.service.*;
import java.util.*;

/**
 * Controller for Driver interactions, operational status, vehicle setup,
 * OTP verification, trip progression, cancellations, and earnings overview.
 */
public class DriverCli {
    private final DriverService driverService;
    private final TripService tripService;
    private final RatingService ratingService;
    private final InputValidator input;

    public DriverCli(DriverService driverService, TripService tripService,
                     RatingService ratingService, Scanner scanner) {
        this.driverService = driverService;
        this.tripService = tripService;
        this.ratingService = ratingService;
        this.input = new InputValidator(scanner);
    }

    public void register() {
        ConsoleUi.printHeader("Driver Registration");
        String name = input.readString("Enter your full name: ", false);
        String phone = input.readPhone("Enter 10-digit phone number: ");
        String email = input.readEmail("Enter email address: ");
        String license = input.readString("Enter Driving License number: ", false);

        System.out.println("\nEnter your starting location coordinates:");
        String addr = input.readString("Current Landmark/Area: ", false);
        double x = input.readDouble("Current X coordinate (km): ", -100.0);
        double y = input.readDouble("Current Y coordinate (km): ", -100.0);
        Location loc = new Location(addr, x, y);

        try {
            Driver driver = driverService.registerDriver(name, phone, email, license, loc);
            ConsoleUi.printSuccess("Registration submitted! Your Driver ID is: " + driver.getId());
            ConsoleUi.printInfo("Note: Your account is PENDING_APPROVAL. An admin must approve your profile before you can go online.");

            if (input.readConfirmation("Would you like to register your vehicle now?")) {
                registerVehicleFlow(driver);
            }
            driverDashboard(driver);
        } catch (RideBookingException e) {
            ConsoleUi.printError(e.getMessage());
        }
    }

    public void login() {
        ConsoleUi.printHeader("Driver Login");
        String phone = input.readPhone("Enter registered 10-digit phone number: ");
        try {
            Driver driver = driverService.getDriverByPhone(phone);
            ConsoleUi.printSuccess("Welcome back, " + driver.getName() + "!");
            driverDashboard(driver);
        } catch (EntityNotFoundException e) {
            ConsoleUi.printError("Login Failed: " + e.getMessage());
        }
    }

    private void driverDashboard(Driver driver) {
        while (true) {
            // Refresh driver entity state
            driver = driverService.getDriverById(driver.getId());

            ConsoleUi.printHeader("Driver Dashboard - " + driver.getName() + " (" + driver.getId() + ")");
            System.out.printf("Status: %s | Availability: %s | Rating: %.1f★ (%d rides)\n",
                    driver.getStatus(), driver.getAvailability(), driver.getAverageRating(), driver.getCompletedTripsCount());
            System.out.printf("Active Vehicle: %s | Total Earnings: %s | Daily Cancellations: %d/%d\n",
                    driver.getActiveVehicle() != null ? driver.getActiveVehicle().getMakeModel() + " (" + driver.getActiveVehicle().getRegistrationNumber() + ")" : "None",
                    ConsoleUi.formatCurrency(driver.getTotalEarnings()),
                    driver.getDailyCancellationCount(), DriverService.MAX_DAILY_CANCELLATIONS);
            System.out.printf("Current Location: %s\n", driver.getCurrentLocation());

            if (driver.getStatus() == DriverStatus.SUSPENDED) {
                ConsoleUi.printError("YOUR ACCOUNT IS SUSPENDED due to average rating below 3.0 after 20 rides! You cannot go online.");
            } else if (driver.getStatus() == DriverStatus.PENDING_APPROVAL) {
                ConsoleUi.printWarning("Your profile is pending admin approval.");
            }

            System.out.println();
            System.out.println("1. Toggle Availability (Go Online / Offline)");
            System.out.println("2. Manage Active Trip");
            System.out.println("3. Register / Manage Vehicle");
            System.out.println("4. Update Current Location");
            System.out.println("5. Trip History & Rating");
            System.out.println("6. Logout to Main Menu");

            int choice = input.readInt("\nSelect an option (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> toggleAvailabilityFlow(driver);
                case 2 -> manageActiveTripFlow(driver);
                case 3 -> registerVehicleFlow(driver);
                case 4 -> updateLocationFlow(driver);
                case 5 -> tripHistoryFlow(driver);
                case 6 -> {
                    ConsoleUi.printInfo("Logged out from Driver account.");
                    return;
                }
            }
        }
    }

    private void toggleAvailabilityFlow(Driver driver) {
        ConsoleUi.printHeader("Toggle Availability");
        System.out.println("Current Availability: " + driver.getAvailability());
        System.out.println("1. Set to AVAILABLE (Go Online)");
        System.out.println("2. Set to OFFLINE (Go Offline)");
        int choice = input.readInt("Select (1-2): ", 1, 2);

        DriverAvailability target = (choice == 1) ? DriverAvailability.AVAILABLE : DriverAvailability.OFFLINE;
        try {
            driverService.setDriverAvailability(driver.getId(), target);
            ConsoleUi.printSuccess("Availability successfully updated to: " + target);
        } catch (RideBookingException e) {
            ConsoleUi.printError("Cannot change availability: " + e.getMessage());
        }
    }

    private void registerVehicleFlow(Driver driver) {
        ConsoleUi.printHeader("Vehicle Management");
        String regNumber = input.readString("Enter Vehicle Registration Number (e.g. KA-01-AB-1234): ", false);
        String makeModel = input.readString("Enter Vehicle Make & Model (e.g. Maruti Dzire, Honda Activa): ", false);

        System.out.println("\nSelect Vehicle Category:");
        VehicleType[] types = VehicleType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%d. %s\n", (i + 1), types[i].getDisplayName());
        }
        int typeChoice = input.readInt("Select Category (1-" + types.length + "): ", 1, types.length);
        VehicleType selectedType = types[typeChoice - 1];

        try {
            Vehicle vehicle = driverService.registerVehicle(driver.getId(), regNumber, makeModel, selectedType);
            ConsoleUi.printSuccess("Vehicle registered successfully! Assigned Vehicle ID: " + vehicle.getId());
        } catch (RideBookingException e) {
            ConsoleUi.printError(e.getMessage());
        }
    }

    private void updateLocationFlow(Driver driver) {
        ConsoleUi.printHeader("Update Location");
        System.out.println("Current Location: " + driver.getCurrentLocation());
        String addr = input.readString("Enter New Landmark: ", false);
        double x = input.readDouble("New X coordinate (km): ", -100.0);
        double y = input.readDouble("New Y coordinate (km): ", -100.0);

        driverService.updateDriverLocation(driver.getId(), new Location(addr, x, y));
        ConsoleUi.printSuccess("Current location updated!");
    }

    private void manageActiveTripFlow(Driver driver) {
        ConsoleUi.printHeader("Active Trip Console");
        Optional<Trip> activeTripOpt = tripService.getActiveTripForDriver(driver.getId());

        if (activeTripOpt.isEmpty()) {
            ConsoleUi.printInfo("You have no active trips at this time.");
            return;
        }

        Trip trip = activeTripOpt.get();
        ConsoleUi.printTripStatusTimeline(trip.getStatus().name());

        System.out.println("Trip ID:         " + trip.getId());
        System.out.println("Rider ID:        " + trip.getRiderId());
        System.out.println("Pickup:          " + trip.getPickupLocation().getAddress());
        System.out.println("Drop:            " + trip.getDropLocation().getAddress());
        System.out.println("Fare:            " + ConsoleUi.formatCurrency(trip.getFare()));
        System.out.println("Your 80% Share:  " + ConsoleUi.formatCurrency(trip.getFare() * 0.80));
        System.out.println("Current Status:  " + trip.getStatus());

        if (trip.getStatus() == TripStatus.ACCEPTED) {
            System.out.println("\nActions:");
            System.out.println("1. Mark 'Arrived at Pickup Location'");
            System.out.println("2. Cancel Trip (Daily cancellations remaining: " + (DriverService.MAX_DAILY_CANCELLATIONS - driver.getDailyCancellationCount()) + ")");
            System.out.println("3. Back");

            int act = input.readInt("Select Action (1-3): ", 1, 3);
            if (act == 1) {
                try {
                    tripService.driverArrived(trip.getId(), driver.getId());
                    ConsoleUi.printSuccess("Status updated to DRIVER_ARRIVED! Please ask passenger for their 4-digit start OTP.");
                } catch (RideBookingException e) {
                    ConsoleUi.printError(e.getMessage());
                }
            } else if (act == 2) {
                if (input.readConfirmation("Confirm trip cancellation? (Counts towards your 3/day limit)")) {
                    String reason = input.readString("Enter cancellation reason: ", false);
                    try {
                        tripService.cancelTripByDriver(trip.getId(), driver.getId(), reason);
                        ConsoleUi.printSuccess("Trip cancelled. You are now Available for new ride offers.");
                    } catch (RideBookingException e) {
                        ConsoleUi.printError(e.getMessage());
                    }
                }
            }
        } else if (trip.getStatus() == TripStatus.DRIVER_ARRIVED) {
            System.out.println("\nActions:");
            System.out.println("1. Enter 4-Digit Rider OTP to Start Trip");
            System.out.println("2. Back");

            int act = input.readInt("Select (1-2): ", 1, 2);
            if (act == 1) {
                String enteredOtp = input.readString("Enter 4-digit OTP provided by rider: ", false);
                try {
                    tripService.startTrip(trip.getId(), driver.getId(), enteredOtp);
                    ConsoleUi.printSuccess("OTP Verified! Trip is now IN_PROGRESS. Drive safely!");
                } catch (RideBookingException e) {
                    ConsoleUi.printError(e.getMessage());
                }
            }
        } else if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            System.out.println("\nActions:");
            System.out.println("1. Complete Trip & Collect Payment");
            System.out.println("2. Back");

            int act = input.readInt("Select (1-2): ", 1, 2);
            if (act == 1) {
                System.out.println("Select Payment Tender Method:");
                System.out.println("1. Cash (Collect " + ConsoleUi.formatCurrency(trip.getFare()) + " directly from rider)");
                System.out.println("2. UPI (Rider scanned QR code)");
                System.out.println("3. Wallet (Automated in-app deduction)");

                int pm = input.readInt("Select Tender (1-3): ", 1, 3);
                PaymentMethod method = (pm == 1) ? PaymentMethod.CASH : (pm == 2 ? PaymentMethod.UPI : PaymentMethod.WALLET);

                try {
                    Payment payment = tripService.completeTrip(trip.getId(), method);
                    ConsoleUi.printSuccess("Trip successfully completed!");
                    System.out.println("--------------------------------------------------------");
                    System.out.println("Total Fare:             " + ConsoleUi.formatCurrency(payment.getAmount()));
                    System.out.println("Your 80% Earnings:      " + ConsoleUi.formatCurrency(payment.getDriverShare()));
                    System.out.println("Platform Commission 20%: " + ConsoleUi.formatCurrency(payment.getPlatformShare()));
                    System.out.println("Payment Status:         " + payment.getStatus());
                    System.out.println("Remarks:                " + payment.getRemarks());
                    System.out.println("--------------------------------------------------------");

                    if (input.readConfirmation("Would you like to rate the rider now?")) {
                        int score = input.readInt("Rate passenger (1 to 5 stars): ", 1, 5);
                        String comments = input.readString("Comments: ", true);
                        ratingService.submitRating(trip.getId(), driver.getId(), score, comments);
                        ConsoleUi.printSuccess("Rider rating submitted!");
                    }
                } catch (RideBookingException e) {
                    ConsoleUi.printError(e.getMessage());
                }
            }
        }
    }

    private void tripHistoryFlow(Driver driver) {
        ConsoleUi.printHeader("Driver Trip History & Ratings");
        List<Trip> trips = tripService.getTripHistoryForDriver(driver.getId());

        if (trips.isEmpty()) {
            ConsoleUi.printInfo("No completed trips found.");
            return;
        }

        List<String> headers = List.of("Trip ID", "Rider ID", "Distance", "Total Fare", "Status");
        List<List<String>> rows = new ArrayList<>();
        for (Trip t : trips) {
            rows.add(List.of(t.getId(), t.getRiderId(), String.format("%.2f km", t.getDistanceKm()),
                    ConsoleUi.formatCurrency(t.getFare()), t.getStatus().name()));
        }
        ConsoleUi.printTable(headers, rows);
    }
}
