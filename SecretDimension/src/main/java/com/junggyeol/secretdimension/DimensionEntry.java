package com.junggyeol.secretdimension;

import org.bukkit.Location;

public class DimensionEntry {
    private final String dimensionId;
    private final Location returnLocation;
    private final long enteredAt;

    public DimensionEntry(String dimensionId, Location returnLocation, long enteredAt) {
        this.dimensionId = dimensionId;
        this.returnLocation = returnLocation;
        this.enteredAt = enteredAt;
    }

    public String getDimensionId() { return dimensionId; }
    public Location getReturnLocation() { return returnLocation; }
    public long getEnteredAt() { return enteredAt; }
}
