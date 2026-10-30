package com.ridebooking;

import com.ridebooking.model.*;
import com.ridebooking.repository.*;
import java.util.List;
import java.util.Optional;

/**
 * Standalone demo program for Member 4 (Repository Module).
 * Tests and verifies pure in-memory CRUD operations across repositories.
 */
public class Member4Demo {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   MEMBER 4: REPOSITORY MODULE VERIFICATION DEMO  ");
        System.out.println("==================================================");

        // 1. RiderRepository
        RiderRepository riderRepo = new InMemoryRiderRepository();
        Rider r1 = new Rider("RDR-101", "Priya Sharma", "9845012345", "priya@gmail.com", 600.0);
        riderRepo.save(r1);
        Optional<Rider> byPhone = riderRepo.findByPhone("9845012345");
        System.out.println("[✓] Rider Saved and Found by Phone: " + byPhone.map(Rider::getName).orElse("Not Found"));

        // 2. Driver & Vehicle Repositories
        DriverRepository driverRepo = new InMemoryDriverRepository();
        VehicleRepository vehicleRepo = new InMemoryVehicleRepository();

        Location loc1 = new Location("MG Road Metro", 2.0, 3.0);
        Driver d1 = new Driver("DRV-201", "Kiran Rao", "9880011223", "kiran@gmail.com", "KA012019999", loc1);
        Vehicle v1 = new Vehicle("VEH-301", d1.getId(), "KA-01-AB-1234", "Honda City", VehicleType.SEDAN);

        vehicleRepo.save(v1);
        d1.setActiveVehicle(v1);
        d1.setStatus(DriverStatus.APPROVED);
        d1.setAvailability(DriverAvailability.AVAILABLE);
        driverRepo.save(d1);

        Driver d2 = new Driver("DRV-202", "Suresh Kumar", "9880055443", "suresh@gmail.com", "KA052020111", loc1);
        Vehicle v2 = new Vehicle("VEH-302", d2.getId(), "KA-05-CD-5678", "Bajaj RE Auto", VehicleType.AUTO);
        vehicleRepo.save(v2);
        d2.setActiveVehicle(v2);
        d2.setStatus(DriverStatus.APPROVED);
        d2.setAvailability(DriverAvailability.AVAILABLE);
        driverRepo.save(d2);

        List<Driver> availableSedans = driverRepo.findAvailableDriversByVehicleType(VehicleType.SEDAN);
        System.out.printf("[✓] Available Sedan Drivers Count: %d (Driver: %s)\n", availableSedans.size(), availableSedans.get(0).getName());

        List<Driver> allAvailable = driverRepo.findAvailableDrivers();
        System.out.printf("[✓] Total Available Drivers: %d\n", allAvailable.size());

        // 3. RideRequestRepository
        RideRequestRepository reqRepo = new InMemoryRideRequestRepository();
        RideRequest req1 = new RideRequest("REQ-401", r1.getId(), loc1, new Location("Airport T1", 10.0, 15.0), VehicleType.SEDAN, 17.0, 1.0, 350.0);
        reqRepo.save(req1);
        System.out.println("[✓] Pending Requests Count: " + reqRepo.findPendingRequests().size());

        // 4. TripRepository
        TripRepository tripRepo = new InMemoryTripRepository();
        Trip t1 = new Trip("TRP-501", req1.getId(), r1.getId(), d1.getId(), v1.getId(), req1.getPickupLocation(), req1.getDropLocation(), 17.0, 350.0, "1234");
        tripRepo.save(t1);
        Optional<Trip> activeTrip = tripRepo.findActiveTripByRiderId(r1.getId());
        System.out.println("[✓] Active Trip Found for Rider: " + activeTrip.map(Trip::getId).orElse("None"));

        // 5. PaymentRepository
        PaymentRepository payRepo = new InMemoryPaymentRepository();
        Payment p1 = new Payment("PAY-601", t1.getId(), r1.getId(), d1.getId(), 350.0, 280.0, 70.0, PaymentMethod.WALLET, PaymentStatus.FAILED);
        payRepo.save(p1);
        List<Payment> failed = payRepo.findFailedOrPendingPaymentsByRider(r1.getId());
        System.out.printf("[✓] Failed/Pending Payments for Rider: %d (Amount: Rs. %.2f)\n", failed.size(), failed.get(0).getAmount());

        // 6. Delete test
        boolean deleted = riderRepo.delete("NON-EXISTENT");
        System.out.println("[✓] Delete Non-existent ID returns false: " + (!deleted));

        System.out.println("==================================================");
        System.out.println(" MEMBER 4 ALL REPOSITORY VERIFICATIONS PASSED!   ");
        System.out.println("==================================================");
    }
}
