package com.ridebooking.repository;

import com.ridebooking.model.Trip;
import com.ridebooking.model.TripStatus;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory storage for Trips using HashMap and ArrayList.
 */
public class InMemoryTripRepository implements TripRepository {
    private final Map<String, Trip> tripMap = new HashMap<>();

    @Override
    public Trip save(Trip trip) {
        if (trip == null) {
            throw new IllegalArgumentException("Cannot save null trip.");
        }
        tripMap.put(trip.getId(), trip);
        return trip;
    }

    @Override
    public Optional<Trip> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(tripMap.get(id));
    }

    @Override
    public List<Trip> findByRiderId(String riderId) {
        if (riderId == null) return Collections.emptyList();
        return tripMap.values().stream()
                .filter(t -> t.getRiderId().equalsIgnoreCase(riderId.trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Trip> findByDriverId(String driverId) {
        if (driverId == null) return Collections.emptyList();
        return tripMap.values().stream()
                .filter(t -> t.getDriverId().equalsIgnoreCase(driverId.trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Optional<Trip> findActiveTripByRiderId(String riderId) {
        if (riderId == null) return Optional.empty();
        return tripMap.values().stream()
                .filter(t -> t.getRiderId().equalsIgnoreCase(riderId.trim()) &&
                             t.getStatus() != TripStatus.COMPLETED &&
                             t.getStatus() != TripStatus.CANCELLED)
                .findFirst();
    }

    @Override
    public Optional<Trip> findActiveTripByDriverId(String driverId) {
        if (driverId == null) return Optional.empty();
        return tripMap.values().stream()
                .filter(t -> t.getDriverId().equalsIgnoreCase(driverId.trim()) &&
                             t.getStatus() != TripStatus.COMPLETED &&
                             t.getStatus() != TripStatus.CANCELLED)
                .findFirst();
    }

    @Override
    public List<Trip> findAll() {
        return new ArrayList<>(tripMap.values());
    }

    @Override
    public void update(Trip trip) {
        if (trip != null && tripMap.containsKey(trip.getId())) {
            tripMap.put(trip.getId(), trip);
        }
    }

    @Override
    public void clear() {
        tripMap.clear();
    }
}
