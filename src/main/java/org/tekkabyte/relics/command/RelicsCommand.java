package org.tekkabyte.relics.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tekkabyte.relics.gui.RelicLoadoutGui;

public class RelicsCommand implements CommandExecutor {
    private static final String RELICS_COMMAND_PERMISSION = "relics.command.relics";

    private final RelicLoadoutGui loadoutGui;

    public RelicsCommand(RelicLoadoutGui loadoutGui) {
        this.loadoutGui = loadoutGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission(RELICS_COMMAND_PERMISSION)) {
            player.sendMessage("No permission.");
            return true;
        }
        loadoutGui.open(player, false);
        return true;
    }
}
