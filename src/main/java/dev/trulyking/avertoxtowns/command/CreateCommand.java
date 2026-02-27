package dev.trulyking.avertoxtowns.command;

import dev.trulyking.avertoxtowns.AvertoxTownsPlugin;
import dev.trulyking.avertoxtowns.model.CreateSession;
import dev.trulyking.avertoxtowns.model.Town;
import dev.trulyking.avertoxtowns.model.TownType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public final class CreateCommand implements CommandExecutor {
    private final AvertoxTownsPlugin plugin;

    public CreateCommand(AvertoxTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /create city <name> | /create village <name> | /create city finish");
            return true;
        }

        String first = args[0].toLowerCase(Locale.ROOT);
        if (first.equals("finish") || (args.length >= 2 && args[1].equalsIgnoreCase("finish"))) {
            Town town = plugin.townManager().finishSession(player);
            if (town == null) {
                player.sendMessage(plugin.message("application_need_corners"));
                return true;
            }

            removeMarkerSticks(player);
            player.sendMessage(
                plugin.message(
                    "application_submitted",
                    "<type>", town.type().displayName(),
                    "<name>", town.name()
                )
            );

            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("avertoxtowns.admin"))
                .forEach(p -> p.sendMessage("New town application #" + town.id() + " from " + player.getName()));
            return true;
        }

        TownType type;
        if (first.equals("city")) {
            type = TownType.CITY;
        } else if (first.equals("village")) {
            type = TownType.VILLAGE;
        } else {
            player.sendMessage("Use city or village.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /create " + first + " <name>");
            return true;
        }

        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                nameBuilder.append(' ');
            }
            nameBuilder.append(args[i]);
        }
        String name = nameBuilder.toString().trim();
        if (name.isEmpty() || name.equalsIgnoreCase("finish")) {
            player.sendMessage("Provide a valid name.");
            return true;
        }

        CreateSession existing = plugin.townManager().getSession(player.getUniqueId()).orElse(null);
        if (existing != null) {
            player.sendMessage("Previous application session replaced.");
        }

        plugin.townManager().startSession(player.getUniqueId(), type, name);
        player.getInventory().addItem(plugin.createMarkerStick());
        player.sendMessage(plugin.message("app_started", "<type>", type.displayName(), "<name>", name));
        return true;
    }

    private void removeMarkerSticks(Player player) {
        for (ItemStack content : player.getInventory().getContents()) {
            if (plugin.isMarkerStick(content)) {
                player.getInventory().removeItem(content);
            }
        }
    }
}
