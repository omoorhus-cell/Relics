package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.tekkabyte.relics.event.RelicEquipEvent;
import org.tekkabyte.relics.event.RelicUnequipEvent;

public class ExprEventRelicId extends SimpleExpression<String> {

    @Override
    protected String[] get(Event e) {
        if (e instanceof RelicEquipEvent equipEvent) {
            String id = equipEvent.getRelicId();
            return id == null ? new String[0] : new String[]{id};
        }
        if (e instanceof RelicUnequipEvent unequipEvent) {
            String id = unequipEvent.getRelicId();
            return id == null ? new String[0] : new String[]{id};
        }
        return new String[0];
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
        return "event relic id";
    }

    @Override
    public boolean init(ch.njol.skript.lang.Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        return true;
    }
}
