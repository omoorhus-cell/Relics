package org.tekkabyte.relics.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.tekkabyte.relics.relic.RelicService;

public class RelicProtectionListener implements Listener {

    private final RelicService relicService;

    public RelicProtectionListener(RelicService relicService) {
        this.relicService = relicService;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack it = e.getItemInHand();
        if (!relicService.isRelic(it)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack it = e.getItem();
        if (!relicService.isRelic(it)) return;
        e.setUseInteractedBlock(Event.Result.DENY);
        e.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (!relicService.isRelic(e.getItem())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();
        ItemStack it = inv.getItemInMainHand();
        if (!relicService.isRelic(it)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getInventory() == null) return;
        for (ItemStack it : e.getInventory().getMatrix()) {
            if (relicService.isRelic(it)) {
                e.setCancelled(true);
                if (e.getWhoClicked() instanceof Player p) {
                    p.sendMessage("§cYou can't craft with Relics.");
                }
                return;
            }
        }
    }
}
