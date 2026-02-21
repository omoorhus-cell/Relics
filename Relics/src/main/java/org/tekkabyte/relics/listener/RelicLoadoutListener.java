package org.tekkabyte.relics.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.tekkabyte.relics.gui.RelicLoadoutGui;
import org.tekkabyte.relics.gui.RelicViewerGui;
import org.tekkabyte.relics.player.PlayerRelicService;

import java.util.Set;
import java.util.UUID;

public class RelicLoadoutListener implements Listener {

    private final RelicLoadoutGui gui;
    private final RelicViewerGui viewerGui;
    private final PlayerRelicService playerRelicService;

    public RelicLoadoutListener(RelicLoadoutGui gui, RelicViewerGui viewerGui, PlayerRelicService playerRelicService) {
        this.gui = gui;
        this.viewerGui = viewerGui;
        this.playerRelicService = playerRelicService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!gui.isLoadoutInventory(top)) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;

        int raw = e.getRawSlot();
        UUID owner = gui.getOwner(top);
        if (owner != null && !owner.equals(player.getUniqueId())) return;

        if (raw >= top.getSize()) {
            if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
            }
            return;
        }

        e.setCancelled(true);
        if (raw == RelicLoadoutGui.SLOT_RETURN_CORE) {
            if (!openArtifactedCoreGui(player)) {
                player.sendMessage("You do not have a core placed.");
            }
            return;
        }
        if (raw == RelicLoadoutGui.SLOT_OPEN_VIEWER) {
            viewerGui.openPlayer(player, 1);
            return;
        }
        int storageSlot = gui.toStorageSlot(raw);
        if (storageSlot < 0) return;
        if (gui.isLockedDisplaySlot(raw) || gui.isLockedStorageSlot(storageSlot)) return;

        if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
            ItemStack removed = playerRelicService.unequip(player, RelicLoadoutGui.PAGE, storageSlot);
            if (removed != null) {
                player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.9f, 1.0f);
                var leftover = player.getInventory().addItem(removed);
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
            gui.open(player);
            return;
        }

        ItemStack cursor = e.getCursor();
        if (playerRelicService.equip(player, RelicLoadoutGui.PAGE, storageSlot, cursor)) {
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.9f, 1.1f);
            if (cursor != null) {
                int amount = cursor.getAmount() - 1;
                if (amount <= 0) {
                    e.setCursor(null);
                } else {
                    cursor.setAmount(amount);
                    e.setCursor(cursor);
                }
            }
            gui.open(player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!gui.isLoadoutInventory(top)) return;
        if (!(e.getWhoClicked() instanceof Player player)) {
            e.setCancelled(true);
            return;
        }

        UUID owner = gui.getOwner(top);
        if (owner != null && !owner.equals(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        int topSize = top.getSize();
        boolean touchesTop = false;
        for (int rawSlot : e.getRawSlots()) {
            if (rawSlot < topSize) {
                touchesTop = true;
                break;
            }
        }
        if (!touchesTop) return;

        Set<Integer> rawSlots = e.getRawSlots();
        if (rawSlots.size() != 1) {
            e.setCancelled(true);
            return;
        }

        int target = rawSlots.iterator().next();
        int storageSlot = gui.toStorageSlot(target);
        if (storageSlot < 0) {
            e.setCancelled(true);
            return;
        }
        if (gui.isLockedDisplaySlot(target) || gui.isLockedStorageSlot(storageSlot)) {
            e.setCancelled(true);
            return;
        }

        ItemStack oldCursor = e.getOldCursor();
        if (!playerRelicService.equip(player, RelicLoadoutGui.PAGE, storageSlot, oldCursor)) {
            e.setCancelled(true);
            return;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.9f, 1.1f);

        ItemStack newCursor = oldCursor == null ? null : oldCursor.clone();
        if (newCursor != null) {
            int amount = oldCursor.getAmount() - 1;
            if (amount <= 0) {
                newCursor = null;
            } else {
                newCursor.setAmount(amount);
            }
        }
        e.setCursor(newCursor);
        e.setCancelled(true);
        gui.open(player);
    }

    private boolean openArtifactedCoreGui(Player player) {
        Plugin plugin = player.getServer().getPluginManager().getPlugin("ArtifactedCore");
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        if (tryInvokeCoreOpenMethod(plugin, "openCoreGui", player)) return true;
        if (tryInvokeCoreOpenMethod(plugin, "openCore", player)) return true;
        return tryOpenViaCoreClasses(plugin, player);
    }

    private boolean tryInvokeCoreOpenMethod(Plugin plugin, String methodName, Player player) {
        Inventory before = player.getOpenInventory().getTopInventory();
        try {
            var method = plugin.getClass().getMethod(methodName, Player.class);
            Object result = method.invoke(plugin, player);
            if (result instanceof Boolean b) {
                return b;
            }
            Inventory after = player.getOpenInventory().getTopInventory();
            return after != null && after != before;
        } catch (NoSuchMethodException ignored) {
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean tryOpenViaCoreClasses(Plugin plugin, Player player) {
        try {
            Object dataService = plugin.getClass().getMethod("getDataService").invoke(plugin);
            if (dataService == null) return false;

            Object state = dataService.getClass().getMethod("get", UUID.class).invoke(dataService, player.getUniqueId());
            if (state == null) return false;

            Object coreLocationValue = state.getClass().getField("coreLocation").get(state);
            if (!(coreLocationValue instanceof org.bukkit.Location coreLocation) || coreLocation.getWorld() == null) {
                return false;
            }

            Object guiManager;
            try {
                guiManager = plugin.getClass().getMethod("getGuiManager").invoke(plugin);
            } catch (NoSuchMethodException ignored) {
                var guiField = plugin.getClass().getDeclaredField("gui");
                guiField.setAccessible(true);
                guiManager = guiField.get(plugin);
            }
            if (guiManager == null) return false;

            Inventory before = player.getOpenInventory().getTopInventory();
            guiManager.getClass()
                    .getMethod("openCore", Player.class, org.bukkit.block.Block.class)
                    .invoke(guiManager, player, coreLocation.getBlock());
            Inventory after = player.getOpenInventory().getTopInventory();
            return after != null && after != before;
        } catch (Exception ignored) {
            return false;
        }
    }

}
