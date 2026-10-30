package com.ridebooking.service;

import com.ridebooking.model.*;
import com.ridebooking.repository.DriverRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service handling driver discovery and proximity matching.
 * Enforces 5 km maximum matching radius, vehicle type filtering,
 * nearest-first sorting, and a 3-driver offer ceiling.
 */
public class MatchingService {
    public static final double MAX_MATCHING_RADIUS_KM = 5.0;
    public static final int MAX_OFFER_LIMIT = 3;

    private final DriverRepository driverRepository;

    public MatchingService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    /**
     * Finds eligible drivers within the 5 km radius matching the requested vehicle type,
     * sorted from nearest to farthest, excluding already offered drivers.
     *
     * @param pickup Location of the rider.
     * @param vehicleType Desired vehicle type.
     * @param excludedDriverIds Driver IDs who have already rejected or been offered this ride.
     * @return Top candidates (up to 3 maximum).
     */
    public List<MatchingCandidate> findEligibleDrivers(Location pickup, VehicleType vehicleType, List<String> excludedDriverIds) {
        if (pickup == null || vehicleType == null) {
            return Collections.emptyList();
        }

        List<Driver> availableDrivers = driverRepository.findAvailableDriversByVehicleType(vehicleType);
        List<MatchingCandidate> candidates = new ArrayList<>();

        for (Driver driver : availableDrivers) {
            if (excludedDriverIds != null && excludedDriverIds.contains(driver.getId())) {
                continue; // Do not assign the same driver twice rule
            }

            double distance = pickup.distanceTo(driver.getCurrentLocation());
            if (distance <= MAX_MATCHING_RADIUS_KM) {
                candidates.add(new MatchingCandidate(driver, distance));
            }
        }

        // Sort nearest driver first
        Collections.sort(candidates);

        // Offer to maximum of three drivers
        if (candidates.size() > MAX_OFFER_LIMIT) {
            return new ArrayList<>(candidates.subList(0, MAX_OFFER_LIMIT));
        }

        return candidates;
    }
}
