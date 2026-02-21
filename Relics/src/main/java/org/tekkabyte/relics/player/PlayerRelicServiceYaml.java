package org.tekkabyte.relics.player;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tekkabyte.relics.RelicsPlugin;
import org.tekkabyte.relics.event.RelicEquipEvent;
import org.tekkabyte.relics.event.RelicUnequipEvent;
import org.tekkabyte.relics.relic.RelicService;
import org.tekkabyte.relics.storage.ItemStackCodec;
import org.tekkabyte.relics.storage.TemplateStorage;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlayerRelicServiceYaml implements PlayerRelicService {

    private final RelicsPlugin plugin;
    private final RelicService relicService;
    private final TemplateStorage templateStorage;
    private final ItemStackCodec codec = new ItemStackCodec();
    private final Map<UUID, Map<Integer, ItemStack>> activeByPlayer = new HashMap<>();

    private File file;
    private FileConfiguration yaml;

    public PlayerRelicServiceYaml(RelicsPlugin plugin, RelicService relicService, TemplateStorage templateStorage) {
        this.plugin = plugin;
        this.relicService = relicService;
        this.templateStorage = templateStorage;
    }

    @Override
    public void load() {
        this.file = new File(plugin.getDataFolder(), "active_relics.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create active_relics.yml", e);
            }
        }

        this.yaml = YamlConfiguration.loadConfiguration(file);
        this.activeByPlayer.clear();

        ConfigurationSection root = yaml.getConfigurationSection("players");
        if (root == null) return;

        for (String uuidRaw : root.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidRaw);
            } catch (Exception ignored) {
                continue;
            }

            ConfigurationSection relics = root.getConfigurationSection(uuidRaw + ".relics");
            if (relics == null) continue;

            Map<Integer, ItemStack> equipped = new HashMap<>();
            for (String idxRaw : relics.getKeys(false)) {
                int idx;
                try {
                    idx = Integer.parseInt(idxRaw);
                } catch (Exception ignored) {
                    continue;
                }
                String encoded = relics.getString(idxRaw, "");
                if (encoded == null || encoded.isBlank()) continue;
                try {
                    ItemStack item = codec.fromBase64(encoded);
                    if (item != null && relicService.isRelic(item)) {
                        equipped.put(idx, item);
                    }
                } catch (Exception ignored) {
                }
            }
            if (!equipped.isEmpty()) {
                activeByPlayer.put(uuid, equipped);
            }
        }
    }

    @Override
    public void save() {
        yaml.set("players", null);
        for (Map.Entry<UUID, Map<Integer, ItemStack>> playerEntry : activeByPlayer.entrySet()) {
            String base = "players." + playerEntry.getKey() + ".relics";
            for (Map.Entry<Integer, ItemStack> relicEntry : playerEntry.getValue().entrySet()) {
                yaml.set(base + "." + relicEntry.getKey(), codec.toBase64(relicEntry.getValue()));
            }
        }
        try {
            yaml.save(file);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save active_relics.yml", e);
        }
    }

    @Override
    public ItemStack getEquipped(UUID playerId, int page, int slot) {
        int idx = toIndex(page, slot);
        if (idx < 0) return null;
        Map<Integer, ItemStack> equipped = activeByPlayer.get(playerId);
        if (equipped == null) return null;
        ItemStack item = equipped.get(idx);
        return item == null ? null : item.clone();
    }

    @Override
    public boolean equip(Player player, int page, int slot, ItemStack relicItem) {
        if (player == null || relicItem == null) return false;
        int idx = toIndex(page, slot);
        if (idx < 0) return false;
        if (!isRelicItem(relicItem)) return false;

        Map<Integer, ItemStack> equipped = activeByPlayer.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        if (equipped.containsKey(idx)) return false;

        ItemStack one = relicItem.clone();
        one.setAmount(1);
        String relicId = relicService.getRelicId(one);

        RelicEquipEvent event = new RelicEquipEvent(player, page, slot, relicId, one.clone());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        equipped.put(idx, one);
        save();
        return true;
    }

    @Override
    public ItemStack unequip(Player player, int page, int slot) {
        if (player == null) return null;
        int idx = toIndex(page, slot);
        if (idx < 0) return null;
        Map<Integer, ItemStack> equipped = activeByPlayer.get(player.getUniqueId());
        if (equipped == null) return null;
        ItemStack removed = equipped.get(idx);
        if (removed == null) return null;

        String relicId = relicService.getRelicId(removed);
        RelicUnequipEvent event = new RelicUnequipEvent(player, page, slot, relicId, removed.clone());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;

        equipped.remove(idx);
        if (equipped.isEmpty()) {
            activeByPlayer.remove(player.getUniqueId());
        }
        save();
        return removed.clone();
    }

    @Override
    public boolean hasActiveRelic(UUID playerId, String relicId) {
        if (playerId == null || relicId == null) return false;
        Map<Integer, ItemStack> equipped = activeByPlayer.get(playerId);
        if (equipped == null || equipped.isEmpty()) return false;
        for (ItemStack item : equipped.values()) {
            if (Objects.equals(relicId, relicService.getRelicId(item))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getActiveRelicIds(UUID playerId) {
        Set<String> out = new HashSet<>();
        Map<Integer, ItemStack> equipped = activeByPlayer.get(playerId);
        if (equipped == null) return out;
        for (ItemStack item : equipped.values()) {
            String id = relicService.getRelicId(item);
            if (id != null && !id.isBlank()) out.add(id);
        }
        return out;
    }

    private boolean isRelicItem(ItemStack relicItem) {
        return relicService.isRelic(relicItem);
    }

    private int toIndex(int page, int slot) {
        if (page < 1 || slot < 0 || slot >= templateStorage.getPageSize()) return -1;
        return (page - 1) * templateStorage.getPageSize() + slot;
    }
}
