package org.tekkabyte.relics.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;
import org.tekkabyte.relics.player.PlayerRelicService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RelicLoadoutGui {

    public static final String TITLE = "&#A855F7&lRELICS";
    public static final int SIZE = 27;
    public static final int PAGE = 1;
    public static final int SLOT_RETURN_CORE = 18;
    public static final int SLOT_OPEN_VIEWER = 26;
    public static final int[] LOADOUT_SLOTS = {10, 11, 12, 13, 14, 15, 16};

    private static final int BASE_UNLOCKED_SLOTS = 4;

    private final PlayerRelicService playerRelicService;

    public RelicLoadoutGui(PlayerRelicService playerRelicService) {
        this.playerRelicService = playerRelicService;
    }

    public void open(Player player) {
        open(player, true);
    }

    public void open(Player player, boolean includeReturnButton) {
        Inventory inv = Bukkit.createInventory(new RelicLoadoutHolder(player.getUniqueId(), includeReturnButton), SIZE, ItemBuilder.parseLegacyText(TITLE));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").hideTooltip().build();
        for (int slot = 0; slot < SIZE; slot++) {
            inv.setItem(slot, filler);
        }

        for (int i = 0; i < LOADOUT_SLOTS.length; i++) {
            int slot = LOADOUT_SLOTS[i];
            if (isLockedStorageSlot(player, i)) {
                inv.setItem(slot, lockedButton());
                continue;
            }
            inv.setItem(slot, relicSlotButton());
        }

        for (int i = 0; i < LOADOUT_SLOTS.length; i++) {
            if (isLockedStorageSlot(player, i)) continue;
            ItemStack equipped = playerRelicService.getEquipped(player.getUniqueId(), PAGE, i);
            if (equipped == null) continue;
            inv.setItem(LOADOUT_SLOTS[i], withLore(equipped.clone(), List.of(
                    "",
                    "&a&lACTIVATED",
                    "&7RIGHT-CLICK to unequip"
            )));
        }

        if (includeReturnButton) {
            inv.setItem(
                    SLOT_RETURN_CORE,
                    returnButton()
            );
        }
        inv.setItem(
                SLOT_OPEN_VIEWER,
                ItemBuilder.of(Material.ENDER_EYE)
                        .name("&#A855F7&lRELIC INFO")
                        .lore(java.util.List.of("&7", "&#A855F7ℹ &fLearn about relics", "&7", "&7⮩ ᴄʟɪᴄᴋ ᴍᴇ"))
                        .build()
        );

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.1f);
        player.openInventory(inv);
    }

    public boolean isLoadoutInventory(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof RelicLoadoutHolder;
    }

    public UUID getOwner(Inventory inventory) {
        if (!(inventory.getHolder() instanceof RelicLoadoutHolder holder)) {
            return null;
        }
        return holder.owner;
    }

    public boolean shouldShowReturnButton(Inventory inventory) {
        return inventory != null
                && inventory.getHolder() instanceof RelicLoadoutHolder holder
                && holder.includeReturnButton;
    }

    public int toStorageSlot(int rawSlot) {
        for (int i = 0; i < LOADOUT_SLOTS.length; i++) {
            if (LOADOUT_SLOTS[i] == rawSlot) {
                return i;
            }
        }
        return -1;
    }

    public boolean isLockedDisplaySlot(Player player, int rawSlot) {
        int storageSlot = toStorageSlot(rawSlot);
        if (storageSlot < 0) return false;
        return isLockedStorageSlot(player, storageSlot);
    }

    public boolean isLockedStorageSlot(Player player, int storageSlot) {
        if (storageSlot < 0) return true;
        return storageSlot >= unlockedStorageSlots(player);
    }

    private int unlockedStorageSlots(Player player) {
        return Math.min(LOADOUT_SLOTS.length, BASE_UNLOCKED_SLOTS + extraUnlockedSlotsFromArtifactedCore(player));
    }

    private int maxExtraUnlockedSlots() {
        return Math.max(0, LOADOUT_SLOTS.length - Math.max(0, BASE_UNLOCKED_SLOTS));
    }

    private int extraUnlockedSlotsFromArtifactedCore(Player player) {
        if (player == null) return 0;
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("ArtifactedCore");
            if (plugin == null || !plugin.isEnabled()) return 0;

            try {
                Object unlocked = plugin.getClass().getMethod("getUnlockedRelicLoadoutSlots", Player.class).invoke(plugin, player);
                if (unlocked instanceof Number n) {
                    int total = n.intValue();
                    int extra = Math.max(0, total - BASE_UNLOCKED_SLOTS);
                    return Math.min(maxExtraUnlockedSlots(), extra);
                }
            } catch (NoSuchMethodException ignored) {
            }

            try {
                Object unlocked = plugin.getClass().getMethod("getUnlockedRelicLoadoutSlots", UUID.class).invoke(plugin, player.getUniqueId());
                if (unlocked instanceof Number n) {
                    int total = n.intValue();
                    int extra = Math.max(0, total - BASE_UNLOCKED_SLOTS);
                    return Math.min(maxExtraUnlockedSlots(), extra);
                }
            } catch (NoSuchMethodException ignored) {
            }

            Object state;
            try {
                state = plugin.getClass().getMethod("getPlayerState", Player.class).invoke(plugin, player);
            } catch (NoSuchMethodException ignored) {
                state = plugin.getClass().getMethod("getPlayerState", UUID.class).invoke(plugin, player.getUniqueId());
            }
            if (state == null) return 0;

            Object value = state.getClass().getField("prestigeRelicSlotUpgradeLevel").get(state);
            if (!(value instanceof Number n)) return 0;
            int extra = Math.max(0, n.intValue());
            return Math.min(maxExtraUnlockedSlots(), extra);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private ItemStack relicSlotButton() {
        return ItemBuilder.of(Material.STONE_BUTTON)
                .name("&c&lDEACTIVATED")
                .lore(java.util.List.of("&7", "&cℹ&f Drag and drop a relic to activate"))
                .build();
    }

    private ItemStack lockedButton() {
        return ItemBuilder.of(Material.POLISHED_BLACKSTONE_BUTTON)
                .name("&c&lLOCKED")
                .lore(java.util.List.of("&7", "&c✖&f Unlock in Prestige Shop"))
                .build();
    }

    private ItemStack returnButton() {
        ItemStack item = ItemBuilder.of(Material.TIPPED_ARROW)
                .name(RelicViewerGui.PREVIOUS_BUTTON_NAME)
                .lore(RelicViewerGui.PREVIOUS_BUTTON_LORE)
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setBasePotionType(PotionType.HEALING);
            potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(potionMeta);
        }
        return item;
    }

    private ItemStack withLore(ItemStack item, List<String> extraLore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        for (String line : extraLore) {
            lore.add(ItemBuilder.parseLegacyText(line));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static final class RelicLoadoutHolder implements InventoryHolder {
        private final UUID owner;
        private final boolean includeReturnButton;

        private RelicLoadoutHolder(UUID owner, boolean includeReturnButton) {
            this.owner = owner;
            this.includeReturnButton = includeReturnButton;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
