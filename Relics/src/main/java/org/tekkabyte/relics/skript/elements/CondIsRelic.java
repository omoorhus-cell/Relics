package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.tekkabyte.relics.skript.SkriptRelicsBridge;

public class CondIsRelic extends Condition {

    private Expression<ItemStack> itemExpr;

    @Override
    public boolean check(Event e) {
        ItemStack it = itemExpr.getSingle(e);
        return SkriptRelicsBridge.RELIC_SERVICE.isRelic(it);
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "item is relic";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        itemExpr = (Expression<ItemStack>) exprs[0];
        return true;
    }
}
