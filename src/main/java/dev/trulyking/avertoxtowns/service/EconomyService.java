package dev.trulyking.avertoxtowns.service;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.UUID;

public final class EconomyService {
    private final JavaPlugin plugin;
    private Object economyProvider;
    private Method depositOfflinePlayerMethod;
    private Method depositNameMethod;

    public EconomyService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null) {
            return;
        }

        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            ServicesManager servicesManager = plugin.getServer().getServicesManager();
            Method getRegistration = ServicesManager.class.getMethod("getRegistration", Class.class);
            Object registration = getRegistration.invoke(servicesManager, economyClass);
            if (registration == null) {
                return;
            }

            Method getProvider = registration.getClass().getMethod("getProvider");
            Object provider = getProvider.invoke(registration);
            if (provider == null) {
                return;
            }

            economyProvider = provider;

            for (Method method : provider.getClass().getMethods()) {
                if (!method.getName().equals("depositPlayer") || method.getParameterCount() != 2) {
                    continue;
                }

                Class<?> firstParam = method.getParameterTypes()[0];
                Class<?> secondParam = method.getParameterTypes()[1];
                if (secondParam != double.class && secondParam != Double.class) {
                    continue;
                }

                if (OfflinePlayer.class.isAssignableFrom(firstParam)) {
                    depositOfflinePlayerMethod = method;
                }
                if (String.class.isAssignableFrom(firstParam)) {
                    depositNameMethod = method;
                }
            }

            plugin.getLogger().info("Vault economy provider hooked.");
        } catch (Exception ex) {
            plugin.getLogger().warning("Vault detected but hook failed: " + ex.getMessage());
        }
    }

    public boolean isAvailable() {
        return economyProvider != null;
    }

    public void deposit(UUID uuid, double amount) {
        if (economyProvider == null || amount <= 0D) {
            return;
        }

        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (depositOfflinePlayerMethod != null) {
                depositOfflinePlayerMethod.invoke(economyProvider, player, amount);
                return;
            }
            if (depositNameMethod != null) {
                depositNameMethod.invoke(economyProvider, player.getName(), amount);
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("Economy deposit failed for " + uuid + ": " + ex.getMessage());
        }
    }
}
