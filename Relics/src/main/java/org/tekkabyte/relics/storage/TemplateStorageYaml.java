package org.tekkabyte.relics.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.tekkabyte.relics.RelicsPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TemplateStorageYaml implements TemplateStorage {

    private final RelicsPlugin plugin;
    private final ItemStackCodec codec = new ItemStackCodec();

    private File file;
    private FileConfiguration yaml;

    private int maxPages;
    private int pageSize;
    private List<ItemStack> flat;

    public TemplateStorageYaml(RelicsPlugin plugin) {
        this.plugin = plugin;
        this.flat = new ArrayList<>();
    }

    @Override
    public void load() {
        this.maxPages = Math.max(1, plugin.getConfig().getInt("storage.max-pages", 20));
        this.pageSize = Math.max(1, plugin.getConfig().getInt("storage.page-size", 45));

        this.file = new File(plugin.getDataFolder(), "templates.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create templates.yml", e);
            }
        }

        this.yaml = YamlConfiguration.loadConfiguration(file);

        int total = maxPages * pageSize;
        this.flat = new ArrayList<>(total);
        for (int i = 0; i < total; i++) flat.add(null);

        List<String> encoded = yaml.getStringList("templates");
        int limit = Math.min(encoded.size(), total);
        for (int i = 0; i < limit; i++) {
            String s = encoded.get(i);
            if (s == null || s.isBlank()) continue;
            try {
                flat.set(i, codec.fromBase64(s));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void save() {
        int total = maxPages * pageSize;
        List<String> out = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            ItemStack it = flat.get(i);
            out.add(it == null ? "" : codec.toBase64(it));
        }
        yaml.set("templates", out);
        try {
            yaml.save(file);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save templates.yml", e);
        }
    }

    @Override
    public int getMaxPages() {
        return maxPages;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public ItemStack get(int page, int slot) {
        int idx = toIndex(page, slot);
        if (idx < 0 || idx >= flat.size()) return null;
        return flat.get(idx);
    }

    @Override
    public void set(int page, int slot, ItemStack item) {
        int idx = toIndex(page, slot);
        if (idx < 0 || idx >= flat.size()) return;
        flat.set(idx, item == null ? null : item.clone());
    }

    @Override
    public boolean delete(int page, int slot) {
        int idx = toIndex(page, slot);
        if (idx < 0 || idx >= flat.size()) return false;
        if (flat.get(idx) == null) return false;
        flat.set(idx, null);
        return true;
    }

    @Override
    public int saveNextFree(ItemStack item) {
        if (item == null) return -1;
        for (int i = 0; i < flat.size(); i++) {
            if (flat.get(i) == null) {
                flat.set(i, item.clone());
                return i;
            }
        }
        return -1;
    }

    @Override
    public int pageOf(int flatIndex) {
        if (flatIndex < 0) return 1;
        return (flatIndex / pageSize) + 1;
    }

    @Override
    public int slotOf(int flatIndex) {
        if (flatIndex < 0) return 0;
        return flatIndex % pageSize;
    }

    private int toIndex(int page, int slot) {
        if (page < 1) return -1;
        if (slot < 0 || slot >= pageSize) return -1;
        int idx = (page - 1) * pageSize + slot;
        return idx;
    }
}
