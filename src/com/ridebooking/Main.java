package com.ridebooking;

import com.ridebooking.cli.*;
import com.ridebooking.model.*;
import com.ridebooking.repository.*;
import com.ridebooking.service.*;
import java.util.Scanner;

/**
 * Application Entry Point.
 * Assembles layers using Dependency Injection, seeds realistic demo data,
 * and launches the interactive command-line interface.
 */
public class Main {
    public static void main(String[] args) {
        // 1. Initialize Repositories (Member 4)
        RiderRepository riderRepository = new InMemoryRiderRepository();
        DriverRepository driverRepository = new InMemoryDriverRepository();
        VehicleRepository vehicleRepository = new InMemoryVehicleRepository();
        RideRequestRepository rideRequestRepository = new InMemoryRideRequestRepository();
        TripRepository tripRepository = new InMemoryTripRepository();
        PaymentRepository paymentRepository = new InMemoryPaymentRepository();
        RatingRepository ratingRepository = new InMemoryRatingRepository();

        // 2. Initialize Services (Member 3)
        FareCalculatorService fareCalculatorService = new FareCalculatorService();
        MatchingService matchingService = new MatchingService(driverRepository);
        RiderService riderService = new RiderService(riderRepository, paymentRepository);
        DriverService driverService = new DriverService(driverRepository, vehicleRepository);
        TripService tripService = new TripService(tripRepository, rideRequestRepository,
                riderRepository, driverRepository, paymentRepository,
                fareCalculatorService, matchingService);
        RatingService ratingService = new RatingService(ratingRepository, tripRepository,
                riderRepository, driverRepository);

        // 3. Seed Realistic Sample Data
        seedSampleData(riderService, driverService);

        // 4. Initialize CLI Controllers (Member 1)
        Scanner scanner = new Scanner(System.in);
        RiderCli riderCli = new RiderCli(riderService, tripService, matchingService, ratingService, fareCalculatorService, scanner);
        DriverCli driverCli = new DriverCli(driverService, tripService, ratingService, scanner);
        AdminCli adminCli = new AdminCli(riderService, driverService, tripRepository, paymentRepository, rideRequestRepository, fareCalculatorService, scanner);
        MainMenuCli mainMenuCli = new MainMenuCli(riderCli, driverCli, adminCli, scanner);

        // 5. Start CLI Application
        mainMenuCli.start();
    }

    /**
     * Seeds initial test riders, drivers, vehicles, and locations so the system
     * is immediately ready to demonstrate ride matching, trip lifecycle, and surge pricing.
     */
    private static void seedSampleData(RiderService riderService, DriverService driverService) {
        // --- Sample Riders ---
        // 1. Rahul Sharma (Sufficient wallet)
        riderService.registerRider("Rahul Sharma", "9876543210", "rahul.sharma@example.com", 750.0);
        // 2. Ananya Verma (Moderate wallet)
        riderService.registerRider("Ananya Verma", "9811223344", "ananya.verma@example.com", 300.0);
        // 3. Vikram Patel (Low wallet for testing insufficient balance)
        riderService.registerRider("Vikram Patel", "9822334455", "vikram.patel@example.com", 25.0);

        // --- Sample Drivers ---
        // Driver 1: Rajesh Gowda (Sedan, close to Indiranagar (1.0, 1.0))
        Driver d1 = driverService.registerDriver("Rajesh Gowda", "9123456780", "rajesh.gowda@example.com",
                "KA032018001", new Location("Indiranagar 12th Main", 1.2, 1.3));
        driverService.approveDriver(d1.getId());
        driverService.registerVehicle(d1.getId(), "KA-03-HA-1001", "Maruti Swift Dzire", VehicleType.SEDAN);
        driverService.setDriverAvailability(d1.getId(), DriverAvailability.AVAILABLE);

        // Driver 2: Mohammed Arif (Mini, close to Indiranagar (1.5, 1.8))
        Driver d2 = driverService.registerDriver("Mohammed Arif", "9123456781", "arif.m@example.com",
                "KA042019002", new Location("Domlur Flyover", 1.5, 1.8));
        driverService.approveDriver(d2.getId());
        driverService.registerVehicle(d2.getId(), "KA-04-MB-2002", "Tata Tiago", VehicleType.MINI);
        driverService.setDriverAvailability(d2.getId(), DriverAvailability.AVAILABLE);

        // Driver 3: Suresh Kumar (Auto Rickshaw, at (1.1, 1.1))
        Driver d3 = driverService.registerDriver("Suresh Kumar", "9123456782", "suresh.k@example.com",
                "KA012017003", new Location("Indiranagar BDA Complex", 1.1, 1.1));
        driverService.approveDriver(d3.getId());
        driverService.registerVehicle(d3.getId(), "KA-01-F-3003", "Bajaj RE Compact", VehicleType.AUTO);
        driverService.setDriverAvailability(d3.getId(), DriverAvailability.AVAILABLE);

        // Driver 4: David Pinto (Bike, at (1.4, 1.2))
        Driver d4 = driverService.registerDriver("David Pinto", "9123456783", "david.p@example.com",
                "KA052020004", new Location("HAL 2nd Stage", 1.4, 1.2));
        driverService.approveDriver(d4.getId());
        driverService.registerVehicle(d4.getId(), "KA-05-BK-4004", "Hero Splendor Plus", VehicleType.BIKE);
        driverService.setDriverAvailability(d4.getId(), DriverAvailability.AVAILABLE);

        // Driver 5: Harish Nair (Prime SUV, at (2.0, 2.2))
        Driver d5 = driverService.registerDriver("Harish Nair", "9123456784", "harish.nair@example.com",
                "KA532021005", new Location("Koramangala 1st Block", 2.0, 2.2));
        driverService.approveDriver(d5.getId());
        driverService.registerVehicle(d5.getId(), "KA-53-SV-5005", "Toyota Innova Crysta", VehicleType.SUV);
        driverService.setDriverAvailability(d5.getId(), DriverAvailability.AVAILABLE);

        // Driver 6: Unapproved driver (for testing approval flow)
        Driver d6 = driverService.registerDriver("Karthik Murthy", "9123456785", "karthik.m@example.com",
                "KA022022006", new Location("Malleshwaram", 7.0, 8.0));
        driverService.registerVehicle(d6.getId(), "KA-02-X-6006", "Hyundai Creta", VehicleType.SUV);
        // Note: Kept PENDING_APPROVAL and OFFLINE
    }
}
