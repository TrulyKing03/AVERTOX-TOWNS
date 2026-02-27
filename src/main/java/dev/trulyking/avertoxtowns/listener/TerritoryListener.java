package dev.trulyking.avertoxtowns.listener;

import dev.trulyking.avertoxtowns.AvertoxTownsPlugin;
import dev.trulyking.avertoxtowns.model.Town;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TerritoryListener implements Listener {
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("2ae9f451-faa3-463b-a9a2-5dd4e3c1f8b1");
    private final AvertoxTownsPlugin plugin;
    private final Map<UUID, Integer> currentTownByPlayer = new HashMap<>();

    public TerritoryListener(AvertoxTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Town currentTown = plugin.townManager().getApprovedTownAt(player.getLocation());
        if (currentTown != null) {
            currentTownByPlayer.put(player.getUniqueId(), currentTown.id());
            applySpeedBonus(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        currentTownByPlayer.remove(player.getUniqueId());
        removeSpeedBonus(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || event.getFrom().getWorld() == null || event.getTo().getWorld() == null) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
            && event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        Integer knownTownId = currentTownByPlayer.get(playerId);
        Town fromTown = knownTownId == null ? plugin.townManager().getApprovedTownAt(event.getFrom()) : plugin.townManager().getTown(knownTownId);
        Town toTown = plugin.townManager().getApprovedTownAt(event.getTo());

        int fromId = fromTown == null ? -1 : fromTown.id();
        int toId = toTown == null ? -1 : toTown.id();

        if (fromId == toId) {
            if (toTown != null) {
                applySpeedBonus(player);
            } else {
                removeSpeedBonus(player);
            }
            return;
        }

        if (toTown != null) {
            player.sendMessage(plugin.message("enter", "<name>", toTown.name()));
            currentTownByPlayer.put(playerId, toTown.id());
            applySpeedBonus(player);
        } else {
            if (fromTown != null) {
                player.sendMessage(plugin.message("leave", "<name>", fromTown.name()));
            }
            currentTownByPlayer.remove(playerId);
            removeSpeedBonus(player);
        }
    }

    private void applySpeedBonus(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }

        removeSpeedModifier(attribute);

        double amount = plugin.getConfig().getDouble("territory.speed_bonus_scalar", 0.08D);
        if (amount <= 0D) {
            return;
        }

        AttributeModifier modifier = new AttributeModifier(
            SPEED_MODIFIER_ID,
            "avertoxtowns_territory_speed",
            amount,
            AttributeModifier.Operation.ADD_SCALAR
        );
        attribute.addModifier(modifier);
    }

    private void removeSpeedBonus(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }
        removeSpeedModifier(attribute);
    }

    private void removeSpeedModifier(AttributeInstance attribute) {
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (modifier.getUniqueId().equals(SPEED_MODIFIER_ID)) {
                attribute.removeModifier(modifier);
            }
        }
    }
}
