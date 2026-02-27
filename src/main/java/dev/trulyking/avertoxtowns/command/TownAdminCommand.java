package dev.trulyking.avertoxtowns.command;

import dev.trulyking.avertoxtowns.AvertoxTownsPlugin;
import dev.trulyking.avertoxtowns.model.Town;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class TownAdminCommand implements CommandExecutor {
    public static final String DASHBOARD_TITLE = "Town Applications";
    private final AvertoxTownsPlugin plugin;

    public TownAdminCommand(AvertoxTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("avertoxtowns.admin")) {
            player.sendMessage("You do not have permission.");
            return true;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("approve") || args[0].equalsIgnoreCase("reject"))) {
            return handleActionCommand(player, args[0], args[1]);
        }

        openDashboard(player);
        return true;
    }

    private boolean handleActionCommand(Player player, String action, String idArg) {
        int id;
        try {
            id = Integer.parseInt(idArg);
        } catch (NumberFormatException ex) {
            player.sendMessage("Invalid id.");
            return true;
        }

        Town town = plugin.townManager().getTown(id);
        if (town == null) {
            player.sendMessage("Town not found.");
            return true;
        }

        boolean success = action.equalsIgnoreCase("approve")
            ? plugin.townManager().approveTown(id)
            : plugin.townManager().rejectTown(id);

        if (!success) {
            player.sendMessage("Town is not pending.");
            return true;
        }

        notifyOwner(town, action.equalsIgnoreCase("approve"));
        player.sendMessage("Application #" + id + " " + (action.equalsIgnoreCase("approve") ? "approved" : "rejected") + ".");
        return true;
    }

    public void openDashboard(Player player) {
        List<Town> pending = plugin.townManager().getPendingTowns();
        Inventory inventory = Bukkit.createInventory(null, 54, DASHBOARD_TITLE);

        int slot = 0;
        for (Town town : pending) {
            if (slot >= inventory.getSize()) {
                break;
            }
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("#" + town.id() + " " + town.type().displayName() + " " + town.name());
                List<String> lore = new ArrayList<>();
                lore.add("Owner: " + Bukkit.getOfflinePlayer(town.owner()).getName());
                lore.add("Area: " + (town.maxX() - town.minX() + 1) + "x" + (town.maxZ() - town.minZ() + 1));
                lore.add("Left-click: approve");
                lore.add("Right-click: reject");
                meta.setLore(lore);
                meta.getPersistentDataContainer().set(plugin.dashboardTownIdKey(), PersistentDataType.INTEGER, town.id());
                item.setItemMeta(meta);
            }
            inventory.setItem(slot++, item);
        }

        if (pending.isEmpty()) {
            ItemStack none = new ItemStack(Material.BARRIER);
            ItemMeta meta = none.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("No pending applications");
                none.setItemMeta(meta);
            }
            inventory.setItem(22, none);
        }

        player.openInventory(inventory);
    }

    public void notifyOwner(Town town, boolean approved) {
        Player owner = Bukkit.getPlayer(town.owner());
        if (owner == null) {
            return;
        }

        if (approved) {
            owner.sendMessage(plugin.message("admin_approved", "<type>", town.type().displayName(), "<name>", town.name()));
        } else {
            owner.sendMessage(plugin.message("admin_rejected", "<type>", town.type().displayName(), "<name>", town.name()));
        }
    }
}
