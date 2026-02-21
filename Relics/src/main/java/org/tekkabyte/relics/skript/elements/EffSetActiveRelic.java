package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.tekkabyte.relics.skript.SkriptRelicsBridge;

public class EffSetActiveRelic extends Effect {

    private Expression<ItemStack> itemExpr;
    private Expression<Player> playerExpr;
    private Expression<Number> pageExpr;
    private Expression<Number> slotExpr;
    private boolean activate;

    @Override
    protected void execute(Event e) {
        Player player = playerExpr.getSingle(e);
        Number pageNumber = pageExpr.getSingle(e);
        Number slotNumber = slotExpr.getSingle(e);
        if (player == null || pageNumber == null || slotNumber == null) return;

        int page = pageNumber.intValue();
        int slot = slotNumber.intValue();

        if (activate) {
            ItemStack item = itemExpr.getSingle(e);
            if (item == null) return;
            SkriptRelicsBridge.PLAYER_RELIC_SERVICE.equip(player, page, slot, item);
            return;
        }

        SkriptRelicsBridge.PLAYER_RELIC_SERVICE.unequip(player, page, slot);
    }

    @Override
    public String toString(Event e, boolean debug) {
        return activate ? "activate relic for player" : "deactivate relic for player";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        activate = matchedPattern == 0;
        if (activate) {
            itemExpr = (Expression<ItemStack>) exprs[0];
            playerExpr = (Expression<Player>) exprs[1];
            pageExpr = (Expression<Number>) exprs[2];
            slotExpr = (Expression<Number>) exprs[3];
            return true;
        }
        playerExpr = (Expression<Player>) exprs[0];
        pageExpr = (Expression<Number>) exprs[1];
        slotExpr = (Expression<Number>) exprs[2];
        return true;
    }
}
