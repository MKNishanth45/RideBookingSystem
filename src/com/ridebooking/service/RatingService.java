package com.ridebooking.service;

import com.ridebooking.exception.*;
import com.ridebooking.model.*;
import com.ridebooking.repository.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service managing mutual feedback and rating calculations.
 * Enforces 1-5 score boundaries, 24-hour review window, single rating per participant,
 * automatic driver suspension, and rider admin review flagging.
 */
public class RatingService {
    public static final long RATING_VALIDITY_HOURS = 24;

    private final RatingRepository ratingRepository;
    private final TripRepository tripRepository;
    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;
    private final AtomicInteger ratingIdSeq = new AtomicInteger(700);

    public RatingService(RatingRepository ratingRepository, TripRepository tripRepository,
                         RiderRepository riderRepository, DriverRepository driverRepository) {
        this.ratingRepository = ratingRepository;
        this.tripRepository = tripRepository;
        this.riderRepository = riderRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * Submits a rating for a completed trip.
     *
     * @param tripId The completed trip ID.
     * @param fromUserId ID of the user giving rating (rider or driver).
     * @param score Score between 1 and 5.
     * @param feedback Text feedback.
     * @return Saved Rating entity.
     */
    public Rating submitRating(String tripId, String fromUserId, int score, String feedback) {
        if (score < 1 || score > 5) {
            throw new InvalidInputException("Rating score must be an integer between 1 and 5.");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip with ID " + tripId + " not found."));

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new TripLifecycleException("Ratings can only be submitted for completed trips. Current status: " + trip.getStatus());
        }

        // Rule: Ratings allowed within 24 hours of trip completion
        if (trip.getEndTime() != null) {
            Duration elapsed = Duration.between(trip.getEndTime(), LocalDateTime.now());
            if (elapsed.toHours() > RATING_VALIDITY_HOURS) {
                throw new TripLifecycleException("Rating window expired. Ratings must be submitted within 24 hours of trip completion.");
            }
        }

        // Determine if rider or driver is rating
        boolean isRider = trip.getRiderId().equalsIgnoreCase(fromUserId);
        boolean isDriver = trip.getDriverId().equalsIgnoreCase(fromUserId);

        if (!isRider && !isDriver) {
            throw new UnauthorizedException("You were not a participant in trip " + tripId + ".");
        }

        // Rule: One rating per person per trip
        if (ratingRepository.findByTripAndRater(tripId, fromUserId).isPresent()) {
            throw new InvalidInputException("You have already submitted a rating for this trip.");
        }

        String toUserId = isRider ? trip.getDriverId() : trip.getRiderId();
        String ratingId = "RAT-" + ratingIdSeq.incrementAndGet();
        Rating rating = new Rating(ratingId, tripId, fromUserId, toUserId, isRider, score, feedback);

        // Update recipient rating and check compliance
        if (isRider) {
            // Rider is rating Driver
            Driver driver = driverRepository.findById(toUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + toUserId));
            driver.updateRating(score);
            driverRepository.update(driver);
        } else {
            // Driver is rating Rider
            Rider rider = riderRepository.findById(toUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Rider not found: " + toUserId));
            rider.updateRating(score);
            riderRepository.update(rider);
        }

        return ratingRepository.save(rating);
    }

    public List<Rating> getRatingsForTrip(String tripId) {
        return ratingRepository.findByTripId(tripId);
    }

    public List<Rating> getRatingsReceivedByUser(String userId) {
        return ratingRepository.findByToUserId(userId);
    }
}
