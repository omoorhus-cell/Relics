package org.tekkabyte.relics.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.tekkabyte.relics.RelicsPlugin;
import org.tekkabyte.relics.player.PlayerRelicService;
import org.tekkabyte.relics.relic.RelicService;
import org.tekkabyte.relics.storage.TemplateSlotLayout;
import org.tekkabyte.relics.storage.TemplateStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RelicViewerGui {

    public static final int SLOT_PREV = 47;
    public static final int SLOT_NEXT = 51;
    public static final int SLOT_CLOSE = 49;
    public static final int[] ADMIN_RELIC_SLOTS = TemplateSlotLayout.RELIC_TEMPLATE_SLOTS;
    public static final String PREVIOUS_BUTTON_NAME = "&#F92524&lRETURN";
    public static final List<String> PREVIOUS_BUTTON_LORE = List.of("&7", "&#F92524ℹ&f Click to return to previous page");
    public static final String CLOSE_BUTTON_NAME = "&c&lEXIT";
    public static final List<String> CLOSE_BUTTON_LORE = List.of("&7", "&cℹ&f Click to exit the GUI");
    public static final String NEXT_BUTTON_NAME = "&a&lNEXT";
    public static final List<String> NEXT_BUTTON_LORE = List.of("&7", "&aℹ&f Click to go to the next page");

    private final RelicsPlugin plugin;
    private final RelicService relicService;
    private final PlayerRelicService playerRelicService;
    private final TemplateStorage storage;
    private final MiniMessage mm;
    private final NamespacedKey CONTROL_KEY;

    public RelicViewerGui(RelicsPlugin plugin, RelicService relicService, PlayerRelicService playerRelicService, TemplateStorage storage) {
        this.plugin = plugin;
        this.relicService = relicService;
        this.playerRelicService = playerRelicService;
        this.storage = storage;
        this.mm = MiniMessage.miniMessage();
        this.CONTROL_KEY = new NamespacedKey(plugin, "gui_control");
    }

    public void open(Player player, int page) {
        openAdmin(player, page);
    }

    public void openAdmin(Player player, int page) {
        open(player, page, RelicViewerHolder.Mode.ADMIN);
    }

    public void openPlayer(Player player, int page) {
        open(player, page, RelicViewerHolder.Mode.PLAYER);
    }

    private void open(Player player, int page, RelicViewerHolder.Mode mode) {
        int maxPages = storage.getMaxPages();
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        String title = "&#A855F7&lRELIC INFO &r(Page {page})".replace("{page}", String.valueOf(page));

        Inventory inv = Bukkit.createInventory(new RelicViewerHolder(page, mode, player.getUniqueId()), 54, ItemBuilder.parseLegacyText(title));

        if (mode == RelicViewerHolder.Mode.ADMIN) {
            openAdminFrame(player, page, maxPages, inv);
            return;
        }

        openPlayerFrame(player, page, maxPages, inv);
    }

    private void openPlayerFrame(Player player, int page, int maxPages, Inventory inv) {
        ItemStack filler = plainItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < inv.getSize(); slot++) {
            inv.setItem(slot, filler);
        }

        ItemStack placeholder = plainItem(Material.STONE_BUTTON, " ");
        for (int slot : ADMIN_RELIC_SLOTS) {
            inv.setItem(slot, placeholder);
        }

        int visibleSlots = getVisibleTemplateCount();
        for (int displayIndex = 0; displayIndex < visibleSlots; displayIndex++) {
            int slot = ADMIN_RELIC_SLOTS[displayIndex];
            ItemStack template = storage.get(page, displayIndex);
            if (template == null) continue;
            inv.setItem(slot, template.clone());
        }

        inv.setItem(
                SLOT_PREV,
                controlPotionItem(PREVIOUS_BUTTON_NAME, PREVIOUS_BUTTON_LORE, PotionType.HEALING)
        );
        inv.setItem(
                SLOT_NEXT,
                controlPotionItem(NEXT_BUTTON_NAME, NEXT_BUTTON_LORE, PotionType.LUCK)
        );
        inv.setItem(
                SLOT_CLOSE,
                controlItem(Material.RED_DYE, CLOSE_BUTTON_NAME, CLOSE_BUTTON_LORE)
        );

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.1f);
        player.openInventory(inv);
    }

    private void openAdminFrame(Player player, int page, int maxPages, Inventory inv) {
        ItemStack filler = plainItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < inv.getSize(); slot++) {
            inv.setItem(slot, filler);
        }

        ItemStack placeholder = plainItem(Material.STONE_BUTTON, " ");
        for (int slot : ADMIN_RELIC_SLOTS) {
            inv.setItem(slot, placeholder);
        }

        int visibleSlots = getVisibleTemplateCount();
        for (int displayIndex = 0; displayIndex < visibleSlots; displayIndex++) {
            int slot = ADMIN_RELIC_SLOTS[displayIndex];
            ItemStack template = storage.get(page, displayIndex);
            if (template == null) continue;
            if (player.isOp()) {
                String relicId = relicService.getRelicId(template);
                String idLine = relicId == null || relicId.isBlank()
                        ? "<gray>Relic ID: <red>none</red></gray>"
                        : "<gray>Relic ID: <yellow>" + relicId + "</yellow></gray>";
                inv.setItem(slot, withLore(template.clone(), List.of(idLine)));
            } else {
                inv.setItem(slot, template.clone());
            }
        }

        inv.setItem(
                SLOT_PREV,
                controlPotionItem(PREVIOUS_BUTTON_NAME, PREVIOUS_BUTTON_LORE, PotionType.HEALING)
        );
        inv.setItem(
                SLOT_NEXT,
                controlPotionItem(NEXT_BUTTON_NAME, NEXT_BUTTON_LORE, PotionType.LUCK)
        );
        inv.setItem(
                SLOT_CLOSE,
                controlItem(Material.RED_DYE, CLOSE_BUTTON_NAME, CLOSE_BUTTON_LORE)
        );
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.1f);
        player.openInventory(inv);
    }

    public boolean isViewerInventory(Inventory inv) {
        if (inv == null) return false;
        InventoryHolder holder = inv.getHolder();
        return holder instanceof RelicViewerHolder;
    }

    public int getPage(Inventory inv) {
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof RelicViewerHolder rvh) return rvh.getPage();
        return 1;
    }

    public RelicViewerHolder.Mode getMode(Inventory inv) {
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof RelicViewerHolder rvh) return rvh.getMode();
        return RelicViewerHolder.Mode.ADMIN;
    }

    public UUID getOwner(Inventory inv) {
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof RelicViewerHolder rvh) return rvh.getOwner();
        return null;
    }

    public boolean isControl(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Byte b = meta.getPersistentDataContainer().get(CONTROL_KEY, PersistentDataType.BYTE);
        return b != null && b == (byte) 1;
    }

    public boolean isTemplateSlot(int slot) {
        int visibleSlots = getVisibleTemplateCount();
        for (int i = 0; i < visibleSlots; i++) {
            int templateSlot = ADMIN_RELIC_SLOTS[i];
            if (templateSlot == slot) return true;
        }
        return false;
    }

    public int toStorageSlot(int displaySlot) {
        int visibleSlots = getVisibleTemplateCount();
        for (int i = 0; i < visibleSlots; i++) {
            if (ADMIN_RELIC_SLOTS[i] == displaySlot) {
                return i;
            }
        }
        return -1;
    }

    private ItemStack controlItem(Material mat, String name, List<String> lore) {
        ItemStack it = ItemBuilder.of(mat)
                .name(name)
                .lore(lore)
                .build();
        ItemMeta meta = it.getItemMeta();
        meta.getPersistentDataContainer().set(CONTROL_KEY, PersistentDataType.BYTE, (byte) 1);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack controlPotionItem(String name, List<String> lore, PotionType potionType) {
        ItemStack item = controlItem(Material.TIPPED_ARROW, name, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setBasePotionType(potionType);
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
            lore.add(mm.deserialize(line));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack plainItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.setHideTooltip(true);
        item.setItemMeta(meta);
        return item;
    }

    private int getVisibleTemplateCount() {
        return Math.min(storage.getPageSize(), ADMIN_RELIC_SLOTS.length);
    }
}
