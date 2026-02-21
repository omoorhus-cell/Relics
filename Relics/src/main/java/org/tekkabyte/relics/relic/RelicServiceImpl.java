package org.tekkabyte.relics.relic;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.tekkabyte.relics.RelicsPlugin;

public class RelicServiceImpl implements RelicService {

    private final NamespacedKey KEY_RELIC;
    private final NamespacedKey KEY_RELIC_ID;

    public RelicServiceImpl(RelicsPlugin plugin) {
        this.KEY_RELIC = new NamespacedKey(plugin, "relic");
        this.KEY_RELIC_ID = new NamespacedKey(plugin, "relic_id");
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
}
