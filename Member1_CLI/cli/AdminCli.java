package com.ridebooking.cli;

import com.ridebooking.exception.RideBookingException;
import com.ridebooking.model.*;
import com.ridebooking.repository.PaymentRepository;
import com.ridebooking.repository.RideRequestRepository;
import com.ridebooking.repository.TripRepository;
import com.ridebooking.service.DriverService;
import com.ridebooking.service.FareCalculatorService;
import com.ridebooking.service.RiderService;
import java.util.*;

/**
 * Controller for Administrative oversight, driver approvals, compliance monitoring,
 * and platform financial metrics.
 */
public class AdminCli {
    private final RiderService riderService;
    private final DriverService driverService;
    private final TripRepository tripRepository;
    private final PaymentRepository paymentRepository;
    private final RideRequestRepository rideRequestRepository;
    private final FareCalculatorService fareCalculatorService;
    private final InputValidator input;

    public AdminCli(RiderService riderService, DriverService driverService,
                    TripRepository tripRepository, PaymentRepository paymentRepository,
                    RideRequestRepository rideRequestRepository, FareCalculatorService fareCalculatorService,
                    Scanner scanner) {
        this.riderService = riderService;
        this.driverService = driverService;
        this.tripRepository = tripRepository;
        this.paymentRepository = paymentRepository;
        this.rideRequestRepository = rideRequestRepository;
        this.fareCalculatorService = fareCalculatorService;
        this.input = new InputValidator(scanner);
    }

    public void showMenu() {
        while (true) {
            ConsoleUi.printHeader("Admin & System Management Console");
            System.out.println("1. View All Registered Riders (Check Review Flags)");
            System.out.println("2. View All Drivers & Compliance Status");
            System.out.println("3. Review Pending Driver Approvals");
            System.out.println("4. Platform Financial & Revenue Summary");
            System.out.println("5. Live Surge & Demand Status");
            System.out.println("6. Return to Main Menu");

            int choice = input.readInt("\nSelect option (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> viewRiders();
                case 2 -> viewDrivers();
                case 3 -> reviewPendingDrivers();
                case 4 -> financialSummary();
                case 5 -> surgeStatus();
                case 6 -> {
                    return;
                }
            }
        }
    }

    private void viewRiders() {
        ConsoleUi.printHeader("Registered Riders");
        List<Rider> riders = riderService.getAllRiders();

        List<String> headers = List.of("ID", "Name", "Phone", "Wallet", "Rating", "Trips", "Review Flag");
        List<List<String>> rows = new ArrayList<>();
        for (Rider r : riders) {
            rows.add(List.of(
                    r.getId(),
                    r.getName(),
                    r.getPhone(),
                    ConsoleUi.formatCurrency(r.getWalletBalance()),
                    String.format("%.1f★", r.getAverageRating()),
                    String.valueOf(r.getCompletedTripsCount()),
                    r.isFlaggedForReview() ? "[!] FLAGGED (<3.0★ after 10+ rides)" : "Clean"
            ));
        }
        ConsoleUi.printTable(headers, rows);
    }

    private void viewDrivers() {
        ConsoleUi.printHeader("Registered Drivers");
        List<Driver> drivers = driverService.getAllDrivers();

        List<String> headers = List.of("ID", "Name", "Phone", "Status", "Avail", "Rating", "Trips", "Earnings", "Vehicle");
        List<List<String>> rows = new ArrayList<>();
        for (Driver d : drivers) {
            String veh = (d.getActiveVehicle() != null) ? d.getActiveVehicle().getMakeModel() : "None";
            rows.add(List.of(
                    d.getId(),
                    d.getName(),
                    d.getPhone(),
                    d.getStatus().name(),
                    d.getAvailability().name(),
                    String.format("%.1f★", d.getAverageRating()),
                    String.valueOf(d.getCompletedTripsCount()),
                    ConsoleUi.formatCurrency(d.getTotalEarnings()),
                    veh
            ));
        }
        ConsoleUi.printTable(headers, rows);
    }

    private void reviewPendingDrivers() {
        ConsoleUi.printHeader("Pending Driver Approvals");
        List<Driver> pending = driverService.getAllDrivers().stream()
                .filter(d -> d.getStatus() == DriverStatus.PENDING_APPROVAL)
                .toList();

        if (pending.isEmpty()) {
            ConsoleUi.printSuccess("No pending driver applications! All drivers are reviewed.");
            return;
        }

        List<String> headers = List.of("ID", "Name", "Phone", "License", "Location");
        List<List<String>> rows = new ArrayList<>();
        for (Driver d : pending) {
            rows.add(List.of(d.getId(), d.getName(), d.getPhone(), d.getLicenseNumber(), d.getCurrentLocation().toString()));
        }
        ConsoleUi.printTable(headers, rows);

        System.out.println("\nEnter Driver ID to review (or 0 to cancel):");
        String driverId = input.readString("Driver ID: ", false);
        if ("0".equals(driverId)) return;

        System.out.println("1. Approve Driver");
        System.out.println("2. Reject Driver");
        int decision = input.readInt("Action (1-2): ", 1, 2);

        try {
            if (decision == 1) {
                driverService.approveDriver(driverId);
                ConsoleUi.printSuccess("Driver " + driverId + " approved! Driver can now add vehicles and go available.");
            } else {
                driverService.rejectDriver(driverId);
                ConsoleUi.printWarning("Driver " + driverId + " application rejected.");
            }
        } catch (RideBookingException e) {
            ConsoleUi.printError(e.getMessage());
        }
    }

    private void financialSummary() {
        ConsoleUi.printHeader("Platform Financial Performance");
        List<Payment> payments = paymentRepository.findAll();

        double totalFareProcessed = 0.0;
        double totalDriverEarnings = 0.0;
        double totalPlatformRevenue = 0.0;
        int successfulTrips = 0;
        int failedTransactions = 0;

        for (Payment p : payments) {
            if (p.getStatus() == PaymentStatus.PAID) {
                totalFareProcessed += p.getAmount();
                totalDriverEarnings += p.getDriverShare();
                totalPlatformRevenue += p.getPlatformShare();
                successfulTrips++;
            } else {
                failedTransactions++;
            }
        }

        System.out.println("========================================================");
        System.out.println("Total Paid Transactions:     " + successfulTrips);
        System.out.println("Pending / Failed Payments:   " + failedTransactions);
        System.out.println("Total Gross Fare Processed:  " + ConsoleUi.formatCurrency(totalFareProcessed));
        System.out.println("Total Paid to Drivers (80%): " + ConsoleUi.formatCurrency(totalDriverEarnings));
        System.out.println("Platform Commission Net (20%):" + ConsoleUi.formatCurrency(totalPlatformRevenue));
        System.out.println("========================================================");
    }

    private void surgeStatus() {
        ConsoleUi.printHeader("Live Demand & Surge Multiplier");
        int pendingRequests = rideRequestRepository.findPendingRequests().size();
        int availableDrivers = driverService.getAllDrivers().stream()
                .filter(d -> d.getAvailability() == DriverAvailability.AVAILABLE)
                .toList().size();

        boolean isSurge = (availableDrivers == 0 && pendingRequests > 0) || (pendingRequests > 2 * availableDrivers);

        System.out.println("Active Pending Requests: " + pendingRequests);
        System.out.println("Available Online Drivers: " + availableDrivers);
        System.out.println("Surge Trigger Condition: Pending > 2 * Available Drivers");
        System.out.println("Current Surge Multiplier: " + (isSurge ? "1.5x (HIGH DEMAND SURGE ACTIVE)" : "1.0x (Standard Pricing)"));
    }
}
