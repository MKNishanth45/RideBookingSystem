package com.ridebooking.repository;

import com.ridebooking.model.RideRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory storage for RideRequests using HashMap and ArrayList.
 */
public class InMemoryRideRequestRepository implements RideRequestRepository {
    private final Map<String, RideRequest> requestMap = new HashMap<>();

    @Override
    public RideRequest save(RideRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cannot save null ride request.");
        }
        requestMap.put(request.getId(), request);
        return request;
    }

    @Override
    public Optional<RideRequest> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(requestMap.get(id));
    }

    @Override
    public List<RideRequest> findPendingRequests() {
        return requestMap.values().stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()) && !r.isExpired())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<RideRequest> findByRiderId(String riderId) {
        if (riderId == null) return Collections.emptyList();
        return requestMap.values().stream()
                .filter(r -> r.getRiderId().equalsIgnoreCase(riderId.trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<RideRequest> findAll() {
        return new ArrayList<>(requestMap.values());
    }

    @Override
    public void update(RideRequest request) {
        if (request != null && requestMap.containsKey(request.getId())) {
            requestMap.put(request.getId(), request);
        }
    }

    @Override
    public void clear() {
        requestMap.clear();
    }
}
