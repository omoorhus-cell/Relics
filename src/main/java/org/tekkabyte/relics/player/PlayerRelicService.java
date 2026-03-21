package org.tekkabyte.relics.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public interface PlayerRelicService {
    void load();
    void save();
    ItemStack getEquipped(UUID playerId, int page, int slot);
    boolean equip(Player player, int page, int slot, ItemStack relicItem);
    ItemStack unequip(Player player, int page, int slot);
    boolean hasActiveRelic(UUID playerId, String relicId);
    Set<String> getActiveRelicIds(UUID playerId);
}
