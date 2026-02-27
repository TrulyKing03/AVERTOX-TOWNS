package dev.trulyking.avertoxtowns.service;

import dev.trulyking.avertoxtowns.model.RolePoint;
import dev.trulyking.avertoxtowns.model.Town;
import dev.trulyking.avertoxtowns.model.TownRole;
import dev.trulyking.avertoxtowns.model.TownStatus;
import dev.trulyking.avertoxtowns.model.TownType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TownDataStore {
    private final JavaPlugin plugin;
    private final File file;

    public TownDataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "towns.yml");
    }

    public LoadedData load() {
        if (!file.exists()) {
            return new LoadedData(1, new ArrayList<>());
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int nextId = Math.max(1, yaml.getInt("next-id", 1));
        List<Town> towns = new ArrayList<>();

        ConfigurationSection townsSection = yaml.getConfigurationSection("towns");
        if (townsSection == null) {
            return new LoadedData(nextId, towns);
        }

        for (String key : townsSection.getKeys(false)) {
            ConfigurationSection section = townsSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            try {
                int id = Integer.parseInt(key);
                TownType type = TownType.valueOf(section.getString("type", "CITY"));
                String name = section.getString("name", "Unnamed");
                UUID owner = UUID.fromString(section.getString("owner"));
                String world = section.getString("world", "world");
                int minX = section.getInt("min-x");
                int maxX = section.getInt("max-x");
                int minY = section.getInt("min-y");
                int maxY = section.getInt("max-y");
                int minZ = section.getInt("min-z");
                int maxZ = section.getInt("max-z");
                TownStatus status = TownStatus.valueOf(section.getString("status", "PENDING"));
                double revenueBalance = section.getDouble("revenue-balance", 0.0D);

                Town town = new Town(
                    id,
                    type,
                    name,
                    owner,
                    world,
                    minX,
                    maxX,
                    minY,
                    maxY,
                    minZ,
                    maxZ,
                    status,
                    revenueBalance
                );

                ConfigurationSection rolesSection = section.getConfigurationSection("role-points");
                if (rolesSection != null) {
                    Map<TownRole, RolePoint> rolePoints = new EnumMap<>(TownRole.class);
                    for (String roleName : rolesSection.getKeys(false)) {
                        ConfigurationSection roleSection = rolesSection.getConfigurationSection(roleName);
                        if (roleSection == null) {
                            continue;
                        }
                        TownRole role = TownRole.valueOf(roleName);
                        String roleWorld = roleSection.getString("world", world);
                        int x = roleSection.getInt("x");
                        int y = roleSection.getInt("y");
                        int z = roleSection.getInt("z");
                        rolePoints.put(role, new RolePoint(roleWorld, x, y, z));
                    }
                    town.setRolePoints(rolePoints);
                }

                towns.add(town);
            } catch (Exception ex) {
                plugin.getLogger().warning("Skipped invalid town entry " + key + ": " + ex.getMessage());
            }
        }

        return new LoadedData(nextId, towns);
    }

    public void save(Collection<Town> towns, int nextId) {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
            return;
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("next-id", nextId);

        for (Town town : towns) {
            String base = "towns." + town.id();
            yaml.set(base + ".type", town.type().name());
            yaml.set(base + ".name", town.name());
            yaml.set(base + ".owner", town.owner().toString());
            yaml.set(base + ".world", town.world());
            yaml.set(base + ".min-x", town.minX());
            yaml.set(base + ".max-x", town.maxX());
            yaml.set(base + ".min-y", town.minY());
            yaml.set(base + ".max-y", town.maxY());
            yaml.set(base + ".min-z", town.minZ());
            yaml.set(base + ".max-z", town.maxZ());
            yaml.set(base + ".status", town.status().name());
            yaml.set(base + ".revenue-balance", town.revenueBalance());

            for (Map.Entry<TownRole, RolePoint> entry : town.rolePoints().entrySet()) {
                String roleBase = base + ".role-points." + entry.getKey().name();
                RolePoint point = entry.getValue();
                yaml.set(roleBase + ".world", point.world());
                yaml.set(roleBase + ".x", point.x());
                yaml.set(roleBase + ".y", point.y());
                yaml.set(roleBase + ".z", point.z());
            }
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save towns.yml: " + e.getMessage());
        }
    }

    public record LoadedData(int nextId, List<Town> towns) {
    }
}
