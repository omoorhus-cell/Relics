package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.tekkabyte.relics.skript.SkriptRelicsBridge;

public class ExprActiveRelicAtSlot extends SimpleExpression<ItemStack> {

    private Expression<Number> pageExpr;
    private Expression<Number> slotExpr;
    private Expression<Player> playerExpr;

    @Override
    protected ItemStack[] get(Event e) {
        Number pageNumber = pageExpr.getSingle(e);
        Number slotNumber = slotExpr.getSingle(e);
        Player player = playerExpr.getSingle(e);
        if (pageNumber == null || slotNumber == null || player == null) return new ItemStack[0];
        ItemStack item = SkriptRelicsBridge.PLAYER_RELIC_SERVICE.getEquipped(
                player.getUniqueId(),
                pageNumber.intValue(),
                slotNumber.intValue()
        );
        if (item == null) return new ItemStack[0];
        return new ItemStack[]{item};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "active relic at page/slot";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        pageExpr = (Expression<Number>) exprs[0];
        slotExpr = (Expression<Number>) exprs[1];
        playerExpr = (Expression<Player>) exprs[2];
        return true;
    }
}
