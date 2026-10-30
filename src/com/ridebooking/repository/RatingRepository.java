package com.ridebooking.repository;

import com.ridebooking.model.Rating;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Rating entity persistence.
 */
public interface RatingRepository {
    Rating save(Rating rating);
    Optional<Rating> findById(String id);
    List<Rating> findByTripId(String tripId);
    Optional<Rating> findByTripAndRater(String tripId, String fromUserId);
    List<Rating> findByToUserId(String toUserId);
    List<Rating> findAll();
    void clear();
}
