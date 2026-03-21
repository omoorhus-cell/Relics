package org.tekkabyte.relics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.tekkabyte.relics.command.RelicCommand;
import org.tekkabyte.relics.command.RelicsCommand;
import org.tekkabyte.relics.command.ViewRelicsCommand;
import org.tekkabyte.relics.gui.RelicLoadoutGui;
import org.tekkabyte.relics.gui.RelicViewerGui;
import org.tekkabyte.relics.listener.RelicLoadoutListener;
import org.tekkabyte.relics.listener.RelicProtectionListener;
import org.tekkabyte.relics.listener.RelicViewerListener;
import org.tekkabyte.relics.player.PlayerRelicService;
import org.tekkabyte.relics.player.PlayerRelicServiceYaml;
import org.tekkabyte.relics.relic.RelicService;
import org.tekkabyte.relics.relic.RelicServiceImpl;
import org.tekkabyte.relics.storage.TemplateStorage;
import org.tekkabyte.relics.storage.TemplateStorageYaml;

public class RelicsPlugin extends JavaPlugin {

    private RelicService relicService;
    private TemplateStorage templateStorage;
    private PlayerRelicService playerRelicService;
    private RelicViewerGui viewerGui;
    private RelicLoadoutGui loadoutGui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        templateStorage = new TemplateStorageYaml(this);
        relicService = new RelicServiceImpl(this, templateStorage);
        templateStorage.load();
        playerRelicService = new PlayerRelicServiceYaml(this, relicService, templateStorage);
        playerRelicService.load();

        viewerGui = new RelicViewerGui(this, relicService, playerRelicService, templateStorage);
        loadoutGui = new RelicLoadoutGui(playerRelicService);

        RelicCommand relicCommand = new RelicCommand(this, relicService, templateStorage, viewerGui);

        PluginCommand relicCmd = getCommand("relic");
        if (relicCmd != null) {
            relicCmd.setExecutor(relicCommand);
            relicCmd.setTabCompleter(relicCommand);
        } else {
            getLogger().severe("Command 'relic' is missing from plugin.yml");
        }

        PluginCommand viewCmd = getCommand("viewrelics");
        if (viewCmd != null) {
            viewCmd.setExecutor(new ViewRelicsCommand(viewerGui));
        } else {
            getLogger().severe("Command 'viewrelics' is missing from plugin.yml");
        }

        PluginCommand relicsCmd = getCommand("relics");
        if (relicsCmd != null) {
            relicsCmd.setExecutor(new RelicsCommand(loadoutGui));
        } else {
            getLogger().severe("Command 'relics' is missing from plugin.yml");
        }

        Bukkit.getPluginManager().registerEvents(new RelicProtectionListener(relicService), this);
        Bukkit.getPluginManager().registerEvents(new RelicViewerListener(viewerGui, loadoutGui, templateStorage, playerRelicService), this);
        Bukkit.getPluginManager().registerEvents(new RelicLoadoutListener(loadoutGui, viewerGui, playerRelicService), this);

    }

    @Override
    public void onDisable() {
        if (templateStorage != null) {
            templateStorage.save();
        }
        if (playerRelicService != null) {
            playerRelicService.save();
        }
    }

    public RelicService getRelicService() {
        return relicService;
    }

    public TemplateStorage getTemplateStorage() {
        return templateStorage;
    }

    public RelicViewerGui getViewerGui() {
        return viewerGui;
    }

    public PlayerRelicService getPlayerRelicService() {
        return playerRelicService;
    }

    public RelicLoadoutGui getLoadoutGui() {
        return loadoutGui;
    }

    public ItemStack getRelicItem(String relicId) {
        return relicService == null ? null : relicService.getRelicItem(relicId);
    }

    public boolean giveRelic(Player player, String relicId) {
        return relicService != null && relicService.giveRelic(player, relicId);
    }

    public void openLoadoutGui(Player player) {
        openLoadoutGui(player, true);
    }

    public void openLoadoutGui(Player player, boolean includeReturnButton) {
        if (player == null || loadoutGui == null) {
            return;
        }
        loadoutGui.open(player, includeReturnButton);
    }
}

