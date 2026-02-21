package org.tekkabyte.relics.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.plugin.java.JavaPlugin;
import org.tekkabyte.relics.gui.RelicLoadoutGui;
import org.tekkabyte.relics.gui.RelicViewerGui;
import org.tekkabyte.relics.player.PlayerRelicService;
import org.tekkabyte.relics.relic.RelicService;
import org.tekkabyte.relics.storage.TemplateStorage;

public final class SkriptRelicsBridge {

    private static boolean registered = false;
    public static RelicService RELIC_SERVICE;
    public static TemplateStorage TEMPLATE_STORAGE;
    public static PlayerRelicService PLAYER_RELIC_SERVICE;
    public static RelicLoadoutGui LOADOUT_GUI;
    public static RelicViewerGui VIEWER_GUI;

    private SkriptRelicsBridge() {}

    public static void tryRegister(JavaPlugin plugin, RelicService relicService, TemplateStorage templateStorage, PlayerRelicService playerRelicService, RelicLoadoutGui loadoutGui, RelicViewerGui viewerGui) {
        if (registered) return;
        RELIC_SERVICE = relicService;
        TEMPLATE_STORAGE = templateStorage;
        PLAYER_RELIC_SERVICE = playerRelicService;
        LOADOUT_GUI = loadoutGui;
        VIEWER_GUI = viewerGui;

        try {
            SkriptAddon addon = Skript.registerAddon(plugin);
            addon.loadClasses("org.tekkabyte.relics", "skript.elements");
            registered = true;
            plugin.getLogger().info("Hooked into Skript for Relics.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to hook into Skript: " + e.getMessage());
        }
    }
}
