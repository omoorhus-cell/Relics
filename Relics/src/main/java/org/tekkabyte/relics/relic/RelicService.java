package org.tekkabyte.relics.relic;

import org.bukkit.inventory.ItemStack;

public interface RelicService {
    boolean isRelic(ItemStack item);
    String getRelicId(ItemStack item);
    ItemStack applyRelic(ItemStack item, String id);
}
