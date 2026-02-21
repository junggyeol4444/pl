package com.junggyeol.supplybox;

import org.bukkit.Location;

public class SupplyBox {
    private final Location location;
    private final SupplyBoxGrade grade;
    private final long spawnTime;
    private boolean opened = false;

    public SupplyBox(Location location, SupplyBoxGrade grade, long spawnTime) {
        this.location = location;
        this.grade = grade;
        this.spawnTime = spawnTime;
    }

    public Location getLocation() { return location; }
    public SupplyBoxGrade getGrade() { return grade; }
    public long getSpawnTime() { return spawnTime; }
    public boolean isOpened() { return opened; }
    public void setOpened(boolean opened) { this.opened = opened; }
}
