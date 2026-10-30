package com.ridebooking.repository;

import com.ridebooking.model.Rider;
import java.util.*;

/**
 * In-memory storage for Riders using HashMap for O(1) lookups and ArrayList for lists.
 */
public class InMemoryRiderRepository implements RiderRepository {
    private final Map<String, Rider> riderMap = new HashMap<>();

    @Override
    public Rider save(Rider rider) {
        if (rider == null) {
            throw new IllegalArgumentException("Cannot save null rider.");
        }
        riderMap.put(rider.getId(), rider);
        return rider;
    }

    @Override
    public Optional<Rider> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(riderMap.get(id));
    }

    @Override
    public Optional<Rider> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        return riderMap.values().stream()
                .filter(r -> r.getPhone().equals(phone.trim()))
                .findFirst();
    }

    @Override
    public List<Rider> findAll() {
        return new ArrayList<>(riderMap.values());
    }

    @Override
    public void update(Rider rider) {
        if (rider != null && riderMap.containsKey(rider.getId())) {
            riderMap.put(rider.getId(), rider);
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null) return false;
        return riderMap.remove(id) != null;
    }

    @Override
    public void clear() {
        riderMap.clear();
    }
}
