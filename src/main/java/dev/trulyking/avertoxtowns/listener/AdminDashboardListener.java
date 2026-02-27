package dev.trulyking.avertoxtowns.listener;

import dev.trulyking.avertoxtowns.AvertoxTownsPlugin;
import dev.trulyking.avertoxtowns.command.TownAdminCommand;
import dev.trulyking.avertoxtowns.model.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class AdminDashboardListener implements Listener {
    private final AvertoxTownsPlugin plugin;

    public AdminDashboardListener(AvertoxTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDashboardClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!TownAdminCommand.DASHBOARD_TITLE.equals(event.getView().getTitle())) {
            return;
        }

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        Integer id = meta.getPersistentDataContainer().get(plugin.dashboardTownIdKey(), PersistentDataType.INTEGER);
        if (id == null) {
            return;
        }

        ClickType click = event.getClick();
        boolean approved;
        boolean changed;
        if (click.isLeftClick()) {
            approved = true;
            changed = plugin.townManager().approveTown(id);
        } else if (click.isRightClick()) {
            approved = false;
            changed = plugin.townManager().rejectTown(id);
        } else {
            return;
        }

        if (!changed) {
            player.sendMessage("Application is no longer pending.");
            return;
        }

        Town town = plugin.townManager().getTown(id);
        if (town != null) {
            Player owner = Bukkit.getPlayer(town.owner());
            if (owner != null) {
                if (approved) {
                    owner.sendMessage(plugin.message("admin_approved", "<type>", town.type().displayName(), "<name>", town.name()));
                } else {
                    owner.sendMessage(plugin.message("admin_rejected", "<type>", town.type().displayName(), "<name>", town.name()));
                }
            }
        }

        player.sendMessage("Application #" + id + " " + (approved ? "approved" : "rejected") + ".");

        Bukkit.getScheduler().runTask(plugin, () -> new TownAdminCommand(plugin).openDashboard(player));
    }
}
