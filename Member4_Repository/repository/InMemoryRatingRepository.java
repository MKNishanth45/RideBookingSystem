package com.ridebooking.repository;

import com.ridebooking.model.Rating;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory storage for Ratings using HashMap and ArrayList.
 */
public class InMemoryRatingRepository implements RatingRepository {
    private final Map<String, Rating> ratingMap = new HashMap<>();

    @Override
    public Rating save(Rating rating) {
        if (rating == null) {
            throw new IllegalArgumentException("Cannot save null rating.");
        }
        ratingMap.put(rating.getId(), rating);
        return rating;
    }

    @Override
    public Optional<Rating> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(ratingMap.get(id));
    }

    @Override
    public List<Rating> findByTripId(String tripId) {
        if (tripId == null) return Collections.emptyList();
        return ratingMap.values().stream()
                .filter(r -> r.getTripId().equalsIgnoreCase(tripId.trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Optional<Rating> findByTripAndRater(String tripId, String fromUserId) {
        if (tripId == null || fromUserId == null) return Optional.empty();
        return ratingMap.values().stream()
                .filter(r -> r.getTripId().equalsIgnoreCase(tripId.trim()) &&
                             r.getFromUserId().equalsIgnoreCase(fromUserId.trim()))
                .findFirst();
    }

    @Override
    public List<Rating> findByToUserId(String toUserId) {
        if (toUserId == null) return Collections.emptyList();
        return ratingMap.values().stream()
                .filter(r -> r.getToUserId().equalsIgnoreCase(toUserId.trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Rating> findAll() {
        return new ArrayList<>(ratingMap.values());
    }

    @Override
    public void clear() {
        ratingMap.clear();
    }
}
