package dev.trulyking.avertoxtowns.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public final class RolePoint {
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public RolePoint(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static RolePoint fromLocation(Location location) {
        return new RolePoint(
            Objects.requireNonNull(location.getWorld()).getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }

    public String world() {
        return world;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public Location toLocation() {
        World worldObj = Bukkit.getWorld(world);
        if (worldObj == null) {
            return null;
        }
        return new Location(worldObj, x, y, z);
    }

    public String asKey() {
        return world + ":" + x + ":" + y + ":" + z;
    }
}
