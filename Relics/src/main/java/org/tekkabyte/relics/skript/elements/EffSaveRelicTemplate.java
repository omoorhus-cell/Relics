package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.tekkabyte.relics.skript.SkriptRelicsBridge;

public class EffSaveRelicTemplate extends Effect {

    private Expression<ItemStack> itemExpr;
    private Expression<String> idExpr;
    private boolean tagFirst;

    @Override
    protected void execute(Event e) {
        ItemStack it = itemExpr.getSingle(e);
        if (it == null) return;
        ItemStack toSave = it;
        if (tagFirst) {
            String id = idExpr.getSingle(e);
            if (id == null || id.isBlank()) return;
            toSave = SkriptRelicsBridge.RELIC_SERVICE.applyRelic(it, id);
            it.setItemMeta(toSave.getItemMeta());
        }
        int idx = SkriptRelicsBridge.TEMPLATE_STORAGE.saveNextFree(toSave);
        if (idx < 0) return;
        SkriptRelicsBridge.TEMPLATE_STORAGE.save();
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "save item to relic templates";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        tagFirst = matchedPattern == 1;
        itemExpr = (Expression<ItemStack>) exprs[0];
        if (tagFirst) {
            idExpr = (Expression<String>) exprs[1];
        }
        return true;
    }
}
