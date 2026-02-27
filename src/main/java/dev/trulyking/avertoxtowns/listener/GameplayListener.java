package dev.trulyking.avertoxtowns.listener;

import dev.trulyking.avertoxtowns.AvertoxTownsPlugin;
import dev.trulyking.avertoxtowns.model.RoleContext;
import dev.trulyking.avertoxtowns.model.Town;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.FishHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class GameplayListener implements Listener {
    private static final String MAIN_MENU_TITLE = "Town Automation";
    private static final String SELL_MENU_TITLE = "Town Sell Handler";
    private static final String AUTOMATION_INFO_TITLE = "Automation Menu";

    private static final EnumSet<Material> CROPS = EnumSet.of(
        Material.WHEAT,
        Material.CARROTS,
        Material.POTATOES,
        Material.BEETROOTS,
        Material.NETHER_WART
    );

    private final AvertoxTownsPlugin plugin;
    private final Map<java.util.UUID, RoleContext> menuContext = new HashMap<>();

    public GameplayListener(AvertoxTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRoleBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }

        RoleContext roleContext = plugin.townManager().getRoleContextAt(event.getClickedBlock().getLocation());
        if (roleContext == null) {
            return;
        }

        event.setCancelled(true);
        menuContext.put(event.getPlayer().getUniqueId(), roleContext);
        openMainMenu(event.getPlayer(), roleContext);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!MAIN_MENU_TITLE.equals(title) && !SELL_MENU_TITLE.equals(title) && !AUTOMATION_INFO_TITLE.equals(title)) {
            return;
        }

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (MAIN_MENU_TITLE.equals(title)) {
            if (clicked.getType() == Material.HOPPER) {
                openAutomationInfo(player);
            } else if (clicked.getType() == Material.EMERALD) {
                openSellMenu(player);
            }
            return;
        }

        if (SELL_MENU_TITLE.equals(title) && clicked.getType() == Material.CHEST) {
            sellAll(player);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Town town = plugin.townManager().getApprovedTownAt(block.getLocation());
        if (town == null) {
            return;
        }

        applyBreakBoost(player);

        Material material = block.getType();
        if (isCrop(material)) {
            maybeApplyFarmerDropBoost(block, player);
            scheduleRegrow(block);
            return;
        }

        if (isLog(material)) {
            maybeApplyWoodcutterDropBoost(block, player);
            return;
        }

        if (isOre(material)) {
            maybeApplyMinerDropBoost(block, player);
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        Town town = plugin.townManager().getApprovedTownAt(player.getLocation());
        if (town == null) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            FishHook hook = event.getHook();
            int minWait = Math.max(1, plugin.getConfig().getInt("fishing.fast_min_wait_ticks", 20));
            int maxWait = Math.max(minWait, plugin.getConfig().getInt("fishing.fast_max_wait_ticks", 60));
            configureHookWait(hook, minWait, maxWait);
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item item) {
            double chance = plugin.getConfig().getDouble("fishing.bonus_catch_chance", 0.25D);
            if (ThreadLocalRandom.current().nextDouble() <= chance) {
                ItemStack extra = item.getItemStack().clone();
                extra.setAmount(1);
                player.getWorld().dropItemNaturally(player.getLocation(), extra);
                player.sendMessage("Fisherman territory bonus: extra catch.");
            }
        }
    }

    private void openMainMenu(Player player, RoleContext roleContext) {
        Inventory inventory = Bukkit.createInventory(null, 27, MAIN_MENU_TITLE);

        ItemStack automation = new ItemStack(Material.HOPPER);
        ItemMeta automationMeta = automation.getItemMeta();
        if (automationMeta != null) {
            automationMeta.setDisplayName("Automation Menu");
            automation.setItemMeta(automationMeta);
        }
        inventory.setItem(11, automation);

        ItemStack sell = new ItemStack(Material.EMERALD);
        ItemMeta sellMeta = sell.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName("Sell Handler");
            sell.setItemMeta(sellMeta);
        }
        inventory.setItem(15, sell);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("Role: " + roleContext.role().displayName());
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(13, info);

        player.openInventory(inventory);
    }

    private void openAutomationInfo(Player player) {
        RoleContext context = menuContext.get(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(null, 27, AUTOMATION_INFO_TITLE);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();
        if (meta != null && context != null) {
            meta.setDisplayName("Town #" + context.town().id() + " - " + context.town().name());
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("Role: " + context.role().displayName());
            lore.add("Automation access active at this role block.");
            lore.add("Sell handler is linked through this block.");
            meta.setLore(lore);
            info.setItemMeta(meta);
        }
        inventory.setItem(13, info);

        player.openInventory(inventory);
    }

    private void openSellMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, SELL_MENU_TITLE);

        ItemStack sellAll = new ItemStack(Material.CHEST);
        ItemMeta meta = sellAll.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Sell All Supported Items");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("Sells all items configured in sell_prices.");
            lore.add("Payout uses Vault if available.");
            meta.setLore(lore);
            sellAll.setItemMeta(meta);
        }

        inventory.setItem(13, sellAll);
        player.openInventory(inventory);
    }

    private void sellAll(Player player) {
        ConfigurationSection prices = plugin.getConfig().getConfigurationSection("sell_prices");
        if (prices == null) {
            player.sendMessage("Sell prices are not configured.");
            return;
        }

        double total = 0D;
        int soldAmount = 0;
        org.bukkit.inventory.PlayerInventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            String materialKey = item.getType().name().toUpperCase(Locale.ROOT);
            if (!prices.contains(materialKey)) {
                continue;
            }

            double price = prices.getDouble(materialKey, 0D);
            if (price <= 0D) {
                continue;
            }

            int amount = item.getAmount();
            total += price * amount;
            soldAmount += amount;
            inventory.setItem(slot, null);
        }

        if (soldAmount == 0) {
            player.sendMessage("No sellable items found in your inventory.");
            return;
        }

        plugin.economyService().deposit(player.getUniqueId(), total);
        if (plugin.economyService().isAvailable()) {
            player.sendMessage("Sold " + soldAmount + " items for $" + String.format(java.util.Locale.US, "%.2f", total));
        } else {
            player.sendMessage("Sold " + soldAmount + " items worth $" + String.format(java.util.Locale.US, "%.2f", total)
                + " (Vault not found, no external payout provider attached).");
        }
    }

    private void applyBreakBoost(Player player) {
        int seconds = Math.max(1, plugin.getConfig().getInt("boosts.break_boost_seconds", 3));
        int amplifier = Math.max(0, plugin.getConfig().getInt("boosts.break_boost_amplifier", 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, seconds * 20, amplifier, true, false, false));
    }

    private void scheduleRegrow(Block block) {
        Material cropType = block.getType();
        org.bukkit.block.data.BlockData originalData = block.getBlockData().clone();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            block.setType(cropType, false);
            if (originalData instanceof Ageable ageable) {
                org.bukkit.block.data.BlockData placedData = block.getBlockData();
                if (placedData instanceof Ageable placedAgeable) {
                    placedAgeable.setAge(ageable.getMaximumAge());
                    block.setBlockData(placedAgeable, false);
                }
            }
        }, 40L);
    }

    private void maybeApplyFarmerDropBoost(Block block, Player player) {
        if (!isMatureCrop(block)) {
            return;
        }

        double chance = plugin.getConfig().getDouble("boosts.farmer_extra_drop_chance", 0.25D);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }

        Material drop = cropDropMaterial(block.getType());
        if (drop != null) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(drop, 1));
        }
    }

    private void maybeApplyWoodcutterDropBoost(Block block, Player player) {
        double chance = plugin.getConfig().getDouble("boosts.woodcutter_extra_drop_chance", 0.20D);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
    }

    private void maybeApplyMinerDropBoost(Block block, Player player) {
        double chance = plugin.getConfig().getDouble("boosts.miner_extra_drop_chance", 0.20D);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }

        java.util.Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand(), player);
        ItemStack first = drops.stream().findFirst().orElse(null);
        if (first != null) {
            ItemStack extra = first.clone();
            extra.setAmount(1);
            block.getWorld().dropItemNaturally(block.getLocation(), extra);
        }
    }

    private void configureHookWait(Projectile hookProjectile, int min, int max) {
        try {
            Method setMin = hookProjectile.getClass().getMethod("setMinWaitTime", int.class);
            Method setMax = hookProjectile.getClass().getMethod("setMaxWaitTime", int.class);
            setMin.invoke(hookProjectile, min);
            setMax.invoke(hookProjectile, max);
            return;
        } catch (Exception ignored) {
        }

        try {
            Method setWait = hookProjectile.getClass().getMethod("setWaitTime", int.class);
            setWait.invoke(hookProjectile, min);
        } catch (Exception ignored) {
        }
    }

    private boolean isCrop(Material material) {
        return CROPS.contains(material);
    }

    private boolean isLog(Material material) {
        String name = material.name();
        return name.endsWith("_LOG") || name.endsWith("_STEM") || name.endsWith("_WOOD") || name.endsWith("_HYPHAE");
    }

    private boolean isOre(Material material) {
        String name = material.name();
        return name.endsWith("_ORE") || material == Material.ANCIENT_DEBRIS;
    }

    private boolean isMatureCrop(Block block) {
        if (!(block.getBlockData() instanceof Ageable ageable)) {
            return false;
        }
        return ageable.getAge() >= ageable.getMaximumAge();
    }

    private Material cropDropMaterial(Material cropBlock) {
        return switch (cropBlock) {
            case WHEAT -> Material.WHEAT;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }
}
