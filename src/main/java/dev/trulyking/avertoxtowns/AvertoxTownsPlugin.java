package dev.trulyking.avertoxtowns;

import dev.trulyking.avertoxtowns.command.CreateCommand;
import dev.trulyking.avertoxtowns.command.SetCommand;
import dev.trulyking.avertoxtowns.command.TownAdminCommand;
import dev.trulyking.avertoxtowns.listener.AdminDashboardListener;
import dev.trulyking.avertoxtowns.listener.GameplayListener;
import dev.trulyking.avertoxtowns.listener.MarkerListener;
import dev.trulyking.avertoxtowns.listener.TerritoryListener;
import dev.trulyking.avertoxtowns.model.TownRole;
import dev.trulyking.avertoxtowns.service.EconomyService;
import dev.trulyking.avertoxtowns.service.TownDataStore;
import dev.trulyking.avertoxtowns.service.TownManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class AvertoxTownsPlugin extends JavaPlugin {
    private TownManager townManager;
    private EconomyService economyService;
    private NamespacedKey markerKey;
    private NamespacedKey dashboardTownIdKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        markerKey = new NamespacedKey(this, "marker-stick");
        dashboardTownIdKey = new NamespacedKey(this, "dashboard-town-id");

        economyService = new EconomyService(this);
        economyService.setup();

        TownDataStore dataStore = new TownDataStore(this);
        townManager = new TownManager(this, dataStore, economyService);
        townManager.load();

        registerCommands();
        registerListeners();
        startRevenueTask();

        getLogger().info("AVERTOX-TOWNS enabled.");
    }

    @Override
    public void onDisable() {
        if (townManager != null) {
            townManager.save();
        }
    }

    private void registerCommands() {
        PluginCommand create = getCommand("create");
        if (create != null) {
            create.setExecutor(new CreateCommand(this));
        }

        PluginCommand set = getCommand("set");
        if (set != null) {
            set.setExecutor(new SetCommand(this));
        }

        PluginCommand townAdmin = getCommand("townadmin");
        if (townAdmin != null) {
            townAdmin.setExecutor(new TownAdminCommand(this));
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new MarkerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TerritoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameplayListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AdminDashboardListener(this), this);
    }

    private void startRevenueTask() {
        int minutes = Math.max(1, getConfig().getInt("revenue.interval_minutes", 10));
        long periodTicks = minutes * 60L * 20L;
        double amount = getConfig().getDouble("revenue.amount_per_interval", 50D);

        Bukkit.getScheduler().runTaskTimer(this, () -> townManager.applyRevenueTick(amount), periodTicks, periodTicks);
    }

    public TownManager townManager() {
        return townManager;
    }

    public EconomyService economyService() {
        return economyService;
    }

    public NamespacedKey markerKey() {
        return markerKey;
    }

    public NamespacedKey dashboardTownIdKey() {
        return dashboardTownIdKey;
    }

    public ItemStack createMarkerStick() {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getConfig().getString("marker.name", "Town Marker Stick"));
            meta.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, (byte) 1);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            stick.setItemMeta(meta);
        }
        return stick;
    }

    public boolean isMarkerStick(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != Material.STICK || !itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte value = meta.getPersistentDataContainer().get(markerKey, PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    public Material roleMaterial(TownRole role) {
        String path = "automation.materials." + role.name();
        String configured = getConfig().getString(path, role.name());
        Material material = Material.matchMaterial(configured == null ? role.name() : configured.toUpperCase(Locale.ROOT));
        return material == null ? Material.STONE : material;
    }

    public String message(String path, String... replacements) {
        String message = getConfig().getString("messages." + path, path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }
}
