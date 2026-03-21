package org.tekkabyte.relics.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.tekkabyte.relics.RelicsPlugin;
import org.tekkabyte.relics.gui.RelicViewerGui;
import org.tekkabyte.relics.relic.RelicService;
import org.tekkabyte.relics.storage.TemplateStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RelicCommand implements CommandExecutor, TabCompleter {

    private final RelicsPlugin plugin;
    private final RelicService relicService;
    private final TemplateStorage templateStorage;
    private final RelicViewerGui viewerGui;

    public RelicCommand(RelicsPlugin plugin, RelicService relicService, TemplateStorage templateStorage, RelicViewerGui viewerGui) {
        this.plugin = plugin;
        this.relicService = relicService;
        this.templateStorage = templateStorage;
        this.viewerGui = viewerGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(msg("player-only"));
            return true;
        }

        if (args.length == 0) {
            if (!p.hasPermission("relics.admin")) {
                p.sendMessage("/relic view");
                return true;
            }
            p.sendMessage("/relic set <id> [--save]");
            p.sendMessage("/relic save <id>");
            p.sendMessage("/relic delete <page> <slot>");
            p.sendMessage("/relic replace <page> <slot>");
            p.sendMessage("/relic give <page> <slot> [player]");
            p.sendMessage("/relic view");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("view")) {
            if (!p.isOp()) {
                p.sendMessage(msg("no-permission"));
                return true;
            }
            viewerGui.openAdmin(p, 1);
            return true;
        }

        if (!p.hasPermission("relics.admin")) {
            p.sendMessage(msg("no-permission"));
            return true;
        }

        if (sub.equals("set")) {
            if (args.length < 2) {
                p.sendMessage("Usage: /relic set <id> [--save]");
                return true;
            }
            String id = args[1];
            boolean save = args.length >= 3 && args[2].equalsIgnoreCase("--save");

            PlayerInventory inv = p.getInventory();
            ItemStack hand = inv.getItemInMainHand();
            if (hand == null || hand.getType().isAir()) {
                p.sendMessage(msg("must-hold-item"));
                return true;
            }

            ItemStack tagged = relicService.applyRelic(hand, id);
            inv.setItemInMainHand(tagged);
            p.sendMessage(msg("tagged").replace("{id}", id));

            if (save) {
                int idx = templateStorage.saveNextFree(tagged);
                if (idx < 0) {
                    p.sendMessage("No space left in template storage.");
                    return true;
                }
                templateStorage.save();
                int page = templateStorage.pageOf(idx);
                int slot = templateStorage.slotOf(idx);
                p.sendMessage(msg("saved").replace("{page}", String.valueOf(page)).replace("{slot}", String.valueOf(slot)));
            }
            return true;
        }

        if (sub.equals("save")) {
            if (args.length < 2) {
                p.sendMessage("Usage: /relic save <id>");
                return true;
            }
            String id = args[1];
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType().isAir()) {
                p.sendMessage(msg("must-hold-item"));
                return true;
            }
            ItemStack tagged = relicService.applyRelic(hand, id);
            p.getInventory().setItemInMainHand(tagged);
            int idx = templateStorage.saveNextFree(tagged);
            if (idx < 0) {
                p.sendMessage("No space left in template storage.");
                return true;
            }
            templateStorage.save();
            int page = templateStorage.pageOf(idx);
            int slot = templateStorage.slotOf(idx);
            p.sendMessage(msg("tagged").replace("{id}", id));
            p.sendMessage(msg("saved").replace("{page}", String.valueOf(page)).replace("{slot}", String.valueOf(slot)));
            return true;
        }

        if (sub.equals("delete")) {
            if (args.length < 3) {
                p.sendMessage("Usage: /relic delete <page> <slot>");
                return true;
            }
            int page = parseInt(args[1], -1);
            int slot = parseInt(args[2], -1);
            if (page < 1 || slot < 0 || slot >= templateStorage.getPageSize()) {
                p.sendMessage("Invalid page/slot.");
                return true;
            }
            boolean ok = templateStorage.delete(page, slot);
            templateStorage.save();
            p.sendMessage(ok ? msg("deleted") : msg("nothing-there"));
            return true;
        }

        if (sub.equals("replace")) {
            if (args.length < 3) {
                p.sendMessage("Usage: /relic replace <page> <slot>");
                return true;
            }
            int page = parseInt(args[1], -1);
            int slot = parseInt(args[2], -1);
            if (page < 1 || slot < 0 || slot >= templateStorage.getPageSize()) {
                p.sendMessage("Invalid page/slot.");
                return true;
            }
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (!relicService.isRelic(hand)) {
                p.sendMessage(msg("must-be-relic"));
                return true;
            }
            templateStorage.set(page, slot, hand);
            templateStorage.save();
            p.sendMessage(msg("replaced"));
            return true;
        }

        if (sub.equals("give")) {
            if (args.length < 3) {
                p.sendMessage("Usage: /relic give <page> <slot> [player]");
                return true;
            }
            int page = parseInt(args[1], -1);
            int slot = parseInt(args[2], -1);
            if (page < 1 || slot < 0 || slot >= templateStorage.getPageSize()) {
                p.sendMessage("Invalid page/slot.");
                return true;
            }
            Player target = p;
            if (args.length >= 4) {
                target = Bukkit.getPlayerExact(args[3]);
                if (target == null) {
                    p.sendMessage(msg("player-not-found"));
                    return true;
                }
            }
            ItemStack it = templateStorage.get(page, slot);
            if (it == null) {
                p.sendMessage("No template in that slot.");
                return true;
            }
            target.getInventory().addItem(it.clone());
            p.sendMessage(msg("gave").replace("{player}", target.getName()));
            return true;
        }

        p.sendMessage("Unknown subcommand.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("set");
            out.add("save");
            out.add("delete");
            out.add("replace");
            out.add("give");
            out.add("view");
            return out;
        }
        return out;
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private String msg(String key) {
        FileConfiguration cfg = plugin.getConfig();
        String raw = cfg.getString("messages." + key, "Missing message: " + key);
        return raw.replace('?', '*');
    }
}
