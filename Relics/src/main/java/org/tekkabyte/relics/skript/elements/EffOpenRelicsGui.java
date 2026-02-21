package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.tekkabyte.relics.skript.SkriptRelicsBridge;

public class EffOpenRelicsGui extends Effect {

    private Expression<Player> playerExpr;

    @Override
    protected void execute(Event e) {
        Player p = playerExpr.getSingle(e);
        if (p == null) return;
        if (SkriptRelicsBridge.LOADOUT_GUI != null) {
            SkriptRelicsBridge.LOADOUT_GUI.open(p);
            return;
        }
        SkriptRelicsBridge.VIEWER_GUI.openPlayer(p, 1);
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "open relics gui";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        playerExpr = (Expression<Player>) exprs[0];
        return true;
    }
}
