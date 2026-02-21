package org.tekkabyte.relics.listener;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.tekkabyte.relics.gui.RelicLoadoutGui;
import org.tekkabyte.relics.gui.RelicViewerGui;
import org.tekkabyte.relics.gui.RelicViewerHolder;
import org.tekkabyte.relics.player.PlayerRelicService;
import org.tekkabyte.relics.storage.TemplateStorage;

import java.util.Set;
import java.util.UUID;

public class RelicViewerListener implements Listener {

    private final RelicViewerGui gui;
    private final RelicLoadoutGui loadoutGui;
    private final TemplateStorage storage;
    private final PlayerRelicService playerRelicService;

    public RelicViewerListener(RelicViewerGui gui, RelicLoadoutGui loadoutGui, TemplateStorage storage, PlayerRelicService playerRelicService) {
        this.gui = gui;
        this.loadoutGui = loadoutGui;
        this.storage = storage;
        this.playerRelicService = playerRelicService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!gui.isViewerInventory(top)) return;

        if (!(e.getWhoClicked() instanceof Player p)) return;

        int raw = e.getRawSlot();
        int page = gui.getPage(top);
        RelicViewerHolder.Mode mode = gui.getMode(top);
        UUID owner = gui.getOwner(top);
        if (mode == RelicViewerHolder.Mode.PLAYER && owner != null && !owner.equals(p.getUniqueId())) return;

        if (mode == RelicViewerHolder.Mode.PLAYER && raw >= top.getSize()) {
            if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
            }
            return;
        }

        e.setCancelled(true);
        ItemStack current = e.getCurrentItem();

        if (gui.isControl(current)) {
            if (raw == RelicViewerGui.SLOT_PREV) reopen(p, mode, page - 1);
            if (raw == RelicViewerGui.SLOT_NEXT) reopen(p, mode, page + 1);
            if (raw == RelicViewerGui.SLOT_CLOSE) {
                playButtonSound(p);
                p.closeInventory();
            }
            return;
        }

        int storageSlot = gui.toStorageSlot(raw);
        if (storageSlot < 0) return;

        if (mode == RelicViewerHolder.Mode.PLAYER) {
            handlePlayerClick(e, p, page, storageSlot);
            return;
        }

        ItemStack template = storage.get(page, storageSlot);
        if (template == null) return;

        boolean op = p.isOp();
        ClickType click = e.getClick();

        if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
            if (!op) {
                return;
            }
            p.getInventory().addItem(template.clone());
            return;
        }

        if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
            if (!op) {
                return;
            }
            storage.delete(page, storageSlot);
            storage.save();
            gui.openAdmin(p, page);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!gui.isViewerInventory(top)) return;

        RelicViewerHolder.Mode mode = gui.getMode(top);
        if (mode == RelicViewerHolder.Mode.ADMIN) {
            e.setCancelled(true);
            return;
        }

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

        int page = gui.getPage(top);
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

        ItemStack oldCursor = e.getOldCursor();
        if (!playerRelicService.equip(player, page, storageSlot, oldCursor)) {
            e.setCancelled(true);
            return;
        }

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
        gui.openPlayer(player, page);
    }

    private void handlePlayerClick(InventoryClickEvent e, Player player, int page, int slot) {
        if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
            ItemStack removed = playerRelicService.unequip(player, page, slot);
            if (removed != null) {
                var leftover = player.getInventory().addItem(removed);
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
            gui.openPlayer(player, page);
            return;
        }

        ItemStack cursor = e.getCursor();
        if (playerRelicService.equip(player, page, slot, cursor)) {
            if (cursor != null) {
                int amount = cursor.getAmount() - 1;
                if (amount <= 0) {
                    e.setCursor(null);
                } else {
                    cursor.setAmount(amount);
                    e.setCursor(cursor);
                }
            }
            gui.openPlayer(player, page);
        }
    }

    private void reopen(Player player, RelicViewerHolder.Mode mode, int page) {
        if (page <= 0) {
            loadoutGui.open(player);
            return;
        }
        if (mode == RelicViewerHolder.Mode.PLAYER) {
            gui.openPlayer(player, page);
            return;
        }
        gui.openAdmin(player, page);
    }

    private void playButtonSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
    }
}
