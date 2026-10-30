package com.ridebooking.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents feedback and score given by a rider to a driver, or vice versa, for a completed trip.
 */
public class Rating {
    private final String id;
    private final String tripId;
    private final String fromUserId;
    private final String toUserId;
    private final boolean isRiderRatingDriver;
    private final int score; // 1 to 5
    private final String feedback;
    private final LocalDateTime timestamp;

    public Rating(String id, String tripId, String fromUserId, String toUserId,
                  boolean isRiderRatingDriver, int score, String feedback) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Rating ID cannot be null or empty.");
        }
        if (tripId == null || fromUserId == null || toUserId == null) {
            throw new IllegalArgumentException("Trip and user references cannot be null.");
        }
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating score must be an integer between 1 and 5.");
        }

        this.id = id.trim();
        this.tripId = tripId.trim();
        this.fromUserId = fromUserId.trim();
        this.toUserId = toUserId.trim();
        this.isRiderRatingDriver = isRiderRatingDriver;
        this.score = score;
        this.feedback = feedback != null ? feedback.trim() : "";
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getTripId() {
        return tripId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public boolean isRiderRatingDriver() {
        return isRiderRatingDriver;
    }

    public int getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating other)) return false;
        return id.equalsIgnoreCase(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("[%s] Trip: %s | %s -> %s: %d★ (%s)",
                id, tripId, fromUserId, toUserId, score, feedback.isEmpty() ? "No comments" : feedback);
    }
}
