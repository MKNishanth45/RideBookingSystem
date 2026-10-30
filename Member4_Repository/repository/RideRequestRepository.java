package com.ridebooking.repository;

import com.ridebooking.model.RideRequest;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RideRequest entity persistence.
 */
public interface RideRequestRepository {
    RideRequest save(RideRequest request);
    Optional<RideRequest> findById(String id);
    List<RideRequest> findPendingRequests();
    List<RideRequest> findByRiderId(String riderId);
    List<RideRequest> findAll();
    void update(RideRequest request);
    void clear();
}
