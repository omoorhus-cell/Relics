package org.tekkabyte.relics.relic;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface RelicService {
    boolean isRelic(ItemStack item);
    String getRelicId(ItemStack item);
    ItemStack applyRelic(ItemStack item, String id);
    ItemStack getRelicItem(String relicId);
    boolean giveRelic(Player player, String relicId);
}
