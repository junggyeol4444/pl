package com.junggyeol.secretdimension;

import java.util.*;

public class PlayerDimensionData {
    private final UUID uuid;
    private final Set<String> discoveredDimensions = new HashSet<>();

    public PlayerDimensionData(UUID uuid) { this.uuid = uuid; }

    public UUID getUuid() { return uuid; }
    public Set<String> getDiscoveredDimensions() { return discoveredDimensions; }
    public boolean hasDiscovered(String dimensionId) { return discoveredDimensions.contains(dimensionId); }
    public void discover(String dimensionId) { discoveredDimensions.add(dimensionId); }
}
