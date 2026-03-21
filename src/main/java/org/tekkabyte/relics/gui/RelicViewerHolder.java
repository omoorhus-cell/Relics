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
    private final boolean showLoadoutReturnButton;

    public RelicViewerHolder(int page, Mode mode, UUID owner, boolean showLoadoutReturnButton) {
        this.page = page;
        this.mode = mode;
        this.owner = owner;
        this.showLoadoutReturnButton = showLoadoutReturnButton;
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

    public boolean shouldShowLoadoutReturnButton() {
        return showLoadoutReturnButton;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
