package org.tekkabyte.relics.relic;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.tekkabyte.relics.RelicsPlugin;
import org.tekkabyte.relics.storage.TemplateStorage;

import java.util.Map;
import java.util.Objects;

public class RelicServiceImpl implements RelicService {

    private final NamespacedKey KEY_RELIC;
    private final NamespacedKey KEY_RELIC_ID;
    private final TemplateStorage templateStorage;

    public RelicServiceImpl(RelicsPlugin plugin, TemplateStorage templateStorage) {
        this.KEY_RELIC = new NamespacedKey(plugin, "relic");
        this.KEY_RELIC_ID = new NamespacedKey(plugin, "relic_id");
        this.templateStorage = templateStorage;
    }

    @Override
    public boolean isRelic(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte flag = pdc.get(KEY_RELIC, PersistentDataType.BYTE);
        return flag != null && flag == (byte) 1;
    }

    @Override
    public String getRelicId(ItemStack item) {
        if (!isRelic(item)) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(KEY_RELIC_ID, PersistentDataType.STRING);
    }

    @Override
    public ItemStack applyRelic(ItemStack item, String id) {
        if (item == null || item.getType() == Material.AIR) return item;
        ItemStack out = item.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta == null) return out;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_RELIC, PersistentDataType.BYTE, (byte) 1);
        pdc.set(KEY_RELIC_ID, PersistentDataType.STRING, id);
        out.setItemMeta(meta);
        return out;
    }

    @Override
    public ItemStack getRelicItem(String relicId) {
        if (relicId == null || relicId.isBlank()) return null;
        ItemStack template = findTemplateByRelicId(relicId);
        return template == null ? null : template.clone();
    }

    @Override
    public boolean giveRelic(Player player, String relicId) {
        if (player == null) return false;

        ItemStack relicItem = getRelicItem(relicId);
        if (relicItem == null) return false;

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(relicItem);
        for (ItemStack leftover : leftovers.values()) {
            if (leftover == null || leftover.getType() == Material.AIR) continue;
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
        return true;
    }

    private ItemStack findTemplateByRelicId(String relicId) {
        int maxPages = templateStorage.getMaxPages();
        int pageSize = templateStorage.getPageSize();

        for (int page = 1; page <= maxPages; page++) {
            for (int slot = 0; slot < pageSize; slot++) {
                ItemStack template = templateStorage.get(page, slot);
                if (template == null) continue;
                if (Objects.equals(relicId, getRelicId(template))) {
                    return template;
                }
            }
        }

        return null;
    }
}
