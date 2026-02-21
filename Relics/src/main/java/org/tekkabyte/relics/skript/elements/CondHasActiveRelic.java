package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.tekkabyte.relics.skript.SkriptRelicsBridge;

public class CondHasActiveRelic extends Condition {

    private Expression<Player> playerExpr;
    private Expression<String> relicIdExpr;

    @Override
    public boolean check(Event e) {
        Player player = playerExpr.getSingle(e);
        String relicId = relicIdExpr.getSingle(e);
        if (player == null || relicId == null) return false;
        boolean result = SkriptRelicsBridge.PLAYER_RELIC_SERVICE.hasActiveRelic(player.getUniqueId(), relicId);
        return isNegated() ? !result : result;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "player has active relic";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        playerExpr = (Expression<Player>) exprs[0];
        relicIdExpr = (Expression<String>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }
}
