package dev.trulyking.avertoxtowns.model;

import org.bukkit.Location;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class Town {
    private final int id;
    private final TownType type;
    private final String name;
    private final UUID owner;
    private final String world;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;
    private TownStatus status;
    private double revenueBalance;
    private final Map<TownRole, RolePoint> rolePoints;

    public Town(
        int id,
        TownType type,
        String name,
        UUID owner,
        String world,
        int minX,
        int maxX,
        int minY,
        int maxY,
        int minZ,
        int maxZ,
        TownStatus status,
        double revenueBalance
    ) {
        this.id = id;
        this.type = Objects.requireNonNull(type);
        this.name = Objects.requireNonNull(name);
        this.owner = Objects.requireNonNull(owner);
        this.world = Objects.requireNonNull(world);
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.status = Objects.requireNonNull(status);
        this.revenueBalance = revenueBalance;
        this.rolePoints = new EnumMap<>(TownRole.class);
    }

    public int id() {
        return id;
    }

    public TownType type() {
        return type;
    }

    public String name() {
        return name;
    }

    public UUID owner() {
        return owner;
    }

    public String world() {
        return world;
    }

    public int minX() {
        return minX;
    }

    public int maxX() {
        return maxX;
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxZ() {
        return maxZ;
    }

    public TownStatus status() {
        return status;
    }

    public void setStatus(TownStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public double revenueBalance() {
        return revenueBalance;
    }

    public void addRevenue(double amount) {
        this.revenueBalance += amount;
    }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().getName().equals(world)) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }

    public void setRolePoint(TownRole role, RolePoint point) {
        rolePoints.put(Objects.requireNonNull(role), Objects.requireNonNull(point));
    }

    public RolePoint getRolePoint(TownRole role) {
        return rolePoints.get(role);
    }

    public Map<TownRole, RolePoint> rolePoints() {
        return Collections.unmodifiableMap(rolePoints);
    }

    public void setRolePoints(Map<TownRole, RolePoint> points) {
        rolePoints.clear();
        rolePoints.putAll(points);
    }
}
