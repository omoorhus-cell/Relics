package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event;
import org.tekkabyte.relics.skript.SkriptRelicsBridge;

public class EffTagRelic extends Effect {

    private Expression<ItemStack> itemExpr;
    private Expression<String> idExpr;

    @Override
    protected void execute(Event e) {
        ItemStack it = itemExpr.getSingle(e);
        String id = idExpr.getSingle(e);
        if (it == null || id == null) return;
        ItemStack tagged = SkriptRelicsBridge.RELIC_SERVICE.applyRelic(it, id);
        it.setItemMeta(tagged.getItemMeta());
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "tag item as relic";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        itemExpr = (Expression<ItemStack>) exprs[0];
        idExpr = (Expression<String>) exprs[1];
        return true;
    }
}
