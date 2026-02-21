package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.tekkabyte.relics.skript.SkriptRelicsBridge;

public class ExprRelicId extends SimpleExpression<String> {

    private Expression<ItemStack> itemExpr;

    @Override
    protected String[] get(Event e) {
        ItemStack it = itemExpr.getSingle(e);
        String id = SkriptRelicsBridge.RELIC_SERVICE.getRelicId(it);
        if (id == null) return new String[0];
        return new String[]{id};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "relic id";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        itemExpr = (Expression<ItemStack>) exprs[0];
        return true;
    }
}
