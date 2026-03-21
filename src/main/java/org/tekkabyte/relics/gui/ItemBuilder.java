package org.tekkabyte.relics.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ItemBuilder {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&#([0-9a-f]{6})");

    private final ItemStack item;
    private final ItemMeta meta;

    private ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder name(String name) {
        meta.displayName(parseLegacy(name));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return this;
        }
        meta.lore(lines.stream().map(this::parseLegacy).toList());
        return this;
    }

    public ItemBuilder hideTooltip() {
        meta.setHideTooltip(true);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    public static Component parseLegacyText(String text) {
        if (text == null) {
            return Component.empty();
        }
        String normalized = normalizeHex(text).replace('\u00A7', '&');
        return LEGACY.deserialize(normalized).decoration(TextDecoration.ITALIC, false);
    }

    private Component parseLegacy(String text) {
        return parseLegacyText(text);
    }

    private static String normalizeHex(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
