package dev.trulyking.avertoxtowns.model;

import java.util.Locale;
import java.util.Optional;

public enum TownRole {
    FARMER,
    FISHERMAN,
    WOODCUTTER,
    MINER;

    public static Optional<TownRole> fromInput(String input) {
        if (input == null) {
            return Optional.empty();
        }

        String normalized = input.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("FISHER")) {
            normalized = "FISHERMAN";
        }

        try {
            return Optional.of(TownRole.valueOf(normalized));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public String displayName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
