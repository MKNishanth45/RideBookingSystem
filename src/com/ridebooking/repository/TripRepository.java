package com.ridebooking.repository;

import com.ridebooking.model.Trip;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Trip entity persistence.
 */
public interface TripRepository {
    Trip save(Trip trip);
    Optional<Trip> findById(String id);
    List<Trip> findByRiderId(String riderId);
    List<Trip> findByDriverId(String driverId);
    Optional<Trip> findActiveTripByRiderId(String riderId);
    Optional<Trip> findActiveTripByDriverId(String driverId);
    List<Trip> findAll();
    void update(Trip trip);
    void clear();
}
