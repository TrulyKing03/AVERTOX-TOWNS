package dev.trulyking.avertoxtowns.service;

import dev.trulyking.avertoxtowns.model.CreateSession;
import dev.trulyking.avertoxtowns.model.RoleContext;
import dev.trulyking.avertoxtowns.model.RolePoint;
import dev.trulyking.avertoxtowns.model.Town;
import dev.trulyking.avertoxtowns.model.TownRole;
import dev.trulyking.avertoxtowns.model.TownStatus;
import dev.trulyking.avertoxtowns.model.TownType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TownManager {
    private final JavaPlugin plugin;
    private final TownDataStore dataStore;
    private final EconomyService economyService;
    private final Map<Integer, Town> towns = new HashMap<>();
    private final Map<UUID, CreateSession> sessions = new HashMap<>();
    private int nextId = 1;

    public TownManager(JavaPlugin plugin, TownDataStore dataStore, EconomyService economyService) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        this.economyService = economyService;
    }

    public void load() {
        TownDataStore.LoadedData loadedData = dataStore.load();
        towns.clear();
        for (Town town : loadedData.towns()) {
            towns.put(town.id(), town);
        }
        nextId = loadedData.nextId();
    }

    public void save() {
        dataStore.save(towns.values(), nextId);
    }

    public Optional<CreateSession> getSession(UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public CreateSession startSession(UUID playerId, TownType type, String name) {
        CreateSession session = new CreateSession(playerId, type, name);
        sessions.put(playerId, session);
        return session;
    }

    public boolean isTownNameTaken(String name) {
        String normalized = normalizeName(name);
        if (normalized.isEmpty()) {
            return false;
        }

        for (Town town : towns.values()) {
            if (town.status() == TownStatus.REJECTED) {
                continue;
            }
            if (normalizeName(town.name()).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNameUnavailable(String name, UUID excludedPlayerId) {
        String normalized = normalizeName(name);
        if (normalized.isEmpty()) {
            return false;
        }

        if (isTownNameTaken(normalized)) {
            return true;
        }

        for (CreateSession session : sessions.values()) {
            if (excludedPlayerId != null && session.playerId().equals(excludedPlayerId)) {
                continue;
            }
            if (normalizeName(session.name()).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public void clearSession(UUID playerId) {
        sessions.remove(playerId);
    }

    public boolean setCorner(Player player, boolean first, Location location) {
        CreateSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return false;
        }

        if (first) {
            session.setFirstCorner(location);
        } else {
            session.setSecondCorner(location);
        }
        return true;
    }

    public Town finishSession(Player player) {
        UUID uuid = player.getUniqueId();
        CreateSession session = sessions.get(uuid);
        if (session == null || !session.isComplete()) {
            return null;
        }

        Location first = session.firstCorner();
        Location second = session.secondCorner();
        if (first == null || second == null || first.getWorld() == null || second.getWorld() == null) {
            return null;
        }
        if (!first.getWorld().getName().equals(second.getWorld().getName())) {
            return null;
        }

        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

        Town town = new Town(
            nextId++,
            session.type(),
            session.name(),
            uuid,
            first.getWorld().getName(),
            minX,
            maxX,
            minY,
            maxY,
            minZ,
            maxZ,
            TownStatus.PENDING,
            0D
        );

        towns.put(town.id(), town);
        sessions.remove(uuid);
        save();
        return town;
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public Town getTown(int id) {
        return towns.get(id);
    }

    public List<Town> getPendingTowns() {
        List<Town> pending = new ArrayList<>();
        for (Town town : towns.values()) {
            if (town.status() == TownStatus.PENDING) {
                pending.add(town);
            }
        }
        pending.sort(Comparator.comparingInt(Town::id));
        return pending;
    }

    public Collection<Town> getTowns() {
        return towns.values();
    }

    public Town getApprovedTownAt(Location location) {
        for (Town town : towns.values()) {
            if (town.status() == TownStatus.APPROVED && town.contains(location)) {
                return town;
            }
        }
        return null;
    }

    public Town getOwnedApprovedTownAt(UUID owner, Location location) {
        for (Town town : towns.values()) {
            if (town.status() == TownStatus.APPROVED && town.owner().equals(owner) && town.contains(location)) {
                return town;
            }
        }
        return null;
    }

    public RoleContext getRoleContextAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        String key = location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
        for (Town town : towns.values()) {
            if (town.status() != TownStatus.APPROVED) {
                continue;
            }
            for (Map.Entry<TownRole, RolePoint> entry : town.rolePoints().entrySet()) {
                if (entry.getValue().asKey().equals(key)) {
                    return new RoleContext(town, entry.getKey());
                }
            }
        }
        return null;
    }

    public boolean approveTown(int id) {
        Town town = towns.get(id);
        if (town == null || town.status() != TownStatus.PENDING) {
            return false;
        }
        town.setStatus(TownStatus.APPROVED);
        save();
        return true;
    }

    public boolean rejectTown(int id) {
        Town town = towns.get(id);
        if (town == null || town.status() != TownStatus.PENDING) {
            return false;
        }
        town.setStatus(TownStatus.REJECTED);
        save();
        return true;
    }

    public void setRolePoint(Town town, TownRole role, Location location) {
        town.setRolePoint(role, RolePoint.fromLocation(location));
        save();
    }

    public void applyRevenueTick(double amountPerInterval) {
        if (amountPerInterval <= 0D) {
            return;
        }

        for (Town town : towns.values()) {
            if (town.status() != TownStatus.APPROVED) {
                continue;
            }
            town.addRevenue(amountPerInterval);
            economyService.deposit(town.owner(), amountPerInterval);
        }

        save();
    }
}
