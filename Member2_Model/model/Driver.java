package com.ridebooking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a driver on the platform.
 */
public class Driver {
    private final String id;
    private String name;
    private final String phone;
    private String email;
    private final String licenseNumber;
    private DriverStatus status;
    private DriverAvailability availability;
    private Vehicle activeVehicle;
    private Location currentLocation;
    private double averageRating;
    private int totalRatingsCount;
    private double sumOfRatings;
    private int completedTripsCount;
    private int dailyCancellationCount;
    private LocalDate lastCancellationDate;
    private double totalEarnings;
    private final LocalDateTime createdAt;

    public Driver(String id, String name, String phone, String email, String licenseNumber, Location initialLocation) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name cannot be null or empty.");
        }
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new IllegalArgumentException("Valid 10-digit phone number is required.");
        }
        if (email == null || !email.trim().contains("@")) {
            throw new IllegalArgumentException("Valid email address is required.");
        }
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver license number cannot be null or empty.");
        }
        if (initialLocation == null) {
            throw new IllegalArgumentException("Initial driver location cannot be null.");
        }

        this.id = id.trim();
        this.name = name.trim();
        this.phone = phone.trim();
        this.email = email.trim();
        this.licenseNumber = licenseNumber.trim().toUpperCase();
        this.status = DriverStatus.PENDING_APPROVAL;
        this.availability = DriverAvailability.OFFLINE;
        this.activeVehicle = null;
        this.currentLocation = initialLocation;
        this.averageRating = 5.0;
        this.totalRatingsCount = 0;
        this.sumOfRatings = 0.0;
        this.completedTripsCount = 0;
        this.dailyCancellationCount = 0;
        this.lastCancellationDate = LocalDate.now();
        this.totalEarnings = 0.0;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email.trim();
        }
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
        if (status == DriverStatus.SUSPENDED || status == DriverStatus.REJECTED) {
            this.availability = DriverAvailability.OFFLINE;
        }
    }

    public DriverAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(DriverAvailability availability) {
        this.availability = availability;
    }

    public Vehicle getActiveVehicle() {
        return activeVehicle;
    }

    public void setActiveVehicle(Vehicle activeVehicle) {
        this.activeVehicle = activeVehicle;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        if (currentLocation != null) {
            this.currentLocation = currentLocation;
        }
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getTotalRatingsCount() {
        return totalRatingsCount;
    }

    public int getCompletedTripsCount() {
        return completedTripsCount;
    }

    public int getDailyCancellationCount() {
        resetDailyCancellationIfNewDay();
        return dailyCancellationCount;
    }

    public LocalDate getLastCancellationDate() {
        return lastCancellationDate;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void addEarnings(double amount) {
        if (amount > 0) {
            this.totalEarnings = Math.round((this.totalEarnings + amount) * 100.0) / 100.0;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void resetDailyCancellationIfNewDay() {
        LocalDate today = LocalDate.now();
        if (lastCancellationDate == null || !lastCancellationDate.isEqual(today)) {
            this.dailyCancellationCount = 0;
            this.lastCancellationDate = today;
        }
    }

    public void incrementDailyCancellation() {
        resetDailyCancellationIfNewDay();
        this.dailyCancellationCount++;
    }

    public void incrementCompletedTrips() {
        this.completedTripsCount++;
        evaluatePerformanceCompliance();
    }

    /**
     * Updates rating and checks for automatic suspension.
     * Rule: Suspend a driver whose average rating is below 3.0 after at least 20 rides.
     */
    public void updateRating(int newScore) {
        if (newScore < 1 || newScore > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5.");
        }
        this.totalRatingsCount++;
        this.sumOfRatings += newScore;
        this.averageRating = Math.round((this.sumOfRatings / this.totalRatingsCount) * 10.0) / 10.0;

        evaluatePerformanceCompliance();
    }

    private void evaluatePerformanceCompliance() {
        if (this.completedTripsCount >= 20 && this.averageRating < 3.0) {
            this.status = DriverStatus.SUSPENDED;
            this.availability = DriverAvailability.OFFLINE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Driver other)) return false;
        return id.equalsIgnoreCase(other.id) || phone.equals(other.phone) || licenseNumber.equalsIgnoreCase(other.licenseNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase(), phone, licenseNumber.toUpperCase());
    }

    @Override
    public String toString() {
        String veh = activeVehicle != null ? activeVehicle.getMakeModel() + " (" + activeVehicle.getVehicleType().getDisplayName() + ")" : "None";
        return String.format("[%s] %s | Phone: %s | Status: %s | Availability: %s | Vehicle: %s | Rating: %.1f★ (%d rides) | Earnings: Rs. %.2f",
                id, name, phone, status, availability, veh, averageRating, completedTripsCount, totalEarnings);
    }
}
