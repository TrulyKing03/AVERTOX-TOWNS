package dev.trulyking.avertoxtowns.model;

import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

public final class CreateSession {
    private final UUID playerId;
    private final TownType type;
    private final String name;
    private Location firstCorner;
    private Location secondCorner;

    public CreateSession(UUID playerId, TownType type, String name) {
        this.playerId = playerId;
        this.type = type;
        this.name = name;
    }

    public UUID playerId() {
        return playerId;
    }

    public TownType type() {
        return type;
    }

    public String name() {
        return name;
    }

    public Location firstCorner() {
        return firstCorner;
    }

    public Location secondCorner() {
        return secondCorner;
    }

    public void setFirstCorner(Location firstCorner) {
        this.firstCorner = Objects.requireNonNull(firstCorner).clone();
    }

    public void setSecondCorner(Location secondCorner) {
        this.secondCorner = Objects.requireNonNull(secondCorner).clone();
    }

    public boolean isComplete() {
        return firstCorner != null && secondCorner != null;
    }
}
