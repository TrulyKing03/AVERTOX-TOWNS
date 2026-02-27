package dev.trulyking.avertoxtowns.command;

import dev.trulyking.avertoxtowns.AvertoxTownsPlugin;
import dev.trulyking.avertoxtowns.model.Town;
import dev.trulyking.avertoxtowns.model.TownRole;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SetCommand implements CommandExecutor {
    private final AvertoxTownsPlugin plugin;

    public SetCommand(AvertoxTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 2 || !args[0].equalsIgnoreCase("city")) {
            player.sendMessage("Usage: /set city <farmer|fisherman|woodcutter|miner>");
            return true;
        }

        TownRole role = TownRole.fromInput(args[1]).orElse(null);
        if (role == null) {
            player.sendMessage("Invalid role.");
            return true;
        }

        Town town = plugin.townManager().getOwnedApprovedTownAt(player.getUniqueId(), player.getLocation());
        if (town == null) {
            player.sendMessage(plugin.message("no_owned_town_here"));
            return true;
        }

        Location location = player.getLocation().getBlock().getLocation();
        plugin.townManager().setRolePoint(town, role, location);

        Block block = location.getBlock();
        Material material = plugin.roleMaterial(role);
        block.setType(material, false);

        player.sendMessage(plugin.message("role_set", "<role>", role.displayName()));
        return true;
    }
}
