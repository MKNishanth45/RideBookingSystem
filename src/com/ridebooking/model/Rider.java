package com.ridebooking.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a registered customer/rider on the platform.
 */
public class Rider {
    private final String id;
    private String name;
    private final String phone;
    private String email;
    private double walletBalance;
    private double averageRating;
    private int totalRatingsCount;
    private double sumOfRatings;
    private int completedTripsCount;
    private boolean flaggedForReview;
    private final LocalDateTime createdAt;

    public Rider(String id, String name, String phone, String email, double initialWalletBalance) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider name cannot be null or empty.");
        }
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new IllegalArgumentException("Valid 10-digit phone number is required.");
        }
        if (email == null || !email.trim().contains("@")) {
            throw new IllegalArgumentException("Valid email address is required.");
        }
        if (initialWalletBalance < 0) {
            throw new IllegalArgumentException("Initial wallet balance cannot be negative.");
        }

        this.id = id.trim();
        this.name = name.trim();
        this.phone = phone.trim();
        this.email = email.trim();
        this.walletBalance = Math.round(initialWalletBalance * 100.0) / 100.0;
        this.averageRating = 5.0; // Starts with standard baseline 5.0
        this.totalRatingsCount = 0;
        this.sumOfRatings = 0.0;
        this.completedTripsCount = 0;
        this.flaggedForReview = false;
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

    public double getWalletBalance() {
        return walletBalance;
    }

    public void creditWallet(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be greater than zero.");
        }
        this.walletBalance = Math.round((this.walletBalance + amount) * 100.0) / 100.0;
    }

    public boolean debitWallet(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be greater than zero.");
        }
        if (this.walletBalance >= amount) {
            this.walletBalance = Math.round((this.walletBalance - amount) * 100.0) / 100.0;
            return true;
        }
        return false;
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

    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }

    public void setFlaggedForReview(boolean flaggedForReview) {
        this.flaggedForReview = flaggedForReview;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Updates the running average rating and evaluates if the rider requires admin review.
     * Rule: Flag riders below 3.0 after at least 10 completed trips for admin review.
     */
    public void updateRating(int newScore) {
        if (newScore < 1 || newScore > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5.");
        }
        this.totalRatingsCount++;
        this.sumOfRatings += newScore;
        this.averageRating = Math.round((this.sumOfRatings / this.totalRatingsCount) * 10.0) / 10.0;

        checkReviewStatus();
    }

    public void incrementCompletedTrips() {
        this.completedTripsCount++;
        checkReviewStatus();
    }

    private void checkReviewStatus() {
        if (this.completedTripsCount >= 10 && this.averageRating < 3.0) {
            this.flaggedForReview = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rider other)) return false;
        return id.equalsIgnoreCase(other.id) || phone.equals(other.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase(), phone);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Phone: %s | Wallet: Rs. %.2f | Rating: %.1f★ (%d trips) | Flagged: %s",
                id, name, phone, walletBalance, averageRating, completedTripsCount, flaggedForReview ? "YES" : "No");
    }
}
