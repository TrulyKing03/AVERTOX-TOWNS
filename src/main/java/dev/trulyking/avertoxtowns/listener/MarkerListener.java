package dev.trulyking.avertoxtowns.listener;

import dev.trulyking.avertoxtowns.AvertoxTownsPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class MarkerListener implements Listener {
    private final AvertoxTownsPlugin plugin;

    public MarkerListener(AvertoxTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMarkerUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();
        if (!plugin.isMarkerStick(item)) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Player player = event.getPlayer();
        Location markerLocation = clickedBlock.getLocation();

        boolean firstCorner = action == Action.LEFT_CLICK_BLOCK;
        boolean updated = plugin.townManager().setCorner(player, firstCorner, markerLocation);
        if (!updated) {
            return;
        }

        if (firstCorner) {
            player.sendMessage("First corner set at "
                + markerLocation.getBlockX() + ", "
                + markerLocation.getBlockY() + ", "
                + markerLocation.getBlockZ());
        } else {
            player.sendMessage("Second corner set at "
                + markerLocation.getBlockX() + ", "
                + markerLocation.getBlockY() + ", "
                + markerLocation.getBlockZ());
        }

        event.setCancelled(true);
    }
}
