package org.tekkabyte.relics.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class RelicViewerHolder implements InventoryHolder {

    public enum Mode {
        ADMIN,
        PLAYER
    }

    private final int page;
    private final Mode mode;
    private final UUID owner;

    public RelicViewerHolder(int page, Mode mode, UUID owner) {
        this.page = page;
        this.mode = mode;
        this.owner = owner;
    }

    public int getPage() {
        return page;
    }

    public Mode getMode() {
        return mode;
    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
