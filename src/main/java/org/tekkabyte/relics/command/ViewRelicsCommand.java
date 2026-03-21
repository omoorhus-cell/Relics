package org.tekkabyte.relics.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tekkabyte.relics.gui.RelicViewerGui;

public class ViewRelicsCommand implements CommandExecutor {

    private final RelicViewerGui gui;

    public ViewRelicsCommand(RelicViewerGui gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.isOp()) {
            sender.sendMessage("No permission.");
            return true;
        }

        int page = 1;
        if (args.length >= 1) {
            try { page = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }

        gui.openAdmin(player, Math.max(1, page));
        return true;
    }
}
