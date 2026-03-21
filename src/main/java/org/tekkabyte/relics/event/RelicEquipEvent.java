package org.tekkabyte.relics.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class RelicEquipEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int page;
    private final int slot;
    private final String relicId;
    private final ItemStack relicItem;
    private boolean cancelled;

    public RelicEquipEvent(Player player, int page, int slot, String relicId, ItemStack relicItem) {
        this.player = player;
        this.page = page;
        this.slot = slot;
        this.relicId = relicId;
        this.relicItem = relicItem;
    }

    public Player getPlayer() {
        return player;
    }

    public int getPage() {
        return page;
    }

    public int getSlot() {
        return slot;
    }

    public String getRelicId() {
        return relicId;
    }

    public ItemStack getRelicItem() {
        return relicItem;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
