package dev.trulyking.avertoxtowns.model;

public enum TownType {
    CITY,
    VILLAGE;

    public String displayName() {
        return name().toLowerCase();
    }
}
