package com.ridebooking.service;

import com.ridebooking.exception.*;
import com.ridebooking.model.*;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.VehicleRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service orchestrating Driver registration, vehicle association, admin approval,
 * availability toggling, location updates, cancellation limits, and suspension.
 */
public class DriverService {
    public static final int MAX_DAILY_CANCELLATIONS = 3;
    public static final int SUSPENSION_TRIP_THRESHOLD = 20;
    public static final double MIN_ACCEPTABLE_RATING = 3.0;

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final AtomicInteger driverIdSeq = new AtomicInteger(200);
    private final AtomicInteger vehicleIdSeq = new AtomicInteger(300);

    public DriverService(DriverRepository driverRepository, VehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Registers a driver with unique phone and license numbers.
     */
    public Driver registerDriver(String name, String phone, String email, String licenseNumber, Location initialLocation) {
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new InvalidInputException("Phone number must be exactly 10 digits.");
        }
        if (licenseNumber == null || licenseNumber.trim().length() < 5) {
            throw new InvalidInputException("A valid driving license number is required.");
        }

        if (driverRepository.findByPhone(phone.trim()).isPresent()) {
            throw new InvalidInputException("A driver with phone number " + phone.trim() + " already exists.");
        }
        if (driverRepository.findByLicenseNumber(licenseNumber.trim()).isPresent()) {
            throw new InvalidInputException("A driver with license number " + licenseNumber.trim().toUpperCase() + " already exists.");
        }

        String driverId = "DRV-" + driverIdSeq.incrementAndGet();
        Driver driver = new Driver(driverId, name, phone, email, licenseNumber, initialLocation);
        return driverRepository.save(driver);
    }

    /**
     * Registers a vehicle and assigns it as the driver's active vehicle.
     * Enforces unique vehicle registration number across the platform.
     */
    public Vehicle registerVehicle(String driverId, String regNumber, String makeModel, VehicleType vehicleType) {
        Driver driver = getDriverById(driverId);

        if (regNumber == null || regNumber.trim().isEmpty()) {
            throw new InvalidInputException("Vehicle registration number cannot be blank.");
        }

        if (vehicleRepository.findByRegistrationNumber(regNumber.trim()).isPresent()) {
            throw new InvalidInputException("Vehicle with registration number " + regNumber.trim().toUpperCase() + " is already registered.");
        }

        String vehicleId = "VEH-" + vehicleIdSeq.incrementAndGet();
        Vehicle vehicle = new Vehicle(vehicleId, driver.getId(), regNumber, makeModel, vehicleType);
        vehicleRepository.save(vehicle);

        // Set as active vehicle for driver
        driver.setActiveVehicle(vehicle);
        driverRepository.update(driver);
        return vehicle;
    }

    public Driver getDriverById(String id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with ID " + id + " not found."));
    }

    public Driver getDriverByPhone(String phone) {
        return driverRepository.findByPhone(phone)
                .orElseThrow(() -> new EntityNotFoundException("No driver registered with phone " + phone + "."));
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    /**
     * Admin action to approve a driver application.
     */
    public void approveDriver(String driverId) {
        Driver driver = getDriverById(driverId);
        driver.setStatus(DriverStatus.APPROVED);
        driverRepository.update(driver);
    }

    /**
     * Admin action to reject a driver application.
     */
    public void rejectDriver(String driverId) {
        Driver driver = getDriverById(driverId);
        driver.setStatus(DriverStatus.REJECTED);
        driver.setAvailability(DriverAvailability.OFFLINE);
        driverRepository.update(driver);
    }

    /**
     * Toggles driver availability state.
     * Rules:
     * - Only APPROVED drivers can become AVAILABLE.
     * - Driver must have an active vehicle.
     * - Suspended or rejected drivers cannot go online.
     */
    public void setDriverAvailability(String driverId, DriverAvailability availability) {
        Driver driver = getDriverById(driverId);

        if (availability == DriverAvailability.AVAILABLE) {
            if (driver.getStatus() != DriverStatus.APPROVED) {
                throw new UnauthorizedException("Only approved drivers can become Available. Current status: " + driver.getStatus());
            }
            if (driver.getActiveVehicle() == null) {
                throw new InvalidInputException("Driver must have an active vehicle before becoming Available.");
            }
        }

        driver.setAvailability(availability);
        driverRepository.update(driver);
    }

    /**
     * Updates driver's current coordinates.
     */
    public void updateDriverLocation(String driverId, Location newLocation) {
        Driver driver = getDriverById(driverId);
        driver.setCurrentLocation(newLocation);
        driverRepository.update(driver);
    }

    /**
     * Records a driver cancellation and enforces the daily cancellation limit.
     * Rule: Driver cancellation limit: three rides per day.
     */
    public void recordDriverCancellation(String driverId) {
        Driver driver = getDriverById(driverId);
        driver.resetDailyCancellationIfNewDay();

        if (driver.getDailyCancellationCount() >= MAX_DAILY_CANCELLATIONS) {
            throw new TripLifecycleException("Cancellation limit exceeded! Drivers are permitted a maximum of "
                    + MAX_DAILY_CANCELLATIONS + " cancellations per day.");
        }

        driver.incrementDailyCancellation();
        driverRepository.update(driver);
    }

    /**
     * Checks driver performance metrics and triggers suspension if conditions are met.
     * Rule: Suspend a driver whose average rating is below 3.0 after at least 20 rides.
     */
    public void checkDriverSuspension(String driverId) {
        Driver driver = getDriverById(driverId);
        if (driver.getCompletedTripsCount() >= SUSPENSION_TRIP_THRESHOLD &&
            driver.getAverageRating() < MIN_ACCEPTABLE_RATING) {
            driver.setStatus(DriverStatus.SUSPENDED);
            driver.setAvailability(DriverAvailability.OFFLINE);
            driverRepository.update(driver);
        }
    }
}
