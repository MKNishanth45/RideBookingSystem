package com.ridebooking.repository;

import com.ridebooking.model.Vehicle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory storage for Vehicles using HashMap and ArrayList.
 */
public class InMemoryVehicleRepository implements VehicleRepository {
    private final Map<String, Vehicle> vehicleMap = new HashMap<>();

    @Override
    public Vehicle save(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Cannot save null vehicle.");
        }
        vehicleMap.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(vehicleMap.get(id));
    }

    @Override
    public Optional<Vehicle> findByRegistrationNumber(String regNo) {
        if (regNo == null) return Optional.empty();
        return vehicleMap.values().stream()
                .filter(v -> v.getRegistrationNumber().equalsIgnoreCase(regNo.trim()))
                .findFirst();
    }

    @Override
    public List<Vehicle> findByDriverId(String driverId) {
        if (driverId == null) return Collections.emptyList();
        return vehicleMap.values().stream()
                .filter(v -> v.getDriverId().equalsIgnoreCase(driverId.trim()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicleMap.values());
    }

    @Override
    public void update(Vehicle vehicle) {
        if (vehicle != null && vehicleMap.containsKey(vehicle.getId())) {
            vehicleMap.put(vehicle.getId(), vehicle);
        }
    }

    @Override
    public void clear() {
        vehicleMap.clear();
    }
}
